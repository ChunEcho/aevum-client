/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.FoodData
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.api.utils.math.SmoothAnimationUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.FoodData;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class DFHungerBar
extends Module {
    public static DFHungerBar INSTANCE;
    public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", -94.0, -300.0, 300.0, 1.0));
    public final BooleanSetting leftSettings = this.add(new BooleanSetting("LeftBar", true).setParent());
    public final SliderSetting leftWidth = this.add(new SliderSetting("LeftWidth", 71.0, 20.0, 300.0, 1.0, () -> this.leftSettings.isOpen()));
    public final SliderSetting leftHeight = this.add(new SliderSetting("LeftHeight", 4.0, 1.0, 10.0, 1.0, () -> this.leftSettings.isOpen()));
    public final BooleanSetting rightSettings = this.add(new BooleanSetting("RightBar", true).setParent());
    public final SliderSetting rightWidth = this.add(new SliderSetting("RightWidth", 34.0, 10.0, 200.0, 1.0, () -> this.rightSettings.isOpen()));
    public final SliderSetting rightHeight = this.add(new SliderSetting("RightHeight", 4.0, 1.0, 10.0, 1.0, () -> this.rightSettings.isOpen()));
    public final BooleanSetting dividerSettings = this.add(new BooleanSetting("Divider", true).setParent());
    public final SliderSetting dividerWidth = this.add(new SliderSetting("DividerWidth", 1.0, 1.0, 6.0, 1.0, () -> this.dividerSettings.isOpen()));
    public final SliderSetting dividerGap = this.add(new SliderSetting("DividerGap", 0.0, 0.0, 6.0, 1.0, () -> this.dividerSettings.isOpen()));
    public final ColorSetting dividerColor = this.add(new ColorSetting("DividerColor", new Color(0, 0, 0, 100), () -> this.dividerSettings.isOpen()));
    public final ColorSetting barColor = this.add(new ColorSetting("BarColor", new Color(255, 255, 255, 255)));
    public final ColorSetting bgColor = this.add(new ColorSetting("Background", new Color(136, 136, 136, 136)));
    public final SliderSetting smoothFactor = this.add(new SliderSetting("SmoothFactor", 0.08, 0.01, 0.5, 0.01));
    public final BooleanSetting hideVanilla = this.add(new BooleanSetting("HideVanilla", false));
    private final SmoothAnimationUtil smoothFood = new SmoothAnimationUtil(20.0, 0.08);
    private final SmoothAnimationUtil smoothSaturation = new SmoothAnimationUtil(5.0, 0.08);

    public DFHungerBar() {
        super("DFHungerBar", Module.Category.HUD);
        this.setChinese("\u4e09\u89d2\u6d32\u4f53\u529b\u6761");
        INSTANCE = this;
    }

    public static boolean shouldHideVanilla() {
        return INSTANCE != null && INSTANCE.isOn() && DFHungerBar.INSTANCE.hideVanilla.getValue();
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        if (DFHungerBar.mc.player == null || DFHungerBar.mc.level == null) {
            return;
        }
        FoodData hunger = DFHungerBar.mc.player.getFoodData();
        int foodLevel = hunger.method_7586();
        float saturationLevel = hunger.getSaturationLevel();
        float displaySaturation = foodLevel >= 20 ? saturationLevel : 0.0f;
        this.smoothFood.setTarget(foodLevel);
        this.smoothSaturation.setTarget(displaySaturation);
        double sf = this.smoothFactor.getValue();
        double smoothFoodVal = this.updateSmooth(this.smoothFood, sf);
        double smoothSatVal = this.updateSmooth(this.smoothSaturation, sf);
        float foodRatio = (float)Math.max(0.0, Math.min(smoothFoodVal / 20.0, 1.0));
        float satRatio = (float)Math.max(0.0, Math.min(smoothSatVal / 20.0, 1.0));
        float lw = this.leftWidth.getValueFloat();
        float lh = this.leftHeight.getValueFloat();
        float rw = this.rightWidth.getValueFloat();
        float rh = this.rightHeight.getValueFloat();
        float dw = this.dividerSettings.getValue() ? this.dividerWidth.getValueFloat() : 0.0f;
        float dg = this.dividerSettings.getValue() ? this.dividerGap.getValueFloat() : 0.0f;
        float totalWidth = lw + dg + dw + dg + rw;
        float screenW = mc.getWindow().getGuiScaledWidth();
        float screenH = mc.getWindow().getGuiScaledHeight();
        float startX = (screenW - totalWidth) / 2.0f;
        float baseY = screenH + (float)this.yOffset.getValueInt();
        PoseStack matrices = drawContext.method_51448();
        Color bar = this.barColor.getValue();
        Color bg = this.bgColor.getValue();
        float leftX = startX;
        float leftY = baseY - lh / 2.0f;
        Render2DUtil.drawRect(matrices, leftX, leftY, lw, lh, bg);
        if (foodRatio > 0.0f) {
            Render2DUtil.drawRect(matrices, leftX, leftY, lw * foodRatio, lh, bar);
        }
        if (this.dividerSettings.getValue()) {
            float divX = leftX + lw + dg;
            float divH = Math.max(lh, rh) + 2.0f;
            float divY = baseY - divH / 2.0f;
            Render2DUtil.drawRect(matrices, divX, divY, dw, divH, this.dividerColor.getValue());
        }
        float rightX = leftX + lw + dg + dw + dg;
        float rightY = baseY - rh / 2.0f;
        Render2DUtil.drawRect(matrices, rightX, rightY, rw, rh, bg);
        if (satRatio > 0.0f) {
            Render2DUtil.drawRect(matrices, rightX, rightY, rw * satRatio, rh, bar);
        }
    }

    private double updateSmooth(SmoothAnimationUtil anim, double factor) {
        double current = anim.get();
        double target = anim.getTarget();
        double diff = target - current;
        if (Math.abs(diff) < 0.01) {
            anim.set(target);
            return target;
        }
        double newVal = current + diff * factor;
        this.setCurrentOnly(anim, newVal);
        return newVal;
    }

    private void setCurrentOnly(SmoothAnimationUtil anim, double newCurrent) {
        double savedTarget = anim.getTarget();
        anim.set(newCurrent);
        anim.setTarget(savedTarget);
    }
}

