/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ClientboundPlayerPositionPacket
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.MoveEvent;
import dev.aevum.api.events.impl.MovedEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.ClientboundPlayerPositionPacket;

public class LongJump
extends Module {
    public static LongJump INSTANCE;
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Strafe));
    private final SliderSetting strafeSpeed = this.add(new SliderSetting("Speed", 0.2873, 0.0, 1.0, 1.0E-4, () -> this.mode.is(Mode.Strafe)));
    private final BooleanSetting jump = this.add(new BooleanSetting("Jump", true, () -> this.mode.is(Mode.Strafe)));
    private final SliderSetting lagTime = this.add(new SliderSetting("LagTime", 500.0, 0.0, 1000.0, 1.0, () -> this.mode.is(Mode.Strafe)));
    private final BooleanSetting airStop = this.add(new BooleanSetting("AirStop", false));
    private final Timer lagTimer = new Timer();
    private boolean stop;
    private double speed;
    private double distance;
    private int stage;
    private boolean boost;

    public LongJump() {
        super("LongJump", Module.Category.Movement);
        this.setChinese("\u957f\u8df3");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (LongJump.mc.player != null) {
            this.speed = MovementUtil.getSpeed(false);
            this.distance = MovementUtil.getDistance2D();
        }
        this.stage = 4;
    }

    @EventListener(priority=100)
    public void invoke(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.lagTimer.reset();
            this.resetStrafe();
        }
    }

    @EventListener
    public void onMove(MovedEvent event) {
        if (!Module.nullCheck()) {
            double dx = LongJump.mc.player.getX() - LongJump.mc.player.xo;
            double dz = LongJump.mc.player.getZ() - LongJump.mc.player.zo;
            this.distance = Math.sqrt(dx * dx + dz * dz);
        }
    }

    @EventListener
    public void invoke(MoveEvent event) {
        if (!MovementUtil.isMoving() && this.airStop.getValue()) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
        }
        if (!(LongJump.mc.player.isHandsBusy() || LongJump.mc.player.isSuppressingSlidingDownLadder() || LongJump.mc.player.getAbilities().flying || LongJump.mc.player.isFallFlying() || !MovementUtil.isMoving())) {
            if (this.mode.is(Mode.Strafe)) {
                if (this.stop) {
                    this.stop = false;
                } else if (this.lagTimer.passed(this.lagTime.getValueInt())) {
                    if (this.stage == 1) {
                        this.speed = 1.35 * MovementUtil.getSpeed(false, this.strafeSpeed.getValue()) - 0.01;
                    } else if (this.stage != 2 || !LongJump.mc.player.onGround() || !LongJump.mc.options.keyJump.isDown() && !this.jump.getValue()) {
                        if (this.stage == 3) {
                            this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(false, this.strafeSpeed.getValue()));
                            this.boost = !this.boost;
                        } else {
                            if ((BlockUtil.canCollide(null, LongJump.mc.player.method_5829().method_989(0.0, MovementUtil.getMotionY(), 0.0)) || LongJump.mc.player.minorHorizontalCollision) && this.stage > 0) {
                                this.stage = 1;
                            }
                            this.speed = this.distance - this.distance / 159.0;
                        }
                    } else {
                        double yMotion = 0.3999 + MovementUtil.getJumpSpeed();
                        MovementUtil.setMotionY(yMotion);
                        event.setY(yMotion);
                        this.speed *= this.boost ? 1.6835 : 1.395;
                    }
                    this.speed = Math.min(this.speed, 10.0);
                    this.speed = Math.max(this.speed, MovementUtil.getSpeed(false, this.strafeSpeed.getValue()));
                    double n = LongJump.mc.player.input.forwardImpulse;
                    double n2 = LongJump.mc.player.input.leftImpulse;
                    double n3 = LongJump.mc.player.getYRot();
                    if (n == 0.0 && n2 == 0.0) {
                        event.setX(0.0);
                        event.setZ(0.0);
                    } else if (n != 0.0 && n2 != 0.0) {
                        n *= Math.sin(0.7853981633974483);
                        n2 *= Math.cos(0.7853981633974483);
                    }
                    event.setX((n * this.speed * -Math.sin(Math.toRadians(n3)) + n2 * this.speed * Math.cos(Math.toRadians(n3))) * 0.99);
                    event.setZ((n * this.speed * Math.cos(Math.toRadians(n3)) - n2 * this.speed * -Math.sin(Math.toRadians(n3))) * 0.99);
                    ++this.stage;
                }
            }
        } else {
            this.resetStrafe();
            this.stop = true;
        }
    }

    public void resetStrafe() {
        this.stage = 4;
        this.speed = 0.0;
        this.distance = 0.0;
    }

    public static enum Mode {
        Strafe;

    }
}

