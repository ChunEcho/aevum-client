# Aevum Client

基于 [Frog](https://github.com/awds14592478/FrogClient) 分支魔改的 Minecraft 1.21.1 Fabric 客户端，针对 2b2t.xin。

## 安装

1. 下载 aevum-client-1.0.0.jar
2. 放进 .minecraft/mods/
3. 启动游戏

需要 Fabric Loader >= 0.15.0、Java 21、Fabric API。

## License

MIT

## 更新：无伤虚空重锤（VoidMaceGhost）

新增模块 `VoidMaceGhost`，假下沉触发重锤粉碎伤害，**不打自己**。

之前版本在虚空里发 `onGround=true` 想清 `fallDistance`，这会导致自伤 ——
原版 `Entity#updateFallDistance` 在 `onGround=true` 时会调 `block.onLandedUpon(...)`，
而 `Block#onLandedUpon` 默认实现就是 `entity.handleFallDamage(fallDistance, 1.0f, ...)`，
连空气方块都会触发（`AirBlock` 没重写它）。这就是「一点点自伤」的来源。

现在全程 `onGround=false`，只走 `fallDistance -= dy` 分支，**既不清零也不吃伤害**：

```
1. PositionAndOnGround(x, targetY, z, false)   ← 下沉，fallDistance 累加
2. PositionAndOnGround(x, y, z, false)         ← 回位，继续累加
3. PlayerInteractEntityC2SPacket.attack(...)   ← 粉碎成立
4. PositionAndOnGround(x, y, z, false)         ← 收尾
```

单 tick 内完成，避免跨 tick 被客户端自己的移动包干扰。
服务端有「moved too quickly」拦截（阈值 `100.0 × 单tick包数`），所以必须单包直达。

### 设置

| 设置 | 默认 | 说明 |
|---|---|---|
| `DiveHeight` | 26.0 | 下沉深度（必须 >20 才有粉碎） |
| `BelowWorld` | false | 下沉到 VoidY（**会触发虚空伤害**，平地别开） |
| `TargetRange` | 8.0 | 搜索半径 |
| `AttackRange` | 6.0 | 出手距离 |
| `GhostHand` | true | 幽灵手（只在攻击瞬间换锤） |
| `SpaceCheck` | true | 检查下落路径为空气 |
| `TotemGuard` | true | 低血保护 |

模块名后缀显示状态：`notarget` `nomace` `charge` `lowspace` `blocked` `water` `lowhp` `idle`

### 无伤汉化

`Lang` 增加了**驼峰分词**，能翻之前翻不出来的设置名：

```
TargetRange → Target + Range → 目标范围
```

规则：先查整串，查不到则分词逐词查，**任一词查不到就整串放弃**（避免中英混杂）。

翻译表在 `src/main/resources/assets/aevum/lang/map.txt`（`英文<TAB>中文`）。

### 源码

新版本包名是 `dev.chun`（旧仓库里的 `dev.aevum` 是历史版本）：

```
src/main/java/dev/chun/mod/modules/impl/combat/VoidMaceGhost.java
src/main/java/dev/chun/api/utils/i18n/Lang.java
src/main/resources/assets/aevum/lang/map.txt
```
