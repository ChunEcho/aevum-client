/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.WebBlock
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.TimerEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Player;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.WebBlock;
import net.minecraft.Packet;
import net.minecraft.BlockState;
import net.minecraft.ServerboundPlayerActionPacket;

public class FastWeb
extends Module {
    public static FastWeb INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Vanilla));
    public final BooleanSetting onlySneak = this.add(new BooleanSetting("OnlySneak", true));
    public final BooleanSetting grim = this.add(new BooleanSetting("Grim", false).setParent());
    public final BooleanSetting abortPacket = this.add(new BooleanSetting("AbortPacket", true, this.grim::isOpen));
    public final SliderSetting xZSlow = this.add(new SliderSetting("XZSpeed", 25.0, 0.0, 100.0, 0.1, () -> this.mode.getValue() == Mode.Custom).setSuffix("%"));
    public final SliderSetting ySlow = this.add(new SliderSetting("YSpeed", 100.0, 0.0, 100.0, 0.1, () -> this.mode.getValue() == Mode.Custom).setSuffix("%"));
    private final SliderSetting fastSpeed = this.add(new SliderSetting("Speed", 3.0, 0.0, 8.0, () -> this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.Strict));
    private boolean work = false;
    private boolean pausedByAntiPiston = false;
    private boolean wasEnabledBeforeAntiPiston = false;

    public FastWeb() {
        super("FastWeb", "So you don't need to keep timer on keybind", Module.Category.Movement);
        this.setChinese("\u8718\u86db\u7f51\u52a0\u901f");
        INSTANCE = this;
    }

    public boolean isPausedByAntiPiston() {
        return this.pausedByAntiPiston;
    }

    public void pauseForAntiPiston() {
        if (this.pausedByAntiPiston) {
            return;
        }
        this.wasEnabledBeforeAntiPiston = this.isOn();
        this.pausedByAntiPiston = true;
        if (this.isOn()) {
            this.disable();
        }
    }

    public void resumeAfterAntiPiston() {
        if (!this.pausedByAntiPiston) {
            return;
        }
        this.pausedByAntiPiston = false;
        if (this.wasEnabledBeforeAntiPiston && this.isOff()) {
            this.enable();
        }
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.pausedByAntiPiston) {
            return;
        }
        this.work = !FastWeb.mc.player.onGround() && (FastWeb.mc.options.keyShift.isDown() || !this.onlySneak.getValue()) && Aevum.PLAYER.isInWeb((Player)FastWeb.mc.player);
        boolean bl = this.work;
        if (this.work && this.mode.is(Mode.Vanilla)) {
            MovementUtil.setMotionY(-this.fastSpeed.getValue());
        }
        if (this.grim.getValue() && (FastWeb.mc.options.keyShift.isDown() || !this.onlySneak.getValue())) {
            for (BlockPos pos : this.getIntersectingWebs()) {
                if (this.abortPacket.getValue()) {
                    mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.ABORT_DESTROY_BLOCK, pos, Direction.DOWN));
                }
                mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
            }
        }
    }

    @EventListener(priority=-100)
    public void onTimer(TimerEvent event) {
        if (this.work && this.mode.getValue() == Mode.Strict) {
            event.set(this.fastSpeed.getValueFloat());
        }
    }

    public List<BlockPos> getIntersectingWebs() {
        int radius = 2;
        ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    BlockState state;
                    BlockPos blockPos = BlockPos.method_49637((double)(FastWeb.mc.player.getX() + (double)x), (double)(FastWeb.mc.player.getY() + (double)y), (double)(FastWeb.mc.player.getZ() + (double)z));
                    if (FastWeb.mc.player.position().method_1022(blockPos.method_46558()) > 1.0 && FastWeb.mc.player.getEyePosition().method_1022(blockPos.method_46558()) > 1.0 || !((state = FastWeb.mc.level.method_8320(blockPos)).method_26204() instanceof WebBlock)) continue;
                    blocks.add(blockPos);
                }
            }
        }
        return blocks;
    }

    public static enum Mode {
        Vanilla,
        Strict,
        Custom,
        Ignore;

    }
}

