/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundContainerClickPacket
 *  net.minecraft.ServerboundContainerClosePacket
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 *  net.minecraft.ServerboundPlayerCommandPacket
 *  net.minecraft.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.ServerboundSetCarriedItemPacket
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.KeyMapping
 *  net.minecraft.InputConstants
 *  net.minecraft.ChatScreen
 *  net.minecraft.DataComponents
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.InteractItemEvent;
import dev.aevum.api.events.impl.KeyboardInputEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.AutoWalk;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.impl.movement.Sprint;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Packet;
import net.minecraft.ServerboundContainerClickPacket;
import net.minecraft.ServerboundContainerClosePacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundPlayerActionPacket;
import net.minecraft.ServerboundPlayerCommandPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.KeyMapping;
import net.minecraft.InputConstants;
import net.minecraft.ChatScreen;
import net.minecraft.DataComponents;

public class NoSlow
extends Module {
    public static NoSlow INSTANCE;
    final Queue<ServerboundContainerClickPacket> storedClicks = new LinkedList<ServerboundContainerClickPacket>();
    final AtomicBoolean pause = new AtomicBoolean();
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Vanilla));
    private final BooleanSetting soulSand = this.add(new BooleanSetting("SoulSand", true));
    private final BooleanSetting sneak = this.add(new BooleanSetting("Sneak", false));
    private final BooleanSetting climb = this.add(new BooleanSetting("Climb", false));
    private final BooleanSetting gui = this.add(new BooleanSetting("Gui", true));
    private final BooleanSetting allowSneak = this.add(new BooleanSetting("AllowSneak", false, this.gui::getValue));
    private final EnumSetting<Bypass> clickBypass = this.add(new EnumSetting<Bypass>("GuiMoveBypass", Bypass.None));
    boolean using = false;
    int delay = 0;

    public NoSlow() {
        super("NoSlow", Module.Category.Movement);
        this.setChinese("\u65e0\u51cf\u901f");
        INSTANCE = this;
    }

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.using = NoSlow.mc.player.isUsingItem();
        --this.delay;
        if (this.using) {
            this.delay = 2;
        }
        if (this.using && !NoSlow.mc.player.isHandsBusy() && !NoSlow.mc.player.isFallFlying()) {
            switch (this.mode.getValue().ordinal()) {
                case 1: {
                    mc.getConnection().method_52787((Packet)new ServerboundSetCarriedItemPacket(NoSlow.mc.player.getInventory().selected));
                    break;
                }
                case 2: {
                    if (NoSlow.mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                        NoSlow.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                        break;
                    }
                    NoSlow.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                    break;
                }
                case 3: {
                    NoSlow.mc.gameMode.method_2906(NoSlow.mc.player.containerMenu.containerId, 1, 0, ClickType.PICKUP, (Player)NoSlow.mc.player);
                    if (NoSlow.mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
                        NoSlow.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                        break;
                    }
                    NoSlow.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                }
            }
        }
        if (this.gui.getValue() && !(NoSlow.mc.screen instanceof ChatScreen)) {
            for (KeyMapping k : new KeyMapping[]{NoSlow.mc.options.keyDown, NoSlow.mc.options.keyLeft, NoSlow.mc.options.keyRight}) {
                k.method_23481(InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)k.saveString()).getValue()));
            }
            NoSlow.mc.options.keyJump.method_23481(ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce) && ElytraFly.INSTANCE.autoJump.getValue() || InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keyJump.saveString()).getValue()));
            NoSlow.mc.options.keyUp.method_23481(AutoWalk.INSTANCE.forward() || InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keyUp.saveString()).getValue()));
            NoSlow.mc.options.keySprint.method_23481(Sprint.INSTANCE.isOn() && !Sprint.INSTANCE.inWater() || InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keySprint.saveString()).getValue()));
            if (this.allowSneak.getValue()) {
                NoSlow.mc.options.keyShift.method_23481(InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keyShift.saveString()).getValue()));
            }
        }
    }

    @EventListener(priority=100)
    public void keyboard(KeyboardInputEvent event) {
        if (this.sneak.getValue()) {
            event.cancel();
        }
        if (this.gui.getValue() && !(NoSlow.mc.screen instanceof ChatScreen)) {
            for (KeyMapping k : new KeyMapping[]{NoSlow.mc.options.keyDown, NoSlow.mc.options.keyLeft, NoSlow.mc.options.keyRight}) {
                k.method_23481(InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)k.saveString()).getValue()));
            }
            NoSlow.mc.options.keyJump.method_23481(ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.mode.is(ElytraFly.Mode.Bounce) && ElytraFly.INSTANCE.autoJump.getValue() || InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keyJump.saveString()).getValue()));
            NoSlow.mc.options.keyUp.method_23481(AutoWalk.INSTANCE.forward() || InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keyUp.saveString()).getValue()));
            NoSlow.mc.options.keySprint.method_23481(Sprint.INSTANCE.isOn() && !Sprint.INSTANCE.inWater() || InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keySprint.saveString()).getValue()));
            if (this.allowSneak.getValue()) {
                NoSlow.mc.options.keyShift.method_23481(InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)InputConstants.method_15981((String)NoSlow.mc.options.keyShift.saveString()).getValue()));
            }
            NoSlow.mc.player.input.up = NoSlow.mc.options.keyUp.isDown();
            NoSlow.mc.player.input.down = NoSlow.mc.options.keyDown.isDown();
            NoSlow.mc.player.input.left = NoSlow.mc.options.keyLeft.isDown();
            NoSlow.mc.player.input.right = NoSlow.mc.options.keyRight.isDown();
            NoSlow.mc.player.input.forwardImpulse = NoSlow.getMovementMultiplier(NoSlow.mc.player.input.up, NoSlow.mc.player.input.down);
            NoSlow.mc.player.input.leftImpulse = NoSlow.getMovementMultiplier(NoSlow.mc.player.input.left, NoSlow.mc.player.input.right);
            NoSlow.mc.player.input.jumping = NoSlow.mc.options.keyJump.isDown();
            NoSlow.mc.player.input.shiftKeyDown = NoSlow.mc.options.keyShift.isDown();
        }
    }

    @EventListener
    public void onUse(InteractItemEvent event) {
        if (event.isPre()) {
            if (this.delay > 0) {
                NoSlow.mc.rightClickDelay = 0;
                event.cancel();
            } else if (this.mode.is(Mode.GrimPacket) && NoSlow.mc.player != null && NoSlow.mc.player.method_5998(event.hand).method_7909().components().method_57832(DataComponents.FOOD)) {
                NoSlow.mc.gameMode.method_2906(NoSlow.mc.player.containerMenu.containerId, 1, 0, ClickType.PICKUP, (Player)NoSlow.mc.player);
            }
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send e) {
        ServerboundUseItemPacket packet;
        Packet<?> packet2;
        if (NoSlow.nullCheck()) {
            return;
        }
        if (this.mode.is(Mode.Drop) && (packet2 = e.getPacket()) instanceof ServerboundUseItemPacket && (packet = (ServerboundUseItemPacket)packet2).getHand() == InteractionHand.MAIN_HAND && NoSlow.mc.player.getMainHandItem().method_7909().components().method_57832(DataComponents.FOOD)) {
            mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.DROP_ITEM, BlockPos.ZERO, Direction.DOWN));
            return;
        }
        if (!MovementUtil.isMoving() || this.pause.get()) {
            return;
        }
        packet2 = e.getPacket();
        if (packet2 instanceof ServerboundContainerClickPacket) {
            ServerboundContainerClickPacket click = (ServerboundContainerClickPacket)packet2;
            switch (this.clickBypass.getValue().ordinal()) {
                case 3: {
                    if (click.getClickType() == ClickType.PICKUP || click.getClickType() == ClickType.PICKUP_ALL) break;
                    mc.getConnection().method_52787((Packet)new ServerboundContainerClosePacket(0));
                    break;
                }
                case 1: {
                    if (!NoSlow.mc.player.onGround() || NoSlow.mc.level.getBlockCollisions((Entity)NoSlow.mc.player, NoSlow.mc.player.method_5829().method_989(0.0, 0.0656, 0.0)).iterator().hasNext()) break;
                    if (NoSlow.mc.player.isSprinting()) {
                        mc.getConnection().method_52787((Packet)new ServerboundPlayerCommandPacket((Entity)NoSlow.mc.player, ServerboundPlayerCommandPacket.ServerboundPlayerCommandPacket$Action.STOP_SPRINTING));
                    }
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(NoSlow.mc.player.getX(), NoSlow.mc.player.getY() + 0.0656, NoSlow.mc.player.getZ(), false));
                    break;
                }
                case 2: {
                    if (!NoSlow.mc.player.onGround() || NoSlow.mc.level.getBlockCollisions((Entity)NoSlow.mc.player, NoSlow.mc.player.method_5829().method_989(0.0, 2.71875E-7, 0.0)).iterator().hasNext()) break;
                    if (NoSlow.mc.player.isSprinting()) {
                        mc.getConnection().method_52787((Packet)new ServerboundPlayerCommandPacket((Entity)NoSlow.mc.player, ServerboundPlayerCommandPacket.ServerboundPlayerCommandPacket$Action.STOP_SPRINTING));
                    }
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(NoSlow.mc.player.getX(), NoSlow.mc.player.getY() + 2.71875E-7, NoSlow.mc.player.getZ(), false));
                    break;
                }
                case 4: {
                    this.storedClicks.add(click);
                    e.cancel();
                }
            }
        }
        if (e.getPacket() instanceof ServerboundContainerClosePacket && this.clickBypass.is(Bypass.Delay)) {
            this.pause.set(true);
            while (!this.storedClicks.isEmpty()) {
                mc.getConnection().method_52787((Packet)this.storedClicks.poll());
            }
            this.pause.set(false);
        }
    }

    @EventListener
    public void onPacketSendPost(PacketEvent.Sent e) {
        if (e.getPacket() instanceof ServerboundContainerClickPacket && NoSlow.mc.player.isSprinting() && this.clickBypass.is(Bypass.NCP)) {
            mc.getConnection().method_52787((Packet)new ServerboundPlayerCommandPacket((Entity)NoSlow.mc.player, ServerboundPlayerCommandPacket.ServerboundPlayerCommandPacket$Action.START_SPRINTING));
        }
    }

    public boolean noSlow() {
        return this.isOn() && this.mode.getValue() != Mode.None && (this.mode.getValue() != Mode.Drop && this.mode.getValue() != Mode.GrimPacket || this.using);
    }

    public boolean soulSand() {
        return this.isOn() && this.soulSand.getValue();
    }

    public boolean climb() {
        return this.isOn() && this.climb.getValue();
    }

    public static enum Mode {
        Vanilla,
        NCP,
        Grim,
        GrimPacket,
        Drop,
        None;

    }

    private static enum Bypass {
        None,
        NCP,
        NCP2,
        Grim,
        Delay;

    }
}

