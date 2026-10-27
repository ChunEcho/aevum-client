/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.asm.accessors.IPlayerMoveC2SPacket;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.exploit.BowBomb;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;

public class NoFall
extends Module {
    private final EnumSetting<NoFallMode> mode = this.add(new EnumSetting<NoFallMode>("Mode", NoFallMode.Packet));
    private final SliderSetting getDistance = this.add(new SliderSetting("Distance", 3.0, 0.0, 8.0, 0.1));

    public NoFall() {
        super("NoFall", "Prevents fall damage.", Module.Category.Player);
        this.setChinese("\u6ca1\u6709\u6454\u843d\u4f24\u5bb3");
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (NoFall.nullCheck()) {
            return;
        }
        if (this.mode.is(NoFallMode.Grim) && this.checkFalling()) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(NoFall.mc.player.getX(), NoFall.mc.player.getY() + 1.0E-9, NoFall.mc.player.getZ(), NoFall.mc.player.getYRot(), NoFall.mc.player.getXRot(), false));
            NoFall.mc.player.method_38785();
        }
    }

    private boolean checkFalling() {
        return NoFall.mc.player.fallDistance > (float)NoFall.mc.player.getMaxFallDistance() && !NoFall.mc.player.onGround() && !NoFall.mc.player.isFallFlying();
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (NoFall.nullCheck()) {
            return;
        }
        for (ItemStack is : NoFall.mc.player.method_5661()) {
            if (is.method_7909() != Items.ELYTRA) continue;
            return;
        }
        if (!this.mode.is(NoFallMode.Packet)) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket packet2 = (ServerboundMovePlayerPacket)packet;
            if (NoFall.mc.player.fallDistance >= (float)this.getDistance.getValue() && !BowBomb.send) {
                ((IPlayerMoveC2SPacket)packet2).setOnGround(true);
            }
        }
    }

    public static enum NoFallMode {
        Packet,
        Grim;

    }
}

