/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;

public class VClip
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Jump));

    public VClip() {
        super("VClip", Module.Category.Movement);
        this.setChinese("\u7eb5\u5411\u7a7f\u5899");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.disable();
        switch (this.mode.getValue().ordinal()) {
            case 1: {
                VClip.mc.player.method_5814(VClip.mc.player.getX(), VClip.mc.player.getY() + 3.0, VClip.mc.player.getZ());
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(VClip.mc.player.getX(), VClip.mc.player.getY(), VClip.mc.player.getZ(), true));
                break;
            }
            case 2: {
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(VClip.mc.player.getX(), VClip.mc.player.getY() + 0.4199999868869781, VClip.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(VClip.mc.player.getX(), VClip.mc.player.getY() + 0.7531999805212017, VClip.mc.player.getZ(), false));
                VClip.mc.player.method_5814(VClip.mc.player.getX(), VClip.mc.player.getY() + 1.0, VClip.mc.player.getZ());
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(VClip.mc.player.getX(), VClip.mc.player.getY(), VClip.mc.player.getZ(), true));
                break;
            }
            case 0: {
                double posX = VClip.mc.player.getX();
                double posY = Math.round(VClip.mc.player.getY());
                double posZ = VClip.mc.player.getZ();
                boolean onGround = VClip.mc.player.onGround();
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(posX, posY, posZ, onGround));
                double halfY = 0.005;
                VClip.mc.player.method_5814(posX, posY -= halfY, posZ);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(posX, posY, posZ, onGround));
                VClip.mc.player.method_5814(posX, posY -= halfY * 300.0, posZ);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(posX, posY, posZ, onGround));
            }
        }
    }

    public static enum Mode {
        Glitch,
        Teleport,
        Jump;

    }
}

