/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.ItemStack;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class LowArmorWarning
extends Module {
    public static LowArmorWarning INSTANCE;
    private final SliderSetting durabilityThreshold = this.add(new SliderSetting("DurabilityThreshold", 20.0, 0.0, 500.0, 1.0));
    private final SliderSetting textScale = this.add(new SliderSetting("TextScale", 1.5, 0.5, 3.0, 0.1));
    private final SliderSetting posX = this.add(new SliderSetting("PosX", 0.0, -1000.0, 1000.0, 1.0));
    private final SliderSetting posY = this.add(new SliderSetting("PosY", 0.0, -1000.0, 1000.0, 1.0));
    private static final int WARNING_COLOR;

    public LowArmorWarning() {
        super("LowArmorWarning", Module.Category.Render);
        this.setChinese("\u4f4e\u62a4\u7532\u9884\u8b66");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics DrawContext, float n) {
        if (LowArmorWarning.mc.player == null) {
            return;
        }
        int n2 = Integer.MAX_VALUE;
        for (int i = 0; i < 4; ++i) {
            int n3;
            ItemStack getArmorStack = LowArmorWarning.mc.player.getInventory().method_7372(i);
            if (getArmorStack == null || getArmorStack.method_7960() || (n3 = getArmorStack.getMaxDamage() - getArmorStack.getDamageValue()) >= n2) continue;
            n2 = n3;
        }
        if (n2 == Integer.MAX_VALUE) {
            return;
        }
        if (n2 > (int)this.durabilityThreshold.getValue()) {
            return;
        }
        float n4 = mc.getWindow().getGuiScaledWidth();
        float n5 = mc.getWindow().getGuiScaledHeight();
        float valueFloat = this.textScale.getValueFloat();
        float n6 = n4 / 2.0f / valueFloat - TextUtil.getWidth("Your armor durability is too low!") / 2.0f + (float)this.posX.getValue() / valueFloat;
        float n7 = n5 / 2.0f / valueFloat + (float)this.posY.getValue() / valueFloat;
        PoseStack scrollInHotbar = DrawContext.method_51448();
        scrollInHotbar.method_22903();
        scrollInHotbar.method_22905(valueFloat, valueFloat, 1.0f);
        TextUtil.drawString(DrawContext, "Your armor durability is too low!", n6, n7, WARNING_COLOR, false, true);
        scrollInHotbar.method_22909();
    }

    static {
        WARNING_COLOR = Color.RED.getRGB();
    }
}

