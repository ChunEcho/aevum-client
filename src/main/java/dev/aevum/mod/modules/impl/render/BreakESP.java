/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.AirItem
 *  net.minecraft.ItemStack
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.BlockGetter
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.Component
 *  net.minecraft.BlockState
 *  net.minecraft.PoseStack
 *  net.minecraft.Holder
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.Aevum;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.core.impl.BreakManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Player;
import net.minecraft.AirItem;
import net.minecraft.ItemStack;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.BlockGetter;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.Component;
import net.minecraft.BlockState;
import net.minecraft.PoseStack;
import net.minecraft.Holder;

public class BreakESP
extends Module {
    public static BreakESP INSTANCE;
    private final BooleanSetting progress = this.add(new BooleanSetting("Progress", true));
    private final SliderSetting damage = this.add(new SliderSetting("Damage", 1.0, 0.0, 2.0, 0.01));
    private final ColorSetting box = this.add(new ColorSetting("Box", new Color(198, 176, 12, 255)).injectBoolean(true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(198, 176, 12, 78)).injectBoolean(true));
    private final ColorSetting boxFriend = this.add(new ColorSetting("FriendBox", new Color(30, 45, 169, 255)).injectBoolean(true));
    private final ColorSetting fillFriend = this.add(new ColorSetting("FriendFill", new Color(30, 45, 169, 78)).injectBoolean(true));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut));
    private final BooleanSetting second = this.add(new BooleanSetting("Second", true));
    private final ColorSetting secondBox = this.add(new ColorSetting("SecondBox", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting secondFill = this.add(new ColorSetting("SecondFill", new Color(255, 255, 255, 100)).injectBoolean(true));
    final DecimalFormat df = new DecimalFormat("0.0");
    final Color startColor = new Color(255, 6, 6);
    final Color endColor = new Color(0, 255, 12);
    final Color doubleColor = new Color(255, 179, 96);

    public BreakESP() {
        super("BreakESP", Module.Category.Render);
        this.setChinese("\u6316\u6398\u663e\u793a");
        INSTANCE = this;
    }

    private Color getFillColor(Player player) {
        return Aevum.FRIEND.isFriend(player) ? this.fillFriend.getValue() : this.fill.getValue();
    }

    private Color getBoxColor(Player player) {
        return Aevum.FRIEND.isFriend(player) ? this.boxFriend.getValue() : this.box.getValue();
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        for (BreakManager.BreakData breakData : Aevum.BREAK.breakMap.values()) {
            if (breakData == null || breakData.getEntity() == null) continue;
            Player player = (Player)breakData.getEntity();
            double size = 0.5 * (1.0 - breakData.fade.ease(this.ease.getValue()));
            AABB cbox = new AABB(breakData.pos).method_1002(size, size, size).method_1002(-size, -size, -size);
            if (this.fill.booleanValue) {
                Render3DUtil.drawFill(matrixStack, cbox, this.getFillColor(player));
            }
            if (this.box.booleanValue) {
                Render3DUtil.drawBox(matrixStack, cbox, this.getBoxColor(player));
            }
            Render3DUtil.drawText3D(player.method_5477().getString(), breakData.pos.method_46558().method_1031(0.0, this.progress.getValue() ? 0.15 : 0.0, 0.0), -1);
            if (!this.progress.getValue()) continue;
            Render3DUtil.drawText3D(Component.method_30163((String)(breakData.failed ? "\u00a74Failed" : (breakData.complete ? "Broke" : this.df.format(Math.min(1.0, (double)breakData.timer.getMs() / breakData.breakTime) * 100.0)))), breakData.pos.method_46558().method_1031(0.0, -0.15, 0.0), 0.0, 0.0, 1.0, breakData.complete ? (BreakESP.mc.level.isEmptyBlock(breakData.pos) ? this.endColor : this.startColor) : ColorUtil.fadeColor(this.startColor, this.endColor, (double)breakData.timer.getMs() / breakData.breakTime));
        }
        if (this.second.getValue()) {
            Iterator iterator = ((ConcurrentHashMap.KeySetView)Aevum.BREAK.doubleMap.keySet()).iterator();
            while (iterator.hasNext()) {
                int i = (Integer)iterator.next();
                BreakManager.BreakData breakDatax = Aevum.BREAK.doubleMap.get(i);
                if (breakDatax != null && breakDatax.getEntity() != null && !BreakESP.mc.level.isEmptyBlock(breakDatax.pos)) {
                    BreakManager.BreakData singleBreakData = Aevum.BREAK.breakMap.get(i);
                    if (singleBreakData != null && singleBreakData.pos.equals((Object)breakDatax.pos)) continue;
                    double sizex = 0.5 * (1.0 - breakDatax.fade.ease(this.ease.getValue()));
                    AABB cboxx = new AABB(breakDatax.pos).method_1002(sizex, sizex, sizex).method_1002(-sizex, -sizex, -sizex);
                    if (this.secondFill.booleanValue) {
                        Render3DUtil.drawFill(matrixStack, cboxx, this.secondFill.getValue());
                    }
                    if (this.secondBox.booleanValue) {
                        Render3DUtil.drawBox(matrixStack, cboxx, this.secondBox.getValue());
                    }
                    Render3DUtil.drawText3D(breakDatax.getEntity().method_5477().getString(), breakDatax.pos.method_46558().method_1031(0.0, 0.15, 0.0), -1);
                    Render3DUtil.drawText3D("Double", breakDatax.pos.method_46558().method_1031(0.0, -0.15, 0.0), this.doubleColor.getRGB());
                    continue;
                }
                Aevum.BREAK.doubleMap.remove(i);
            }
        }
    }

    public static double getBreakTime(BlockPos pos, boolean extraBreak) {
        int slot = BreakESP.getTool(pos);
        if (slot == -1) {
            slot = BreakESP.mc.player.getInventory().selected;
        }
        return BreakESP.getBreakTime(pos, slot, extraBreak ? 1.0 : BreakESP.INSTANCE.damage.getValue());
    }

    static int getTool(BlockPos pos) {
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        float CurrentFastest = 1.0f;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            float destroySpeed;
            float digSpeed;
            if (entry.getValue().method_7909() instanceof AirItem || !((digSpeed = (float)EnchantmentHelper.method_8225((Holder)((Holder)BreakESP.mc.level.method_30349().method_46762(Enchantments.EFFICIENCY.method_58273()).method_46746(Enchantments.EFFICIENCY).get()), (ItemStack)entry.getValue())) + (destroySpeed = entry.getValue().method_7924(BreakESP.mc.level.method_8320(pos))) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            slot.set(entry.getKey());
        }
        return slot.get();
    }

    static double getBreakTime(BlockPos pos, int slot, double damage) {
        return (double)(1.0f / BreakESP.getBlockStrength(pos, BreakESP.mc.player.getInventory().method_5438(slot)) / 20.0f * 1000.0f) * damage;
    }

    static float getBlockStrength(BlockPos position, ItemStack itemStack) {
        BlockState state = BreakESP.mc.level.method_8320(position);
        float hardness = state.method_26214((BlockGetter)BreakESP.mc.level, position);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        float i = state.requiresCorrectToolForDrops() && !itemStack.method_7951(state) ? 100.0f : 30.0f;
        return BreakESP.getDigSpeed(state, itemStack) / hardness / i;
    }

    static float getDigSpeed(BlockState state, ItemStack itemStack) {
        int efficiencyModifier;
        float digSpeed = BreakESP.getDestroySpeed(state, itemStack);
        if (digSpeed > 1.0f && (efficiencyModifier = EnchantmentHelper.method_8225((Holder)((Holder)BreakESP.mc.level.method_30349().method_46762(Enchantments.EFFICIENCY.method_58273()).method_46746(Enchantments.EFFICIENCY).get()), (ItemStack)itemStack)) > 0 && !itemStack.method_7960()) {
            digSpeed += (float)(StrictMath.pow(efficiencyModifier, 2.0) + 1.0);
        }
        return digSpeed < 0.0f ? 0.0f : digSpeed;
    }

    static float getDestroySpeed(BlockState state, ItemStack itemStack) {
        float destroySpeed = 1.0f;
        if (itemStack != null && !itemStack.method_7960()) {
            destroySpeed *= itemStack.method_7924(state);
        }
        return destroySpeed;
    }
}

