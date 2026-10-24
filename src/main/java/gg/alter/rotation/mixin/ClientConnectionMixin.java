package gg.alter.rotation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import gg.alter.rotation.RotationManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

/**
 * 发包拦截 mixin。
 *
 * <p>在 {@link Connection#send(Packet)} 的 HEAD 处替换参数，
 * 把 {@link net.minecraft.network.protocol.game.ServerboundMovePlayerPacket}
 * 的 yaw / pitch 换成 {@link RotationManager} 托管的伪造朝向。</p>
 *
 * <p>使用 {@code @ModifyVariable} 而非 {@code @Inject + ci.cancel()}，
 * 避免「取消后重新投递」造成的递归风险。</p>
 */
@Mixin(Connection.class)
public class ClientConnectionMixin {

    @ModifyVariable(
            method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private Packet<?> rotationManager$modifyOutgoingPacket(Packet<?> packet) {
        Object replaced = RotationManager.onPacketSend(packet);
        if (replaced instanceof Packet<?> result) {
            return result;
        }
        return packet;
    }
}