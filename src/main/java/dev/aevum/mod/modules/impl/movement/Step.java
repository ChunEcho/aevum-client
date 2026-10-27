/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.Attributes
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.TickEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.path.BaritoneUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.SelfTrap;
import dev.aevum.mod.modules.impl.combat.Surround;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Player;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.Attributes;

public class Step
extends Module {
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Vanilla));
    private final SliderSetting height = this.add(new SliderSetting("Height", 1.0, 0.0, 5.0, 0.5));
    private final BooleanSetting useTimer = this.add(new BooleanSetting("Timer", true, () -> this.mode.getValue() == Mode.OldNCP || this.mode.getValue() == Mode.NCP));
    private final BooleanSetting fast = this.add(new BooleanSetting("Fast", true, () -> this.mode.getValue() == Mode.NCP && this.useTimer.getValue()));
    private final BooleanSetting onlyMoving = this.add(new BooleanSetting("OnlyMoving", true));
    private final BooleanSetting surroundPause = this.add(new BooleanSetting("SurroundPause", true));
    private final BooleanSetting inWebPause = this.add(new BooleanSetting("InWebPause", true));
    private final BooleanSetting inBlockPause = this.add(new BooleanSetting("InBlockPause", true));
    private final BooleanSetting sneakingPause = this.add(new BooleanSetting("SneakingPause", true));
    private final BooleanSetting pathingPause = this.add(new BooleanSetting("PathingPause", true));
    boolean timer;
    int packets = 0;

    public Step() {
        super("Step", "Steps up blocks.", Module.Category.Movement);
        this.setChinese("\u6b65\u884c\u8f85\u52a9");
    }

    public static void setStepHeight(float v) {
        Step.mc.player.method_5996(Attributes.STEP_HEIGHT).method_6192((double)v);
    }

    @Override
    public void onDisable() {
        if (Step.nullCheck()) {
            return;
        }
        Step.setStepHeight(0.6f);
        Aevum.TIMER.reset();
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.pathingPause.getValue() && BaritoneUtil.isActive() || this.sneakingPause.getValue() && Step.mc.player.isShiftKeyDown() || this.inBlockPause.getValue() && EntityUtil.isInsideBlock() || Step.mc.player.isInLava() || Step.mc.player.isInWater() || this.inWebPause.getValue() && Aevum.PLAYER.isInWeb((Player)Step.mc.player) || !Step.mc.player.onGround() || this.onlyMoving.getValue() && !MovementUtil.isMoving() || this.surroundPause.getValue() && (Surround.INSTANCE.isOn() || SelfTrap.INSTANCE.isOn())) {
            Step.setStepHeight(0.6f);
            return;
        }
        Step.setStepHeight(this.height.getValueFloat());
    }

    @EventListener
    public void onStep(TickEvent event) {
        if (event.isPost()) {
            --this.packets;
            return;
        }
        if (this.timer && this.packets <= 0) {
            Aevum.TIMER.reset();
            this.timer = false;
        }
        boolean strict = this.mode.getValue() == Mode.NCP;
        boolean bl = strict;
        if (this.mode.getValue().equals((Object)Mode.OldNCP) || strict) {
            double stepHeight = Step.mc.player.getY() - Step.mc.player.yo;
            if (stepHeight <= 0.75 || stepHeight > this.height.getValue()) {
                return;
            }
            double[] offsets = this.getOffset(stepHeight);
            if (offsets != null && offsets.length > 1) {
                if (this.useTimer.getValue()) {
                    Aevum.TIMER.set((float)this.getTimer(stepHeight));
                    this.timer = true;
                    this.packets = 2;
                }
                for (double offset : offsets) {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Step.mc.player.xo, Step.mc.player.yo + offset, Step.mc.player.zo, false));
                }
            }
        }
    }

    public double getTimer(double height) {
        if (height > 0.6 && height <= 1.0) {
            if (!this.fast.getValue() && this.mode.getValue() == Mode.NCP) {
                return 0.3333333333333333;
            }
            return 0.5;
        }
        double[] offsets = this.getOffset(height);
        if (offsets == null) {
            return 1.0;
        }
        return 1.0 / (double)offsets.length;
    }

    public double[] getOffset(double height) {
        boolean strict = this.mode.getValue() == Mode.NCP;
        boolean bl = strict;
        if (height == 0.75) {
            if (strict) {
                return new double[]{0.42, 0.753, 0.75};
            }
            return new double[]{0.42, 0.753};
        }
        if (height == 0.8125) {
            if (strict) {
                return new double[]{0.39, 0.7, 0.8125};
            }
            return new double[]{0.39, 0.7};
        }
        if (height == 0.875) {
            if (strict) {
                return new double[]{0.39, 0.7, 0.875};
            }
            return new double[]{0.39, 0.7};
        }
        if (height == 1.0) {
            if (strict) {
                return new double[]{0.42, 0.753, 1.0};
            }
            return new double[]{0.42, 0.753};
        }
        if (height == 1.5) {
            return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
        }
        if (height == 2.0) {
            return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        }
        if (height == 2.5) {
            return new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
        }
        return null;
    }

    public static enum Mode {
        Vanilla,
        OldNCP,
        NCP;

    }
}

