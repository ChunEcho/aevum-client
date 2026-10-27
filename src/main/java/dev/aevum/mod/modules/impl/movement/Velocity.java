/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.FishingHook
 *  net.minecraft.Level
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Direction$AxisDirection
 *  net.minecraft.AABB
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundEntityEventPacket
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.EntityVelocityUpdateEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.TickEvent;
import dev.aevum.api.events.impl.UpdateRotateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.FishingHook;
import net.minecraft.Level;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Packet;
import net.minecraft.ClientboundEntityEventPacket;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundPlayerActionPacket;

public class Velocity
extends Module {
    public static Velocity INSTANCE;
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Plain));
    private final SliderSetting lagPause = this.add(new SliderSetting("LagPause", 50, 0, 500, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)));
    public final BooleanSetting ignorePearlLag = this.add(new BooleanSetting("IgnorePearlLag", true, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)).setParent());
    private final SliderSetting phaseTime = this.add(new SliderSetting("PhaseTime", 250, 0, 1000, () -> (this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)) && this.ignorePearlLag.isOpen()));
    public final BooleanSetting noRotation = this.add(new BooleanSetting("NoRotation", false, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)));
    public final BooleanSetting flagInWall = this.add(new BooleanSetting("FlagInWall", false, () -> this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)).setParent());
    public final BooleanSetting whenPushOutOfBlocks = this.add(new BooleanSetting("WhilePushOut", false, () -> (this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)) && this.flagInWall.isOpen()));
    public final BooleanSetting staticSetting = this.add(new BooleanSetting("Static", false, () -> this.mode.is(Mode.Grim)));
    public final BooleanSetting cancelAll = this.add(new BooleanSetting("CancelAll", false, () -> !this.mode.is(Mode.None)));
    private final SliderSetting horizontal = this.add(new SliderSetting("Horizontal", 0.0, 0.0, 100.0, 1.0, () -> !this.mode.is(Mode.None) && !this.cancelAll.getValue()));
    private final SliderSetting vertical = this.add(new SliderSetting("Vertical", 0.0, 0.0, 100.0, 1.0, () -> !this.mode.is(Mode.None) && !this.cancelAll.getValue()));
    public final BooleanSetting whileLiquid = this.add(new BooleanSetting("WhileLiquid", false));
    public final BooleanSetting whileElytra = this.add(new BooleanSetting("FallFlying", false));
    public final BooleanSetting noClimb = this.add(new BooleanSetting("NoClimb", false));
    public final BooleanSetting waterPush = this.add(new BooleanSetting("NoWaterPush", false));
    public final BooleanSetting entityPush = this.add(new BooleanSetting("NoEntityPush", true));
    public final BooleanSetting blockPush = this.add(new BooleanSetting("NoBlockPush", true));
    public final BooleanSetting fishBob = this.add(new BooleanSetting("NoFishBob", true));
    public final Timer pearlTimer = new Timer();
    private final Timer lagBackTimer = new Timer();
    private boolean flag;
    static boolean pushOutOfBlocks;

    public Velocity() {
        super("Velocity", Module.Category.Movement);
        this.setChinese("\u53cd\u51fb\u9000");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (this.mode.is(Mode.None)) {
            return null;
        }
        return this.mode.getValue().name() + ", " + (String)(this.cancelAll.getValue() ? "Cancel" : this.horizontal.getValueInt() + "%, " + this.vertical.getValueInt() + "%");
    }

    @EventListener
    public void onRotate(UpdateRotateEvent event) {
        if (this.noRotation()) {
            event.setRotation(Aevum.ROTATION.rotationYaw, 89.0f);
        }
    }

    public boolean noRotation() {
        return this.isOn() && (this.mode.is(Mode.Grim) || this.mode.is(Mode.Wall)) && EntityUtil.isInsideBlock() && this.noRotation.getValue();
    }

    @EventListener
    public void onVelocity(EntityVelocityUpdateEvent event) {
        if (Velocity.nullCheck()) {
            return;
        }
        if (event.getEntity() != Velocity.mc.player) {
            return;
        }
        if (this.mode.is(Mode.None)) {
            return;
        }
        if (Velocity.mc.player.isInLiquid() && !this.whileLiquid.getValue()) {
            return;
        }
        if (Velocity.mc.player.isFallFlying() && !this.whileElytra.getValue()) {
            return;
        }
        switch (this.mode.getValue().ordinal()) {
            case 1: {
                if (!this.lagBackTimer.passedMs(this.lagPause.getValue())) {
                    return;
                }
                if (EntityUtil.isInsideBlock() || this.getPos() != null || this.staticSetting.getValue() && MovementUtil.isStatic()) {
                    if (event.getX() == 0.0 && event.getZ() == 0.0) break;
                    this.flag = true;
                    break;
                }
                return;
            }
            case 2: {
                if (!this.lagBackTimer.passedMs(this.lagPause.getValue())) {
                    return;
                }
                if (EntityUtil.isInsideBlock()) {
                    if (event.getX() == 0.0 && event.getZ() == 0.0) break;
                    this.flag = true;
                    break;
                }
                return;
            }
        }
        if (this.cancelAll.getValue()) {
            event.cancel();
        } else {
            double h = (double)this.horizontal.getValueInt() / 100.0;
            double v = (double)this.vertical.getValueInt() / 100.0;
            event.setX(event.getX() * h);
            event.setZ(event.getZ() * h);
            event.setY(event.getY() * v);
        }
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        FishingHook fishHook;
        Entity entity;
        ClientboundEntityEventPacket packet;
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket && (!this.ignorePearlLag.getValue() || this.pearlTimer.passed(this.phaseTime.getValueInt()))) {
            this.lagBackTimer.reset();
        }
        if (Velocity.nullCheck()) {
            return;
        }
        if (Velocity.mc.player.isInLiquid() && !this.whileLiquid.getValue()) {
            return;
        }
        if (this.fishBob.getValue() && event.getPacket() instanceof ClientboundEntityEventPacket && (packet = (ClientboundEntityEventPacket)event.getPacket()).getEventId() == 31 && (entity = packet.method_11469((Level)Velocity.mc.level)) instanceof FishingHook && (fishHook = (FishingHook)entity).getHookedIn() == Velocity.mc.player) {
            event.setCancelled(true);
        }
    }

    @EventListener
    public void onUpdate(TickEvent event) {
        if (Velocity.nullCheck()) {
            return;
        }
        if (event.isPost() || Velocity.mc.player.isInLiquid() && !this.whileLiquid.getValue()) {
            return;
        }
        if (this.flagInWall.getValue()) {
            pushOutOfBlocks = false;
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() - (double)Velocity.mc.player.getBbWidth() * 0.35, Velocity.mc.player.getZ() + (double)Velocity.mc.player.getBbWidth() * 0.35);
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() - (double)Velocity.mc.player.getBbWidth() * 0.35, Velocity.mc.player.getZ() - (double)Velocity.mc.player.getBbWidth() * 0.35);
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() + (double)Velocity.mc.player.getBbWidth() * 0.35, Velocity.mc.player.getZ() - (double)Velocity.mc.player.getBbWidth() * 0.35);
            Velocity.pushOutOfBlocks(Velocity.mc.player.getX() + (double)Velocity.mc.player.getBbWidth() * 0.35, Velocity.mc.player.getZ() + (double)Velocity.mc.player.getBbWidth() * 0.35);
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.flag) {
            if (this.lagBackTimer.passedMs(this.lagPause.getValue()) && (this.flagInWall.getValue() && (!pushOutOfBlocks || this.whenPushOutOfBlocks.getValue()) || !EntityUtil.isInsideBlock())) {
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(Velocity.mc.player.getX(), Velocity.mc.player.getY(), Velocity.mc.player.getZ(), Aevum.ROTATION.rotationYaw, Aevum.ROTATION.rotationPitch, Velocity.mc.player.onGround()));
                BlockPos pos = this.getPos();
                if (pos != null) {
                    mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, pos, Velocity.mc.player.getDirection().getOpposite()));
                }
            }
            this.flag = false;
        }
    }

    public BlockPos getPos() {
        if (Velocity.mc.level.method_8320(Velocity.mc.player.method_24515().method_10074()).method_26204() == Blocks.OBSIDIAN) {
            return Velocity.mc.player.method_24515().method_10074();
        }
        return null;
    }

    private static void pushOutOfBlocks(double x, double z) {
        BlockPos blockPos = BlockPos.method_49637((double)x, (double)Velocity.mc.player.getY(), (double)z);
        if (Velocity.wouldCollideAt(blockPos)) {
            double d = x - (double)blockPos.method_10263();
            double e = z - (double)blockPos.getZ();
            Direction direction = null;
            double f = Double.MAX_VALUE;
            Direction[] directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
            for (Direction direction2 : directions) {
                double g = direction2.getAxis().method_10172(d, 0.0, e);
                double h = direction2.getAxisDirection() == Direction.Direction$AxisDirection.POSITIVE ? 1.0 - g : g;
                double d2 = h;
                if (!(h < f) || Velocity.wouldCollideAt(blockPos.method_10093(direction2))) continue;
                f = h;
                direction = direction2;
            }
            if (direction != null) {
                pushOutOfBlocks = true;
            }
        }
    }

    private static boolean wouldCollideAt(BlockPos pos) {
        AABB box = Velocity.mc.player.method_5829();
        AABB box2 = new AABB((double)pos.method_10263(), box.minY, (double)pos.getZ(), (double)pos.method_10263() + 1.0, box.maxY, (double)pos.getZ() + 1.0).method_1011(1.0E-7);
        return Velocity.mc.player.level().method_39454((Entity)Velocity.mc.player, box2);
    }

    static {
        pushOutOfBlocks = false;
    }

    public static enum Mode {
        Plain,
        Grim,
        Wall,
        None;

    }
}

