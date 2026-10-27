/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffects
 *  net.minecraft.Player
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.Mth
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.SprintEvent;
import dev.aevum.api.events.impl.TickEvent;
import dev.aevum.api.events.impl.TickMovementEvent;
import dev.aevum.api.events.impl.UpdateRotateEvent;
import dev.aevum.api.utils.path.BaritoneUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.movement.HoleSnap;
import dev.aevum.mod.modules.impl.player.Freecam;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import net.minecraft.MobEffects;
import net.minecraft.Player;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.Mth;

public class Sprint
extends Module {
    public static Sprint INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Legit));
    public final BooleanSetting inWaterPause = this.add(new BooleanSetting("InWaterPause", true));
    public final BooleanSetting inWebPause = this.add(new BooleanSetting("InWebPause", true));
    public final BooleanSetting sneakingPause = this.add(new BooleanSetting("SneakingPause", false));
    public final BooleanSetting blindnessPause = this.add(new BooleanSetting("BlindnessPause", false));
    public final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", false));
    public final BooleanSetting lagPause = this.add(new BooleanSetting("LagPause", true));
    boolean pause = false;

    public Sprint() {
        super("Sprint", "Permanently keeps player in sprinting mode.", Module.Category.Movement);
        this.setChinese("\u5f3a\u5236\u75be\u8dd1");
        INSTANCE = this;
    }

    public static float getSprintYaw(float yaw) {
        if (Sprint.mc.options.keyUp.isDown() && !Sprint.mc.options.keyDown.isDown()) {
            if (Sprint.mc.options.keyLeft.isDown() && !Sprint.mc.options.keyRight.isDown()) {
                yaw -= 45.0f;
            } else if (Sprint.mc.options.keyRight.isDown() && !Sprint.mc.options.keyLeft.isDown()) {
                yaw += 45.0f;
            }
        } else if (Sprint.mc.options.keyDown.isDown() && !Sprint.mc.options.keyUp.isDown()) {
            yaw += 180.0f;
            if (Sprint.mc.options.keyLeft.isDown() && !Sprint.mc.options.keyRight.isDown()) {
                yaw += 45.0f;
            } else if (Sprint.mc.options.keyRight.isDown() && !Sprint.mc.options.keyLeft.isDown()) {
                yaw -= 45.0f;
            }
        } else if (Sprint.mc.options.keyLeft.isDown() && !Sprint.mc.options.keyRight.isDown()) {
            yaw -= 90.0f;
        } else if (Sprint.mc.options.keyRight.isDown() && !Sprint.mc.options.keyLeft.isDown()) {
            yaw += 90.0f;
        }
        return Mth.method_15393((float)yaw);
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (this.lagPause.getValue() && event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.pause = true;
        }
    }

    public boolean inWater() {
        return this.inWaterPause.getValue() && Sprint.mc.player.isInLiquid();
    }

    @EventListener
    public void onMove(TickMovementEvent event) {
        if (BaritoneUtil.isPathing()) {
            return;
        }
        if (this.inWater()) {
            return;
        }
        if (this.mode.getValue() == Mode.PressKey) {
            Sprint.mc.options.keySprint.method_23481(true);
        } else {
            Sprint.mc.player.method_5728(this.shouldSprint());
        }
    }

    @EventListener
    public void tick(TickEvent event) {
        if (event.isPost()) {
            this.pause = false;
        }
    }

    @EventListener
    public void sprint(SprintEvent event) {
        if (BaritoneUtil.isPathing() || this.mode.is(Mode.PressKey)) {
            return;
        }
        if (this.inWater()) {
            return;
        }
        event.cancel();
        event.setSprint(this.shouldSprint());
    }

    private boolean shouldSprint() {
        if (!(Sprint.mc.player.getFoodData().method_7586() <= 6 && !Sprint.mc.player.method_7337() || !MovementUtil.isMoving() || this.pause || Sprint.mc.player.isShiftKeyDown() && this.sneakingPause.getValue() || Aevum.PLAYER.isInWeb((Player)Sprint.mc.player) && this.inWebPause.getValue() || Sprint.mc.player.isUsingItem() && this.usingPause.getValue() || Sprint.mc.player.isHandsBusy() || Sprint.mc.player.method_6059(MobEffects.BLINDNESS) && this.blindnessPause.getValue())) {
            switch (this.mode.getValue().ordinal()) {
                case 1: {
                    if (AntiCheat.INSTANCE.movementSync()) {
                        return Sprint.mc.player.input.forwardImpulse > 0.0f;
                    }
                    return HoleSnap.INSTANCE.isOn() || Sprint.mc.options.keyUp.isDown() && Mth.method_15356((float)Sprint.mc.player.getYRot(), (float)Aevum.ROTATION.rotationYaw) < 40.0f;
                }
                case 3: {
                    if (AntiCheat.INSTANCE.movementSync()) {
                        return Sprint.mc.player.input.forwardImpulse > 0.0f;
                    }
                    return HoleSnap.INSTANCE.isOn() || Mth.method_15356((float)Sprint.getSprintYaw(Sprint.mc.player.getYRot()), (float)Aevum.ROTATION.rotationYaw) < 40.0f;
                }
                case 2: {
                    return true;
                }
            }
        }
        return false;
    }

    @EventListener(priority=-100)
    public void rotate(UpdateRotateEvent event) {
        if (BaritoneUtil.isPathing()) {
            return;
        }
        if (!((Sprint.mc.player.getFoodData().method_7586() <= 6 && !Sprint.mc.player.method_7337() || !MovementUtil.isMoving() || Freecam.INSTANCE.isOn() || Sprint.mc.player.isFallFlying() || Aevum.PLAYER.isInWeb((Player)Sprint.mc.player) && this.inWebPause.getValue() || Sprint.mc.player.isShiftKeyDown() && this.sneakingPause.getValue() || Sprint.mc.player.isHandsBusy() || Sprint.mc.player.isUsingItem() && this.usingPause.getValue() || Sprint.mc.player.isInLiquid() || !Freecam.INSTANCE.isOff() || Sprint.mc.player.method_6059(MobEffects.BLINDNESS)) && this.blindnessPause.getValue() || !this.mode.is(Mode.Rotation) || event.isModified())) {
            event.setYaw(Sprint.getSprintYaw(Sprint.mc.player.getYRot()));
        }
    }

    public static enum Mode {
        PressKey,
        Legit,
        Rage,
        Rotation;

    }
}

