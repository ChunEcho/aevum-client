/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.BlockState
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.BlockHitResult
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.MoveEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.TimerEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.Fly;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.HashMap;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.BlockState;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.ClipContext;
import net.minecraft.BlockHitResult;

public class FastFall
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Fast));
    private final BooleanSetting noLag = this.add(new BooleanSetting("NoLag", true, () -> this.mode.getValue() == Mode.Fast));
    private final BooleanSetting useTimerSetting = this.add(new BooleanSetting("UseTimer", false));
    private final SliderSetting timer = this.add(new SliderSetting("Timer", 2.5, 1.0, 8.0, 0.1, this.useTimerSetting::getValue));
    private final BooleanSetting anchor = this.add(new BooleanSetting("Anchor", true));
    private final SliderSetting height = this.add(new SliderSetting("Height", 10.0, 1.0, 20.0, 0.5));
    private final Timer lagTimer = new Timer();
    boolean onGround = false;
    private boolean useTimer;

    public FastFall() {
        super("FastFall", "Miyagi son simulator", Module.Category.Movement);
        this.setChinese("\u5feb\u901f\u5760\u843d");
    }

    @Override
    public void onDisable() {
        this.useTimer = false;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener(priority=-100)
    public void onMove(MoveEvent event) {
        if (FastFall.nullCheck()) {
            return;
        }
        if (FastFall.mc.player.onGround() && this.anchor.getValue() && this.traceDown() != 0 && (double)this.traceDown() <= this.height.getValue() && this.trace()) {
            event.setX(event.getX() * 0.05);
            event.setZ(event.getZ() * 0.05);
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.height.getValue() > 0.0 && (double)this.traceDown() > this.height.getValue() || FastFall.mc.player.isInWall() || FastFall.mc.player.isUnderWater() || FastFall.mc.player.isInLava() || FastFall.mc.player.isSuppressingSlidingDownLadder() || !this.lagTimer.passed(1000L) || FastFall.mc.player.isFallFlying() || Fly.INSTANCE.isOn() || FastFall.nullCheck()) {
            return;
        }
        if (Aevum.PLAYER.isInWeb((Player)FastFall.mc.player)) {
            return;
        }
        if (FastFall.mc.player.onGround() && this.mode.getValue() == Mode.Fast) {
            MovementUtil.setMotionY(MovementUtil.getMotionY() - (double)(this.noLag.getValue() ? 0.62f : 1.0f));
        }
        if (this.useTimerSetting.getValue()) {
            if (!FastFall.mc.player.onGround()) {
                if (this.onGround) {
                    this.useTimer = true;
                }
                if (MovementUtil.getMotionY() >= 0.0) {
                    this.useTimer = false;
                }
                this.onGround = false;
            } else {
                this.useTimer = false;
                MovementUtil.setMotionY(-0.08);
                this.onGround = true;
            }
        } else {
            this.useTimer = false;
        }
    }

    @EventListener
    public void onTimer(TimerEvent event) {
        if (FastFall.nullCheck()) {
            return;
        }
        if (!FastFall.mc.player.onGround() && this.useTimer) {
            event.set(this.timer.getValueFloat());
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (!FastFall.nullCheck() && event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.lagTimer.reset();
        }
    }

    private int traceDown() {
        int y;
        int retval = 0;
        for (int tracey = y = (int)Math.round(FastFall.mc.player.getY()) - 1; tracey >= 0; --tracey) {
            BlockHitResult trace = FastFall.mc.level.method_17742(new ClipContext(FastFall.mc.player.position(), new Vec3(FastFall.mc.player.getX(), (double)tracey, FastFall.mc.player.getZ()), ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.NONE, (Entity)FastFall.mc.player));
            if (trace != null && trace.method_17783() == HitResult.HitResult$Type.BLOCK) {
                return retval;
            }
            ++retval;
        }
        return retval;
    }

    private boolean trace() {
        AABB bbox = FastFall.mc.player.method_5829();
        Vec3 basepos = bbox.method_1005();
        double minX = bbox.minX;
        double minZ = bbox.minZ;
        double maxX = bbox.maxX;
        double maxZ = bbox.maxZ;
        HashMap<Vec3, Vec3> positions = new HashMap<Vec3, Vec3>();
        positions.put(basepos, new Vec3(basepos.x, basepos.y - 1.0, basepos.z));
        positions.put(new Vec3(minX, basepos.y, minZ), new Vec3(minX, basepos.y - 1.0, minZ));
        positions.put(new Vec3(maxX, basepos.y, minZ), new Vec3(maxX, basepos.y - 1.0, minZ));
        positions.put(new Vec3(minX, basepos.y, maxZ), new Vec3(minX, basepos.y - 1.0, maxZ));
        positions.put(new Vec3(maxX, basepos.y, maxZ), new Vec3(maxX, basepos.y - 1.0, maxZ));
        for (Vec3 key : positions.keySet()) {
            ClipContext context = new ClipContext(key, (Vec3)positions.get(key), ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.NONE, (Entity)FastFall.mc.player);
            BlockHitResult result = FastFall.mc.level.method_17742(context);
            if (result == null || result.method_17783() != HitResult.HitResult$Type.BLOCK) continue;
            return false;
        }
        BlockState state = FastFall.mc.level.method_8320((BlockPos)new BlockPosX(FastFall.mc.player.getX(), FastFall.mc.player.getY() - 1.0, FastFall.mc.player.getZ()));
        return state.method_26215();
    }

    private static enum Mode {
        Fast,
        None;

    }
}

