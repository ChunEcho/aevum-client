/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.TextureAtlasSprite
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.GuiGraphics
 *  net.minecraft.MobEffectTextureManager
 */
package dev.aevum.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.gui.fonts.FontRenderer;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.TextureAtlasSprite;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectInstance;
import net.minecraft.GuiGraphics;
import net.minecraft.MobEffectTextureManager;

public class PotionHud
extends Module
implements HudElement {
    public static PotionHud INSTANCE;
    private int hudX = 2;
    private int hudY = 130;
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final BooleanSetting showIcon = this.add(new BooleanSetting("ShowIcon", true));
    private final SliderSetting textSize = this.add(new SliderSetting("TextSize", 8.0, 4.0, 20.0, 0.5));
    private final SliderSetting iconOffset = this.add(new SliderSetting("IconOffset", 0.0, -10.0, 10.0, 0.1));
    private final SliderSetting spacing = this.add(new SliderSetting("Spacing", 2.0, 0.0, 10.0, 0.5));
    private final BooleanSetting reverseOnRight = this.add(new BooleanSetting("ReverseOnRight", true));
    private final BooleanSetting rightAlign = this.add(new BooleanSetting("RightAlign", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

    public PotionHud() {
        super("Potions", Module.Category.HUD);
        this.setChinese("\u836f\u6c34\u6548\u679c");
        INSTANCE = this;
    }

    private static String getDuration(MobEffectInstance pe) {
        if (pe.method_48559()) {
            return "\u221e";
        }
        int d = pe.method_5584();
        return String.format("%d:%02d", d / 1200, d % 1200 / 20);
    }

    private FontRenderer getPotionFont() {
        return FontManager.get(this.textSize.getValueFloat());
    }

    private float getPotionFontHeight() {
        return this.getPotionFont().getFontHeight();
    }

    private int getPotionIconSize() {
        return Math.max((int)(this.getPotionFontHeight() * 0.8f), 8);
    }

    private List<PotionLine> getPotionLines() {
        ArrayList<PotionLine> lines = new ArrayList<PotionLine>();
        if (PotionHud.mc.player == null) {
            return lines;
        }
        Collection effects = PotionHud.mc.player.getActiveEffects();
        for (MobEffectInstance effect : effects) {
            String name = ((MobEffect)effect.method_5579().comp_349()).getDisplayName().getString();
            int amp = effect.method_5578() + 1;
            String duration = PotionHud.getDuration(effect);
            String text = name + " " + amp + " \u00a7f" + duration;
            lines.add(new PotionLine(effect, text));
        }
        return lines;
    }

    private boolean isRightSide() {
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        return this.getElementX() > mc.getWindow().getGuiScaledWidth() / 2;
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        this.renderElement(drawContext, tickDelta);
    }

    @Override
    public String getElementName() {
        return this.getChinese() != null ? this.getChinese() : this.getName();
    }

    @Override
    public int getElementX() {
        return this.hudX;
    }

    @Override
    public void setElementX(int x) {
        this.hudX = x;
    }

    @Override
    public int getElementY() {
        return this.hudY;
    }

    @Override
    public void setElementY(int y) {
        this.hudY = y;
    }

    @Override
    public int getElementWidth() {
        List<PotionLine> lines = this.getPotionLines();
        FontRenderer font = this.getPotionFont();
        int iconPart = this.showIcon.getValue() ? this.getPotionIconSize() + (int)this.spacing.getValue() : 0;
        int max = 50;
        for (PotionLine line : lines) {
            int w = (int)font.getWidth(line.text) + iconPart;
            if (w <= max) continue;
            max = w;
        }
        return max + 4;
    }

    @Override
    public int getElementHeight() {
        int count = Math.max(this.getPotionLines().size(), 1);
        return (int)((this.getPotionFontHeight() + 2.0f) * (float)count);
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        if (PotionHud.mc.player == null) {
            return;
        }
        List<PotionLine> lines = this.getPotionLines();
        if (lines.isEmpty()) {
            return;
        }
        FontRenderer font = this.getPotionFont();
        float fontHeight = font.getFontHeight();
        int iconSize = this.getPotionIconSize();
        float iconYOff = this.iconOffset.getValueFloat();
        int gap = (int)this.spacing.getValue();
        boolean reverse = this.reverseOnRight.getValue() && this.isRightSide();
        boolean alignRight = this.rightAlign.getValue() && this.isRightSide();
        int totalWidth = this.getElementWidth() - 4;
        float y = this.hudY;
        for (PotionLine line : lines) {
            float iconX;
            float textX;
            float baseX;
            int textWidth = (int)font.getWidth(line.text);
            int lineWidth = textWidth + (this.showIcon.getValue() ? iconSize + gap : 0);
            float f = baseX = alignRight ? (float)(this.hudX + totalWidth - lineWidth) : (float)this.hudX;
            if (this.showIcon.getValue()) {
                if (reverse) {
                    textX = baseX;
                    iconX = baseX + (float)textWidth + (float)gap;
                } else {
                    iconX = baseX;
                    textX = baseX + (float)iconSize + (float)gap;
                }
            } else {
                textX = baseX;
                iconX = baseX;
            }
            if (this.showIcon.getValue()) {
                MobEffectTextureManager sm = Wrapper.mc.getMobEffectTextures();
                TextureAtlasSprite sprite = sm.get(line.effect.method_5579());
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                RenderSystem.enableBlend();
                int iconY = (int)(y + (fontHeight - (float)iconSize) / 2.0f + iconYOff);
                context.method_25298((int)iconX, iconY, 0, iconSize, iconSize, sprite);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            }
            font.drawString(context.method_51448(), line.text, textX, y, (float)this.color.getValue().getRed() / 255.0f, (float)this.color.getValue().getGreen() / 255.0f, (float)this.color.getValue().getBlue() / 255.0f, (float)this.color.getValue().getAlpha() / 255.0f, this.shadow.getValue());
            y += fontHeight + 2.0f;
        }
    }

    private static class PotionLine {
        private final MobEffectInstance effect;
        private final String text;

        private PotionLine(MobEffectInstance effect, String text) {
            this.effect = effect;
            this.text = text;
        }
    }
}

