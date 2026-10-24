package gg.alter.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 服务端静默旋转管理器（Server-side Silent Rotation）。
 *
 * <p>核心能力：把模块期望的朝向单独下发给服务端，客户端视角保持不变。
 * 通过 {@link #onPacketSend(Object)} 拦截 {@link ServerboundMovePlayerPacket}，
 * 把包内的 yaw / pitch 替换成伪造值。</p>
 *
 * <p>反作弊对抗要点：所有旋转增量都经过 <b>GCD 量化</b>
 * （{@link #applyMouseDelta(float, float)}），保证输出角度在数学上
 * 与原版鼠标输入产生的角度完全同构，消除「旋转增量不是鼠标网格整数倍」的特征。</p>
 *
 * <h2>使用方式</h2>
 * <pre>{@code
 * // 1. 申请朝向控制权（owner 用于抢占式加锁）
 * RotationManager.setServerRotation(this, targetYaw, targetPitch);
 *
 * // 2. 发包时由 mixin 调用，返回替换后的包
 * Object newPacket = RotationManager.onPacketSend(packet);
 *
 * // 3. 用完释放，下一包会自动回正到真实朝向
 * RotationManager.releaseServerRotation(this);
 * }</pre>
 *
 * <p><b>注意</b>：{@link #applyMouseDelta(float, float)} 系列方法会真实旋转本地玩家，
 * 属于「服务端静默 + 客户端真实旋转」模式，不是纯静默。</p>
 */
public final class RotationManager {

    /** 当前下发到服务端的 yaw。 */
    private static volatile float serverYaw;

    /** 当前下发到服务端的 pitch。 */
    private static volatile float serverPitch;

    /** 是否处于朝向接管状态。 */
    private static volatile boolean active;

    /** 是否需要把客户端真实朝向同步回服务端（释放后的一次性回正）。 */
    private static volatile boolean resync;

    /** 当前持有朝向控制权的模块实例，用于抢占式加锁。 */
    private static Object owner;

    /** 接管状态的过期时间戳（毫秒），防止模块忘记 release 导致朝向永久锁死。 */
    private static long expireAt;

    /** 全局锁，保护上述所有可变状态。 */
    private static final Object LOCK = new Object();

    /** yaw 方向的鼠标计数余数，用于跨帧累积不足一个网格的零头。 */
    private static float yawRemainder;

    /** pitch 方向的鼠标计数余数。 */
    private static float pitchRemainder;

    private RotationManager() {
    }

    // ==================== 朝向接管 ====================

    /**
     * 申请朝向控制权并设置下发给服务端的朝向。
     *
     * <p>若已有其他模块持有控制权，本次调用会被静默忽略（抢占式语义：
     * 先到先得，避免多模块争抢导致朝向抖动）。</p>
     *
     * @param caller 调用方实例，作为控制权归属标识
     * @param yaw    下发给服务端的 yaw
     * @param pitch  下发给服务端的 pitch
     */
    public static void setServerRotation(Object caller, float yaw, float pitch) {
        synchronized (LOCK) {
            if (active && owner != null && owner != caller) {
                return;
            }
            owner = caller;
            serverYaw = yaw;
            serverPitch = pitch;
            active = true;
            resync = false;
            expireAt = System.currentTimeMillis() + 100L;
        }
    }

    /**
     * 释放朝向控制权。仅当调用方是当前持有者时生效。
     *
     * <p>释放后会在下一个发包周期把客户端真实朝向同步回服务端（一次性回正）。</p>
     *
     * @param caller 调用方实例
     */
    public static void releaseServerRotation(Object caller) {
        synchronized (LOCK) {
            if (owner != caller) {
                return;
            }
            active = false;
            owner = null;
            resync = true;
        }
    }

    /** 当前是否处于朝向接管状态。 */
    public static boolean hasServerRotation() {
        return active;
    }

    // ==================== 鼠标余数 ====================

    /** 返回当前累积的鼠标计数余数 {@code [yawRemainder, pitchRemainder]}。 */
    public static float[] mouseRemainders() {
        return new float[]{yawRemainder, pitchRemainder};
    }

    /** 清零鼠标计数余数。 */
    public static void resetMouseRemainders() {
        yawRemainder = 0F;
        pitchRemainder = 0F;
    }

    /** 把值钳制到 {@code [-limit, limit]}。 */
    public static float clamp(float value, float limit) {
        return value > limit ? limit : (value < -limit ? -limit : value);
    }

    // ==================== GCD 量化（反作弊核心） ====================

    /**
     * 原版鼠标灵敏度换算系数。
     *
     * <p>原版逻辑：{@code multiplier = (sensitivity * 0.6 + 0.2)^3 * 8}。
     * 该系数决定「1 个鼠标计数对应多少度」。必须与实际设置保持一致，
     * 否则量化结果会偏离真实鼠标行为，反而产生新特征。</p>
     *
     * @param mc Minecraft 客户端实例
     * @return 当前灵敏度下的旋转乘数
     */
    public static double vanillaMouseMultiplier(Minecraft mc) {
        double d = mc.options.sensitivity().get() * 0.6D + 0.2D;
        return d * d * d * 8.0D;
    }

    /**
     * 鼠标网格（最小旋转步长）。
     *
     * <p>等于「1 个鼠标计数」对应的角度：{@code multiplier * 0.15}。
     * 真人鼠标产生的所有旋转增量都必然是此值的整数倍，
     * 因此把 AI 旋转对齐到该网格即可消除 GCD 特征。</p>
     *
     * @param mc Minecraft 客户端实例
     * @return 最小旋转步长（度），下限 0.15
     */
    public static float mouseGrid(Minecraft mc) {
        float grid = (float) vanillaMouseMultiplier(mc) * 0.15F;
        return grid <= 0F ? 0.15F : grid;
    }

    // ==================== 角度工具 ====================

    /** 把角度归一化到 {@code [-180, 180)}。 */
    public static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360F;
        if (wrapped >= 180F) {
            wrapped -= 360F;
        }
        if (wrapped < -180F) {
            wrapped += 360F;
        }
        return wrapped;
    }

    /** 把 {@code yaw} 归一化到以 {@code reference} 为基准的等价角度，避免跨 ±180 时跳变。 */
    public static float wrapToReference(float yaw, float reference) {
        return reference + wrapDegrees(yaw - reference);
    }

    /** 到位死区。当前实现为 0，即关闭死区（死区会产生可被识别的速度突变）。 */
    public static float arriveDeadzone(Minecraft mc) {
        return 0F;
    }

    /**
     * 计算从 {@code from} 看向 {@code to} 所需的 {@code [yaw, pitch]}。
     *
     * @return {@code [yaw, pitch]}（度）
     */
    public static float[] lookAt(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return new float[]{
                (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D),
                (float) (-Math.toDegrees(Math.atan2(dy, horizontal)))
        };
    }

    // ==================== 瞄准点计算 ====================

    /** 取碰撞箱中心，并沿 Y 轴偏移 {@code offset}。 */
    public static Vec3 aimCenter(AABB box, double offset) {
        Vec3 center = box.getCenter();
        return new Vec3(center.x, center.y + offset, center.z);
    }

    /** 把点 {@code p} 钳制到碰撞箱 {@code box} 内。 */
    public static Vec3 clampToBox(AABB box, Vec3 p) {
        return new Vec3(
                Math.max(box.minX, Math.min(box.maxX, p.x)),
                Math.max(box.minY, Math.min(box.maxY, p.y)),
                Math.max(box.minZ, Math.min(box.maxZ, p.z))
        );
    }

    /**
     * 判断从 {@code from} 沿 {@code look} 方向发射的射线是否命中碰撞箱。
     *
     * @param box     目标碰撞箱
     * @param from    射线起点
     * @param look    射线方向（未归一化也可）
     * @param reach   射线长度
     * @param tolerance 碰撞箱外扩容差
     */
    public static boolean isAimingAtBox(AABB box, Vec3 from, Vec3 look, double reach, double tolerance) {
        AABB expanded = box.inflate(tolerance);
        return expanded.contains(from) || expanded.clip(from, from.add(look.scale(reach))).isPresent();
    }

    /** 求从 {@code from} 沿 {@code look} 方向与碰撞箱的交点，无交点则返回中心。 */
    public static Vec3 bestAimPoint(AABB box, Vec3 from, Vec3 look) {
        return box.clip(from, from.add(look.scale(64.0D))).orElse(box.getCenter());
    }

    // ==================== 发包拦截 ====================

    /**
     * 发包拦截入口。由 mixin 在 {@code ClientConnection.send} 处调用。
     *
     * <p>处理逻辑：</p>
     * <ol>
     *   <li>只处理 {@link ServerboundMovePlayerPacket}，其他包原样返回</li>
     *   <li>接管超时则自动失效并标记回正</li>
     *   <li>接管中：替换为伪造朝向</li>
     *   <li>刚释放：替换为客户端真实朝向（一次性回正）</li>
     *   <li>其余情况：原样返回</li>
     * </ol>
     *
     * @param packet 原始包
     * @return 替换后的包，或原包
     */
    public static Object onPacketSend(Object packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket movePacket)) {
            return packet;
        }

        float yaw;
        float pitch;

        synchronized (LOCK) {
            if (active && System.currentTimeMillis() > expireAt) {
                active = false;
                owner = null;
                resync = true;
            }

            if (active) {
                yaw = serverYaw;
                pitch = serverPitch;
            } else if (resync) {
                resync = false;
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    return packet;
                }
                yaw = player.getYRot();
                pitch = player.getXRot();
            } else {
                return packet;
            }
        }

        if (movePacket.hasPosition()) {
            return new ServerboundMovePlayerPacket.PosRot(
                    movePacket.getX(0.0D),
                    movePacket.getY(0.0D),
                    movePacket.getZ(0.0D),
                    yaw,
                    pitch,
                    movePacket.isOnGround(),
                    movePacket.horizontalCollision()
            );
        }
        return new ServerboundMovePlayerPacket.Rot(
                yaw,
                pitch,
                movePacket.isOnGround(),
                movePacket.horizontalCollision()
        );
    }

    // ==================== GCD 量化旋转 ====================

    /** 按鼠标增量旋转视角（默认启用 GCD 量化）。 */
    public static float[] applyMouseDelta(float deltaYaw, float deltaPitch) {
        return applyMouseDelta(deltaYaw, deltaPitch, true);
    }

    /**
     * 按鼠标增量旋转视角，并把增量量化到鼠标网格。
     *
     * <p>流程：目标角度 ÷ 网格 = 鼠标计数 → 加上上帧余数 → 四舍五入取整
     * → 保存新余数 → 按整数计数旋转。这样多帧累积后的总旋转量依然精确，
     * 但单帧输出必定是网格整数倍。</p>
     *
     * @param deltaYaw   期望的 yaw 增量（度）
     * @param deltaPitch 期望的 pitch 增量（度）
     * @param quantize   保留参数，当前未使用
     * @return 实际产生的 {@code [yawDelta, pitchDelta]}
     */
    public static float[] applyMouseDelta(float deltaYaw, float deltaPitch, boolean quantize) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return new float[]{0F, 0F};
        }

        float grid = mouseGrid(mc);
        float yawCounts = deltaYaw / grid + yawRemainder;
        float pitchCounts = deltaPitch / grid + pitchRemainder;

        long yawSteps = Math.round(yawCounts);
        long pitchSteps = Math.round(pitchCounts);

        yawRemainder = yawCounts - yawSteps;
        pitchRemainder = pitchCounts - pitchSteps;

        return turn(mc, yawSteps, pitchSteps);
    }

    /**
     * 直接按整数鼠标计数旋转视角（用于外部已算好计数的场景）。
     *
     * @param yawCounts      yaw 方向的鼠标计数
     * @param pitchCounts    pitch 方向的鼠标计数
     * @param yawRemainder   传入的 yaw 余数（会被覆盖保存）
     * @param pitchRemainder 传入的 pitch 余数（会被覆盖保存）
     */
    public static float[] applyMouseCounts(int yawCounts, int pitchCounts,
                                           float yawRemainder, float pitchRemainder) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return new float[]{0F, 0F};
        }
        RotationManager.yawRemainder = yawRemainder;
        RotationManager.pitchRemainder = pitchRemainder;
        return turn(mc, yawCounts, pitchCounts);
    }

    /**
     * 按鼠标计数真实旋转本地玩家视角。
     *
     * @return 实际产生的 {@code [yawDelta, pitchDelta]}
     */
    private static float[] turn(Minecraft mc, long yawSteps, long pitchSteps) {
        LocalPlayer player = mc.player;
        float oldYaw = player.getYRot();
        float oldPitch = player.getXRot();
        double multiplier = vanillaMouseMultiplier(mc);
        player.turn(yawSteps * multiplier, pitchSteps * multiplier);
        return new float[]{
                player.getYRot() - oldYaw,
                player.getXRot() - oldPitch
        };
    }
}
