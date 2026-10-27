/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundAcceptTeleportationPacket
 *  net.minecraft.ServerboundClientCommandPacket
 *  net.minecraft.ServerboundContainerClickPacket
 *  net.minecraft.ServerboundContainerClosePacket
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.ServerboundMovePlayerPacket$StatusOnly
 *  net.minecraft.ServerboundMoveVehiclePacket
 *  net.minecraft.ServerboundPickItemPacket
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerCommandPacket
 *  net.minecraft.ServerboundSetCarriedItemPacket
 *  net.minecraft.ServerboundSwingPacket
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ServerboundPongPacket
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.EntityVelocityUpdateEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.asm.accessors.ICloseHandledScreenC2SPacket;
import dev.aevum.asm.accessors.ICommonPongC2SPacket;
import dev.aevum.asm.accessors.IHandSwingC2SPacket;
import dev.aevum.asm.accessors.IPlayerActionC2SPacket;
import dev.aevum.asm.accessors.IPlayerInteractBlockC2SPacket;
import dev.aevum.asm.accessors.IPlayerInteractItemC2SPacket;
import dev.aevum.asm.accessors.IUpdateSelectedSlotC2SPacket;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.Packet;
import net.minecraft.ServerboundAcceptTeleportationPacket;
import net.minecraft.ServerboundClientCommandPacket;
import net.minecraft.ServerboundContainerClickPacket;
import net.minecraft.ServerboundContainerClosePacket;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundMoveVehiclePacket;
import net.minecraft.ServerboundPickItemPacket;
import net.minecraft.ServerboundPlayerActionPacket;
import net.minecraft.ServerboundPlayerCommandPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;
import net.minecraft.ServerboundSwingPacket;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.BlockHitResult;
import net.minecraft.ServerboundPongPacket;

public class PacketLogger
extends Module {
    public static PacketLogger INSTANCE;
    private final BooleanSetting moveFull = this.add(new BooleanSetting("MoveFull", true));
    private final BooleanSetting movePos = this.add(new BooleanSetting("MovePosition", true));
    private final BooleanSetting moveLook = this.add(new BooleanSetting("MoveLook", true));
    private final BooleanSetting moveGround = this.add(new BooleanSetting("MoveGround", true));
    private final BooleanSetting vehicleMove = this.add(new BooleanSetting("VehicleMove", true));
    private final BooleanSetting playerAction = this.add(new BooleanSetting("PlayerAction", true));
    private final BooleanSetting updateSlot = this.add(new BooleanSetting("UpdateSlot", true));
    private final BooleanSetting handSwing = this.add(new BooleanSetting("HandSwing", true));
    private final BooleanSetting pong = this.add(new BooleanSetting("Pong", true));
    private final BooleanSetting interactEntity = this.add(new BooleanSetting("InteractEntity", true));
    private final BooleanSetting interactBlock = this.add(new BooleanSetting("InteractBlock", true));
    private final BooleanSetting interactItem = this.add(new BooleanSetting("InteractItem", true));
    private final BooleanSetting closeScreen = this.add(new BooleanSetting("CloseScreen", true));
    private final BooleanSetting command = this.add(new BooleanSetting("ClientCommand", true));
    private final BooleanSetting status = this.add(new BooleanSetting("ClientStatus", true));
    private final BooleanSetting clickSlot = this.add(new BooleanSetting("ClickSlot", true));
    private final BooleanSetting pickInventory = this.add(new BooleanSetting("PickInventory", true));
    private final BooleanSetting teleportConfirm = this.add(new BooleanSetting("TeleportConfirm", true));
    private final BooleanSetting s2cVelocity = this.add(new BooleanSetting("S2cVelocity", true));

    public PacketLogger() {
        super("PacketLogger", Module.Category.Misc);
        this.setChinese("\u6570\u636e\u5305\u8bb0\u5f55");
        INSTANCE = this;
    }

    private void logPacket(String msg, Object ... args) {
        String s = String.format(msg, args);
        CommandManager.sendMessage(s);
    }

    @EventListener(priority=999999)
    public void velocity(EntityVelocityUpdateEvent event) {
        if (this.s2cVelocity.getValue() && event.getEntity() == PacketLogger.mc.player) {
            this.logPacket("S2C Velocity, x: %s, y: %s, z: %s, isExplosion: %s", event.getX(), event.getY(), event.getZ(), event.isExplosion());
        }
    }

    @EventListener(priority=-999999)
    public void onPacketSend(PacketEvent.Send event) {
        Object accessor;
        StringBuilder builder;
        ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot packet;
        Packet<?> rawPacket = event.getPacket();
        if (rawPacket instanceof ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot) {
            packet = (ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot)rawPacket;
            if (this.moveFull.getValue()) {
                builder = new StringBuilder();
                builder.append("PlayerMove Full - ");
                if (packet.hasPosition()) {
                    builder.append("x: ").append(packet.method_12269(0.0)).append(", y: ").append(packet.method_12268(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
                }
                if (packet.hasRotation()) {
                    builder.append("yaw: ").append(packet.method_12271(0.0f)).append(", pitch: ").append(packet.method_12270(0.0f)).append(" ");
                }
                builder.append(" onground: ").append(packet.method_12273());
                this.logPacket(builder.toString(), new Object[0]);
            }
        }
        if (rawPacket instanceof ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos) {
            packet = (ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos)rawPacket;
            if (this.movePos.getValue()) {
                builder = new StringBuilder();
                builder.append("PlayerMove PosGround - ");
                if (packet.hasPosition()) {
                    builder.append("x: ").append(packet.method_12269(0.0)).append(", y: ").append(packet.method_12268(0.0)).append(", z: ").append(packet.getZ(0.0)).append(" ");
                }
                builder.append(" onground: ").append(packet.method_12273());
                this.logPacket(builder.toString(), new Object[0]);
            }
        }
        if (rawPacket instanceof ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot) {
            packet = (ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot)rawPacket;
            if (this.moveLook.getValue()) {
                builder = new StringBuilder();
                builder.append("PlayerMove LookGround - ");
                if (packet.hasRotation()) {
                    builder.append("yaw: ").append(packet.method_12271(0.0f)).append(", pitch: ").append(packet.method_12270(0.0f)).append(" ");
                }
                builder.append(" onground: ").append(packet.method_12273());
                this.logPacket(builder.toString(), new Object[0]);
            }
        }
        if (rawPacket instanceof ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly) {
            packet = (ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly)rawPacket;
            if (this.moveGround.getValue()) {
                this.logPacket("PlayerMove Ground - onground: " + packet.method_12273(), new Object[0]);
            }
        }
        if (rawPacket instanceof ServerboundMoveVehiclePacket) {
            packet = (ServerboundMoveVehiclePacket)rawPacket;
            if (this.vehicleMove.getValue()) {
                this.logPacket("VehicleMove - x: %s, y: %s, z: %s, yaw: %s, pitch: %s", packet.getX(), packet.getY(), packet.getZ(), Float.valueOf(packet.getYRot()), Float.valueOf(packet.getXRot()));
            }
        }
        if (rawPacket instanceof ServerboundPlayerActionPacket) {
            packet = (ServerboundPlayerActionPacket)rawPacket;
            accessor = (IPlayerActionC2SPacket)packet;
            if (this.playerAction.getValue() && accessor.getDirection() != null) {
                this.logPacket("PlayerAction - action: %s, direction: %s, pos: %s", accessor.getAction().name(), accessor.getDirection().name(), accessor.getPos().toShortString());
            }
        }
        if (rawPacket instanceof ServerboundSetCarriedItemPacket) {
            packet = (ServerboundSetCarriedItemPacket)rawPacket;
            if (this.updateSlot.getValue()) {
                this.logPacket("UpdateSlot - slot: %d", ((IUpdateSelectedSlotC2SPacket)packet).getSelectedSlot());
            }
        }
        if (rawPacket instanceof ServerboundSwingPacket) {
            packet = (ServerboundSwingPacket)rawPacket;
            if (this.handSwing.getValue()) {
                this.logPacket("HandSwing - hand: %s", ((IHandSwingC2SPacket)packet).getHand().name());
            }
        }
        if (rawPacket instanceof ServerboundPongPacket) {
            packet = (ServerboundPongPacket)rawPacket;
            if (this.pong.getValue()) {
                this.logPacket("Pong - %d", ((ICommonPongC2SPacket)packet).getParameter());
            }
        }
        if (rawPacket instanceof ServerboundInteractPacket) {
            packet = (ServerboundInteractPacket)rawPacket;
            if (this.interactEntity.getValue()) {
                this.logPacket("InteractEntity - Entity: %s, id: %s", Criticals.getEntity((ServerboundInteractPacket)packet).method_5477().getString(), Criticals.getEntity((ServerboundInteractPacket)packet).method_5628());
            }
        }
        if (rawPacket instanceof ServerboundUseItemOnPacket) {
            packet = (ServerboundUseItemOnPacket)rawPacket;
            accessor = (IPlayerInteractBlockC2SPacket)packet;
            if (this.interactBlock.getValue()) {
                BlockHitResult blockHitResult = accessor.getBlockHitResult();
                this.logPacket("InteractBlock - pos: %s, dir: %s, hand: %s", blockHitResult.method_17777().toShortString(), blockHitResult.getDirection().name(), accessor.getHand().name());
            }
        }
        if (rawPacket instanceof ServerboundUseItemPacket) {
            packet = (ServerboundUseItemPacket)rawPacket;
            if (this.interactItem.getValue()) {
                this.logPacket("InteractItem - hand: %s", ((IPlayerInteractItemC2SPacket)packet).getHand().name());
            }
        }
        if (rawPacket instanceof ServerboundContainerClosePacket) {
            packet = (ServerboundContainerClosePacket)rawPacket;
            if (this.closeScreen.getValue()) {
                this.logPacket("CloseScreen - id: %s", ((ICloseHandledScreenC2SPacket)packet).getSyncId());
            }
        }
        if (rawPacket instanceof ServerboundPlayerCommandPacket) {
            packet = (ServerboundPlayerCommandPacket)rawPacket;
            if (this.command.getValue()) {
                this.logPacket("ClientCommand - mode: %s", packet.getAction().name());
            }
        }
        if (rawPacket instanceof ServerboundClientCommandPacket) {
            packet = (ServerboundClientCommandPacket)rawPacket;
            if (this.status.getValue()) {
                this.logPacket("ClientStatus - mode: %s", packet.getAction().name());
            }
        }
        if (rawPacket instanceof ServerboundContainerClickPacket) {
            packet = (ServerboundContainerClickPacket)rawPacket;
            if (this.clickSlot.getValue()) {
                this.logPacket("ClickSlot - type: %s, slot: %s, button: %s, id: %s", packet.getClickType().name(), packet.getSlotNum(), packet.getButtonNum(), packet.getContainerId());
            }
        }
        if (rawPacket instanceof ServerboundPickItemPacket) {
            packet = (ServerboundPickItemPacket)rawPacket;
            if (this.pickInventory.getValue()) {
                this.logPacket("PickInventory - slot: %s", packet.getSlot());
            }
        }
        if (rawPacket instanceof ServerboundAcceptTeleportationPacket) {
            packet = (ServerboundAcceptTeleportationPacket)rawPacket;
            if (this.teleportConfirm.getValue()) {
                this.logPacket("TeleportConfirm - id: %s", packet.getId());
            }
        }
    }
}

