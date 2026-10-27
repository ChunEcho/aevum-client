/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class LowHealthWarning
extends Module {
    public static LowHealthWarning INSTANCE;
    private final SliderSetting healthThreshold = this.add(new SliderSetting("HealthThreshold", 6.0, 0.0, 36.0, 0.5));
    private final SliderSetting textScale = this.add(new SliderSetting("TextScale", 1.5, 0.5, 3.0, 0.1));
    private final SliderSetting posX = this.add(new SliderSetting("PosX", 0.0, -1000.0, 1000.0, 1.0));
    private final SliderSetting posY = this.add(new SliderSetting("PosY", 0.0, -1000.0, 1000.0, 1.0));
    private static final int WARNING_COLOR;

    public LowHealthWarning() {
        super("LowHealthWarning", Module.Category.Render);
        this.setChinese("\u4f4e\u8840\u91cf\u9884\u8b66");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics DrawContext, float n) {
        if (LowHealthWarning.mc.player == null) {
            return;
        }
        if (LowHealthWarning.mc.player.getHealth() + LowHealthWarning.mc.player.getAbsorptionAmount() > (float)this.healthThreshold.getValue()) {
            return;
        }
        float n2 = mc.getWindow().getGuiScaledWidth();
        float n3 = mc.getWindow().getGuiScaledHeight();
        float valueFloat = this.textScale.getValueFloat();
        float n4 = n2 / 2.0f / valueFloat - TextUtil.getWidth("Your health is too low!") / 2.0f + (float)this.posX.getValue() / valueFloat;
        float n5 = n3 / 2.0f / valueFloat + (float)this.posY.getValue() / valueFloat;
        PoseStack getMatrices = DrawContext.method_51448();
        getMatrices.method_22903();
        getMatrices.method_22905(valueFloat, valueFloat, 1.0f);
        TextUtil.drawString(DrawContext, "Your health is too low!", n4, n5, WARNING_COLOR, false, true);
        getMatrices.method_22909();
    }

    static {
        WARNING_COLOR = Color.RED.getRGB();
    }
}

