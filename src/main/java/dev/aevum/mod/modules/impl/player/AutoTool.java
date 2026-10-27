/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SoundInstance
 *  net.minecraft.FishingHook
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.AirBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.HitResult
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Holder
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PlaySoundEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.asm.accessors.IMinecraftClient;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.event.KeyEvent;
import net.minecraft.SoundInstance;
import net.minecraft.FishingHook;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.AirBlock;
import net.minecraft.BlockPos;
import net.minecraft.HitResult;
import net.minecraft.BlockHitResult;
import net.minecraft.Holder;

public class AutoTool
extends Module {
    private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
    private final BooleanSetting fish = this.add(new BooleanSetting("Fish", true).setParent());
    private final BooleanSetting autoCast = this.add(new BooleanSetting("AutoCast", true, this.fish::isOpen));
    private final SliderSetting ticksAutoCast = this.add(new SliderSetting("TicksAutoCast", 10, 0, 60, this.fish::isOpen));
    private final SliderSetting ticksCatch = this.add(new SliderSetting("TicksCatch", 6, 0, 60, this.fish::isOpen));
    private final SliderSetting ticksThrow = this.add(new SliderSetting("TicksThrow", 14, 0, 60, this.fish::isOpen));
    private final BooleanSetting splashDetection = this.add(new BooleanSetting("SplashDetection", false, this.fish::isOpen));
    private final SliderSetting splashDetectionRange = this.add(new SliderSetting("DetectionRange", 10, 0, 60, this.fish::isOpen));
    private boolean ticksEnabled;
    private int ticksToRightClick;
    private int ticksData;
    private int autoCastTimer;
    private boolean autoCastEnabled;
    private int autoCastCheckTimer;

    public AutoTool() {
        super("AutoTool", Module.Category.Player);
        this.setChinese("\u81ea\u52a8\u5de5\u5177");
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.autoFish();
        this.autoTool();
    }

    public void autoTool() {
        if (!this.mine.getValue()) {
            return;
        }
        HitResult hitResult = AutoTool.mc.hitResult;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult result = (BlockHitResult)hitResult;
        BlockPos pos = result.method_17777();
        if (AutoTool.mc.level.isEmptyBlock(pos)) {
            return;
        }
        int tool = AutoTool.getTool(pos);
        if (tool != -1 && AutoTool.mc.options.keyAttack.isDown()) {
            AutoTool.mc.player.getInventory().selected = tool;
        }
    }

    @Override
    public void onEnable() {
        this.ticksEnabled = false;
        this.autoCastEnabled = false;
        this.autoCastCheckTimer = 0;
    }

    @EventListener
    private void onPlaySound(PlaySoundEvent event) {
        if (AutoTool.nullCheck()) {
            return;
        }
        if (!this.fish.getValue()) {
            return;
        }
        SoundInstance p = event.sound;
        FishingHook elementCodec = AutoTool.mc.player.fishing;
        if (elementCodec == null) {
            return;
        }
        if (p.method_4775().method_12832().equals("entity.fishing_bobber.splash") && (!this.splashDetection.getValue() || MathUtil.getDistance(elementCodec.getX(), elementCodec.getY(), elementCodec.getZ(), p.method_4784(), p.method_4779(), p.method_4778()) <= this.splashDetectionRange.getValue())) {
            this.ticksEnabled = true;
            this.ticksToRightClick = this.ticksCatch.getValueInt();
            this.ticksData = 0;
        }
    }

    public void autoFish() {
        if (!this.fish.getValue()) {
            return;
        }
        if (this.autoCastCheckTimer <= 0) {
            this.autoCastCheckTimer = 30;
            if (this.autoCast.getValue() && !this.ticksEnabled && !this.autoCastEnabled && AutoTool.mc.player.fishing == null && AutoTool.mc.player.getMainHandItem().method_7909() == Items.FISHING_ROD) {
                this.autoCastTimer = 0;
                this.autoCastEnabled = true;
            }
        } else {
            --this.autoCastCheckTimer;
        }
        if (this.autoCastEnabled) {
            ++this.autoCastTimer;
            if ((double)this.autoCastTimer > this.ticksAutoCast.getValue()) {
                this.autoCastEnabled = false;
                ((IMinecraftClient)mc).invokeDoItemUse();
            }
        }
        if (this.ticksEnabled && this.ticksToRightClick <= 0) {
            if (this.ticksData == 0) {
                ((IMinecraftClient)mc).invokeDoItemUse();
                this.ticksToRightClick = this.ticksThrow.getValueInt();
                this.ticksData = 1;
            } else if (this.ticksData == 1) {
                ((IMinecraftClient)mc).invokeDoItemUse();
                this.ticksEnabled = false;
            }
        }
        --this.ticksToRightClick;
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (AutoTool.mc.options.keyUse.isDown()) {
            this.ticksEnabled = false;
        }
    }

    public static int getTool(BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoTool.mc.player.getInventory().method_5438(i);
            if (stack == ItemStack.EMPTY) continue;
            float digSpeed = EnchantmentHelper.method_8225((Holder)((Holder)AutoTool.mc.level.method_30349().method_30530(Enchantments.EFFICIENCY.method_58273()).method_40264(Enchantments.EFFICIENCY).get()), (ItemStack)stack);
            float destroySpeed = stack.method_7924(AutoTool.mc.level.method_8320(pos));
            if (AutoTool.mc.level.method_8320(pos).method_26204() instanceof AirBlock) {
                return -1;
            }
            if (!(digSpeed + destroySpeed > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            index = i;
        }
        return index;
    }
}

