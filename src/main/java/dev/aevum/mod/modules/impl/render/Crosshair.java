/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class Crosshair
extends Module {
    public static Crosshair INSTANCE;
    public final SliderSetting length = this.add(new SliderSetting("Length", 5.0, 0.0, 20.0, 0.1));
    public final SliderSetting thickness = this.add(new SliderSetting("Thickness", 2.0, 0.0, 20.0, 0.1));
    public final SliderSetting interval = this.add(new SliderSetting("Interval", 2.0, 0.0, 20.0, 0.1));
    private final ColorSetting color = this.add(new ColorSetting("Color"));

    public Crosshair() {
        super("Crosshair", Module.Category.Render);
        this.setChinese("\u51c6\u661f");
        INSTANCE = this;
    }

    public void draw(GuiGraphics context) {
        PoseStack matrixStack = context.method_51448();
        float centerX = (float)mc.getWindow().getGuiScaledWidth() / 2.0f;
        float centerY = (float)mc.getWindow().getGuiScaledHeight() / 2.0f;
        Render2DUtil.drawRect(matrixStack, centerX - this.thickness.getValueFloat() / 2.0f, centerY - this.length.getValueFloat() - this.interval.getValueFloat(), this.thickness.getValueFloat(), this.length.getValueFloat(), this.color.getValue());
        Render2DUtil.drawRect(matrixStack, centerX - this.thickness.getValueFloat() / 2.0f, centerY + this.interval.getValueFloat(), this.thickness.getValueFloat(), this.length.getValueFloat(), this.color.getValue());
        Render2DUtil.drawRect(matrixStack, centerX + this.interval.getValueFloat(), centerY - this.thickness.getValueFloat() / 2.0f, this.length.getValueFloat(), this.thickness.getValueFloat(), this.color.getValue());
        Render2DUtil.drawRect(matrixStack, centerX - this.interval.getValueFloat() - this.length.getValueFloat(), centerY - this.thickness.getValueFloat() / 2.0f, this.length.getValueFloat(), this.thickness.getValueFloat(), this.color.getValue());
    }
}

