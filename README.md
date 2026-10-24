# RotationManager

Minecraft 服务端静默旋转管理器（Server-side Silent Rotation），带 GCD 量化。

## 这是什么

一个独立的 `RotationManager` 工具类，用于把「模块期望的朝向」单独下发给服务端，同时不影响客户端视角。

核心是 **GCD 量化**：把 AI 计算的旋转角度对齐到原版鼠标网格，使输出角度在数学上与原版鼠标输入同构，消除「旋转增量不是鼠标网格整数倍」这一反作弊特征。

## 核心原理

### 1. 鼠标网格

原版 MC 的视角旋转链路：

```
MouseHandler.turn(dx, dy)
  → player.turn(dx * multiplier, dy * multiplier)
    → setYRot(yRot + dx * multiplier * 0.15)
```

其中：

```java
multiplier = (sensitivity * 0.6 + 0.2)^3 * 8
grid       = multiplier * 0.15
```

**1 个鼠标计数 = `grid` 度**。真人鼠标产生的所有旋转增量，必然是 `grid` 的整数倍。

### 2. 量化算法

```java
float grid  = mouseGrid(mc);
float counts = targetDelta / grid + remainder;  // 加上上帧余数
long  steps  = Math.round(counts);              // 取整
remainder    = counts - steps;                  // 保存新余数
player.turn(steps * multiplier, ...);           // 按整数计数旋转
```

余数跨帧累积，保证多帧后的总旋转量依然精确；单帧输出必定是网格整数倍。

### 3. 服务端静默

```java
// 申请控制权
RotationManager.setServerRotation(this, yaw, pitch);

// 发包拦截（由 mixin 调用）
Object pkt = RotationManager.onPacketSend(packet);

// 释放，下一包自动回正
RotationManager.releaseServerRotation(this);
```

`onPacketSend` 拦截 `ServerboundMovePlayerPacket`，把包内 yaw/pitch 替换成伪造值。100ms 自动过期防止模块忘记 release 导致朝向锁死。

## 使用方法

### 1. 拷贝源码

把 `src/main/java/gg/alter/rotation/RotationManager.java` 放到你的项目里，改 package 即可。

### 2. 添加发包 mixin

```java
@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        Object replaced = RotationManager.onPacketSend(packet);
        if (replaced != packet) {
            ci.cancel();
            // 重新投递替换后的包（注意避免递归）
            connection.send((Packet<?>) replaced);
        }
    }
}
```

实际实现建议用 `@ModifyVariable` 直接改写参数，避免递归：

```java
@ModifyVariable(method = "send", at = @At("HEAD"), argsOnly = true)
private Packet<?> modifyPacket(Packet<?> packet) {
    return (Packet<?>) RotationManager.onPacketSend(packet);
}
```

### 3. 在模块里调用

```java
// 每 tick 更新目标朝向
float[] rot = RotationManager.lookAt(player.getEyePosition(), targetPos);
RotationManager.setServerRotation(this, rot[0], rot[1]);

// 发包攻击
mc.getConnection().send(new ServerboundInteractPacket(...));

// 攻击完成，释放
RotationManager.releaseServerRotation(this);
```

## API

### 朝向接管

| 方法 | 说明 |
|---|---|
| `setServerRotation(Object caller, float yaw, float pitch)` | 申请控制权并设置朝向。已有其他 owner 时忽略 |
| `releaseServerRotation(Object caller)` | 释放控制权，下一包回正 |
| `hasServerRotation()` | 是否处于接管状态 |
| `onPacketSend(Object packet)` | 发包拦截入口 |

### GCD 量化

| 方法 | 说明 |
|---|---|
| `vanillaMouseMultiplier(Minecraft mc)` | 原版灵敏度乘数 `(s*0.6+0.2)^3*8` |
| `mouseGrid(Minecraft mc)` | 鼠标网格 `multiplier*0.15` |
| `applyMouseDelta(float dYaw, float dPitch)` | 按增量旋转，自动量化 |
| `applyMouseCounts(int yc, int pc, float yr, float pr)` | 按整数计数旋转 |
| `mouseRemainders()` / `resetMouseRemainders()` | 读取/清零余数 |

### 角度与瞄准

| 方法 | 说明 |
|---|---|
| `wrapDegrees(float d)` | 归一化到 `[-180, 180)` |
| `wrapToReference(float y, float ref)` | 以 ref 为基准归一化，避免 ±180 跳变 |
| `lookAt(Vec3 from, Vec3 to)` | 计算朝向目标的 `[yaw, pitch]` |
| `aimCenter(AABB box, double offset)` | 碰撞箱中心 + Y 偏移 |
| `clampToBox(AABB box, Vec3 p)` | 把点钳制到碰撞箱内 |
| `isAimingAtBox(...)` | 射线是否命中碰撞箱 |
| `bestAimPoint(AABB box, Vec3 from, Vec3 look)` | 求射线与碰撞箱的交点 |

## 已知限制

1. **100ms 过期偏短** — 网络卡顿时从 `setServerRotation` 到下一个 `MovePlayerPacket` 可能超时，导致朝向闪回。建议改成 250~500ms，或按 tick 计数。

2. **`applyMouseDelta` 会真实旋转客户端** — 调用 `player.turn()` 会触发 `setYRot`，客户端视角真的会转。这是「服务端静默 + 客户端真实旋转」模式，不是纯静默。真正的纯静默需要拦截 `Entity.turn` 或直接改包，不走 `player.turn()`。

3. **余数无同步保护** — `yawRemainder` / `pitchRemainder` 没有 volatile 或锁保护。如果渲染线程和 tick 线程同时调用，存在竞态（影响很小，最多丢一点角度）。

4. **只处理 `ServerboundMovePlayerPacket`** — 1.21 中攻击包、放方块包不带朝向字段，所以这里不处理是对的。但版本升级后协议若变化会静默失效。

5. **`arriveDeadzone` 是空实现** — 恒返回 0，即关闭死区。死区会产生可被识别的速度突变。

6. **`quantize` 参数未使用** — `applyMouseDelta(dYaw, dPitch, boolean quantize)` 的三参版本中 `quantize` 是保留参数，当前无实际作用。

## 环境

- Minecraft 1.21.1
- Fabric Loader
- Java 21
- 使用 **Mojang 官方映射**（`Minecraft` / `LocalPlayer` / `Vec3` / `AABB` / `ServerboundMovePlayerPacket`）

## 免责声明

本项目仅供**学习与研究** Minecraft 网络协议与反作弊检测机制使用。

在多人服务器上使用此类工具通常违反服务器规则，可能导致封禁。请仅在**自己拥有或明确授权**的环境中测试。使用者需自行承担全部责任。

## License

MIT