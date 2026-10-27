/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class DFTitle
extends Module
implements HudElement {
    public static DFTitle INSTANCE;
    private int hudX = 10;
    private int hudY = 8;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Normal));
    public final SliderSetting paddingY = this.add(new SliderSetting("HeightAdjust", 0.0, -10.0, 10.0, 0.5));
    public final SliderSetting textYOffset = this.add(new SliderSetting("TextYOffset", 3.0, -10.0, 10.0, 0.5));
    private static final int BG_ALPHA = 29;
    private static final float PADDING_X = 8.0f;
    private static final boolean SHADOW = false;

    public DFTitle() {
        super("DFTitle", Module.Category.HUD);
        this.setChinese("\u4e09\u89d2\u6d32\u6807\u9898");
        INSTANCE = this;
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

    private String getDisplayString() {
        return switch (this.mode.getValue().ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> "\u673a\u5bc6";
            case 2 -> "\u7edd\u5bc6";
            case 0 -> "\u5e38\u89c4";
        };
    }

    private Color getTextColor() {
        return switch (this.mode.getValue().ordinal()) {
            case 2 -> new Color(255, 0, 0, 255);
            default -> Color.WHITE;
        };
    }

    @Override
    public int getElementWidth() {
        float textWidth = FontManager.getWidth(this.getDisplayString());
        return (int)(textWidth + 16.0f);
    }

    @Override
    public int getElementHeight() {
        float textHeight = FontManager.getFontHeight();
        return (int)(textHeight + this.paddingY.getValueFloat() * 2.0f);
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        this.renderElement(drawContext, tickDelta);
    }

    @Override
    public void renderElement(GuiGraphics drawContext, float tickDelta) {
        if (DFTitle.mc.player == null || DFTitle.mc.level == null) {
            return;
        }
        String displayString = this.getDisplayString();
        Color textColor = this.getTextColor();
        float rectWidth = this.getElementWidth();
        float rectHeight = this.getElementHeight();
        float x = this.hudX;
        float y = this.hudY;
        PoseStack matrices = drawContext.method_51448();
        Render2DUtil.drawRect(matrices, x, y, rectWidth, rectHeight, new Color(0, 0, 0, 29));
        float textX = x + 8.0f;
        float textY = y + this.paddingY.getValueFloat() + this.textYOffset.getValueFloat();
        this.drawContextText(drawContext, displayString, textX, textY, textColor, false);
    }

    private void drawContextText(GuiGraphics drawContext, String text, float x, float y, Color color, boolean shadow) {
        PoseStack matrices = drawContext.method_51448();
        if (FontManager.isCustom()) {
            FontManager.get().drawString(matrices, text, x, y, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f, shadow);
        } else {
            drawContext.method_51433(DFTitle.mc.font, text, (int)x, (int)y, color.getRGB(), shadow);
        }
    }

    public static enum Mode {
        Normal,
        Medium,
        Hard;

    }
}

