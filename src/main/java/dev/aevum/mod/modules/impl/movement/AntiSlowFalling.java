/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffects
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.ClientboundPlayerPositionPacket
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.MobEffects;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.ClientboundPlayerPositionPacket;

public class AntiSlowFalling
extends Module {
    public static AntiSlowFalling INSTANCE;
    private final BooleanSetting bypassSlowFalling = this.add(new BooleanSetting("SlowFallingCheck", false));
    private final SliderSetting y = this.add(new SliderSetting("YMove", 0.01, 0.0, 1.0, 0.01, () -> this.bypassSlowFalling.getValue()));
    private final SliderSetting jumpDelay = this.add(new SliderSetting("JumpTick", 0.0, 0.0, 500.0, 1.0, () -> this.bypassSlowFalling.getValue()));
    private final BooleanSetting excludeWater = this.add(new BooleanSetting("ExcludeWater", true, () -> this.bypassSlowFalling.getValue()));
    private final BooleanSetting noFallFlying = this.add(new BooleanSetting("NoFallFlying", true, () -> this.bypassSlowFalling.getValue()));
    private final BooleanSetting noWeb = this.add(new BooleanSetting("NoWeb", true, () -> this.bypassSlowFalling.getValue()));
    private final BooleanSetting springBack = this.add(new BooleanSetting("SpringBack", false, () -> this.bypassSlowFalling.getValue()));
    private final SliderSetting backDelay = this.add(new SliderSetting("BackDelay", 1.0, 1.0, 20.0, 1.0, () -> this.bypassSlowFalling.getValue() && this.springBack.getValue()));
    private final SliderSetting springBackThreshold = this.add(new SliderSetting("SBThreshold", 2.0, 1.0, 10.0, 1.0, () -> this.bypassSlowFalling.getValue() && this.springBack.getValue()));
    private final SliderSetting springBackWindow = this.add(new SliderSetting("SBWindow", 1.0, 0.5, 5.0, 0.1, () -> this.bypassSlowFalling.getValue() && this.springBack.getValue()));
    private final Timer jumpTimer = new Timer();
    private final Timer springBackPacketTimer = new Timer();
    private final Timer sneakTimer = new Timer();
    private int springBackPacketCount = 0;
    private boolean isSneakingForSpringBack = false;
    private boolean shouldSneakForSpringBack = false;
    private SpringBackState springBackState = SpringBackState.IDLE;
    private int sneakStartTick = 0;

    public AntiSlowFalling() {
        super("AntiSlowFalling", Module.Category.Movement);
        this.setChinese("\u53cd\u7f13\u964d");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (Module.nullCheck() || !this.bypassSlowFalling.getValue()) {
            return;
        }
        this.handleSpringBackState();
        if (!this.jumpTimer.passedMs((long)this.jumpDelay.getValue())) {
            return;
        }
        if (!AntiSlowFalling.mc.player.method_6059(MobEffects.SLOW_FALLING)) {
            return;
        }
        if (AntiSlowFalling.mc.player.isSpectator()) {
            return;
        }
        if (!EntityUtil.isInsideBlock()) {
            return;
        }
        if (this.excludeWater.getValue() && AntiSlowFalling.mc.player.isInWater()) {
            return;
        }
        if (this.noFallFlying.getValue()) {
            boolean wearingElytra = false;
            for (ItemStack is : AntiSlowFalling.mc.player.method_5661()) {
                if (is.method_7909() != Items.ELYTRA) continue;
                wearingElytra = true;
                break;
            }
            if (AntiSlowFalling.mc.player.isFallFlying() || wearingElytra) {
                return;
            }
        }
        if (this.noWeb.getValue()) {
            boolean inWeb;
            boolean bl = inWeb = Aevum.PLAYER.isInWeb((Player)AntiSlowFalling.mc.player) || AntiSlowFalling.mc.level.method_8320(EntityUtil.getPlayerPos(true)).method_26204() == Blocks.COBWEB;
            if (inWeb) {
                return;
            }
        }
        if (AntiSlowFalling.mc.player.onGround() && AntiSlowFalling.mc.player.fallDistance <= 0.0f) {
            MovementUtil.setMotionY(this.y.getValue());
            this.jumpTimer.reset();
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!this.bypassSlowFalling.getValue() || !this.springBack.getValue()) {
            return;
        }
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            this.handlePositionCorrection();
        }
    }

    private void handlePositionCorrection() {
        if (AntiSlowFalling.mc.player == null || AntiSlowFalling.mc.level == null) {
            return;
        }
        if (!EntityUtil.isInsideBlock() || !AntiSlowFalling.mc.player.method_6059(MobEffects.SLOW_FALLING)) {
            this.springBackPacketCount = 0;
            return;
        }
        if (this.excludeWater.getValue() && AntiSlowFalling.mc.player.isInWater()) {
            return;
        }
        if (this.noFallFlying.getValue() && AntiSlowFalling.mc.player.isFallFlying()) {
            return;
        }
        if (this.noWeb.getValue()) {
            boolean inWeb;
            boolean bl = inWeb = Aevum.PLAYER.isInWeb((Player)AntiSlowFalling.mc.player) || AntiSlowFalling.mc.level.method_8320(EntityUtil.getPlayerPos(true)).method_26204() == Blocks.COBWEB;
            if (inWeb) {
                return;
            }
        }
        if (this.springBackPacketCount == 0) {
            this.springBackPacketTimer.reset();
        }
        ++this.springBackPacketCount;
        if (this.springBackPacketCount >= this.springBackThreshold.getValueInt() && !this.springBackPacketTimer.passedMs((long)(this.springBackWindow.getValue() * 1000.0))) {
            this.shouldSneakForSpringBack = true;
            this.springBackPacketCount = 0;
        }
    }

    private void handleSpringBackState() {
        if (!this.bypassSlowFalling.getValue() || !this.springBack.getValue()) {
            this.resetSpringBackState();
            return;
        }
        if (AntiSlowFalling.mc.player == null) {
            this.resetSpringBackState();
            return;
        }
        switch (this.springBackState.ordinal()) {
            case 0: {
                if (!this.shouldSneakForSpringBack) break;
                AntiSlowFalling.mc.options.keyShift.method_23481(true);
                this.isSneakingForSpringBack = true;
                this.sneakStartTick = AntiSlowFalling.mc.player.tickCount;
                this.sneakTimer.reset();
                this.springBackState = SpringBackState.SNEAKING;
                this.shouldSneakForSpringBack = false;
                break;
            }
            case 1: {
                int currentTick = AntiSlowFalling.mc.player.tickCount;
                int ticksSneaking = currentTick - this.sneakStartTick;
                if (!((double)ticksSneaking >= this.backDelay.getValue())) break;
                AntiSlowFalling.mc.options.keyShift.method_23481(false);
                this.isSneakingForSpringBack = false;
                this.springBackState = SpringBackState.RELEASING;
                this.sneakTimer.reset();
                break;
            }
            case 2: {
                if (!this.sneakTimer.passedMs(100.0)) break;
                this.springBackState = SpringBackState.IDLE;
            }
        }
        if (this.springBackPacketCount > 0 && this.springBackPacketTimer.passedMs((long)(this.springBackWindow.getValue() * 1000.0))) {
            this.springBackPacketCount = 0;
        }
    }

    private void resetSpringBackState() {
        if (this.isSneakingForSpringBack) {
            AntiSlowFalling.mc.options.keyShift.method_23481(false);
        }
        this.springBackState = SpringBackState.IDLE;
        this.isSneakingForSpringBack = false;
        this.shouldSneakForSpringBack = false;
        this.springBackPacketCount = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.resetSpringBackState();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.resetSpringBackState();
    }

    private static enum SpringBackState {
        IDLE,
        SNEAKING,
        RELEASING;

    }
}

