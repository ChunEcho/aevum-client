package dev.aevum;

import net.fabricmc.api.ClientModInitializer;

/**
 * Aevum Client 的客户端入口点。
 *
 * <p>本类不做任何初始化动作。{@link RotationManager} 是全静态工具类，
 * 所有状态按需懒初始化。此入口仅用于满足 Fabric 的 entrypoint 声明，
 * 同时作为 mod 被加载的标记。</p>
 *
 * <p>实际接入方式见 README：把 {@link RotationManager#onPacketSend(Object)}
 * 挂到发包 mixin 上即可。</p>
 */
public final class AevumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 无需初始化。RotationManager 为纯静态工具类。
    }
}