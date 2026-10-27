/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ResourceLocation
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.skija.SkijaGlowRenderer;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.gui.fonts.FontRenderer;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.Fonts;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class WaterMarkHud
extends Module
implements HudElement {
    public static WaterMarkHud INSTANCE;
    private static final ResourceLocation WURST_TEXTURE;
    private static final float WURST_LOGO_WIDTH = 72.0f;
    private static final float WURST_LOGO_HEIGHT = 18.0f;
    private static final String WURST_VERSION = "v0.01 MC1.21.1";
    private int hudX = 2;
    private int hudY = 2;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.Custom));
    public final EnumSetting<ForeverFontMode> foreverFont = this.add(new EnumSetting<ForeverFontMode>("ForeverFont", ForeverFontMode.Default, () -> this.mode.is(Mode.Forever)));
    public final SliderSetting glitchInterval = this.add(new SliderSetting("GlitchInterval", 1050.0, 200.0, 3000.0, 50.0, () -> this.mode.is(Mode.Forever)));
    public final SliderSetting glitchDuration = this.add(new SliderSetting("GlitchDuration", 230.0, 50.0, 1000.0, 10.0, () -> this.mode.is(Mode.Forever)));
    public final SliderSetting jitterInterval = this.add(new SliderSetting("JitterInterval", 25.0, 10.0, 200.0, 5.0, () -> this.mode.is(Mode.Forever)));
    private final StringSetting title = this.add(new StringSetting("Title", "%hackname% %version%", () -> this.mode.is(Mode.Custom) || this.mode.is(Mode.Forever)));
    private final SliderSetting size = this.add(new SliderSetting("Size", 8.0, 1.0, 20.0, 0.1, () -> this.mode.is(Mode.Custom) || this.mode.is(Mode.Forever)));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true, () -> this.mode.is(Mode.Custom) || this.mode.is(Mode.Forever)));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0), () -> this.mode.is(Mode.Custom)));
    public final BooleanSetting artist = this.add(new BooleanSetting("Artist", false, () -> this.mode.is(Mode.Custom)));
    private final BooleanSetting glow = this.add(new BooleanSetting("Glow", true, () -> this.mode.is(Mode.Custom)).setParent());
    private final SliderSetting glowRadius = this.add(new SliderSetting("GlowRadius", 4.5, 0.1, 10.0, 0.1, () -> this.mode.is(Mode.Custom) && this.glow.isOpen()));
    private final SliderSetting glowOpacity = this.add(new SliderSetting("GlowOpacity", 1.0, 0.1, 1.0, 0.05, () -> this.mode.is(Mode.Custom) && this.glow.isOpen()));
    private final SliderSetting glowYOffset = this.add(new SliderSetting("GlowYOffset", 0.0, -20.0, 10.0, 0.1, () -> this.mode.is(Mode.Custom) && this.glow.isOpen()));
    private final BooleanSetting glowGradient = this.add(new BooleanSetting("Gradient", false, () -> this.mode.is(Mode.Custom) && this.glow.isOpen()));
    private final SliderSetting wurstLogoScale = this.add(new SliderSetting("LogoScale", 1.0, 0.5, 2.0, 0.05, () -> this.mode.is(Mode.Wurst)));
    private ResourceLocation currentGlowTexture = null;
    private String lastGlowConfig = "";
    private long lastGlowUpdate = 0L;
    private long lastGlitchTime = 0L;
    private long lastJitterTime = 0L;
    private boolean isGlitched = false;
    private float rgbOffsetL = 0.0f;
    private float rgbOffsetR = 0.0f;
    private final float[] sliceY = new float[3];
    private final float[] sliceH = new float[3];
    private final float[] sliceX = new float[3];

    public WaterMarkHud() {
        super("WaterMark", Module.Category.HUD);
        this.setChinese("\u6c34\u5370");
        INSTANCE = this;
    }

    public void setWatermarkText(String text) {
        this.title.setValue(text);
    }

    public String getWatermarkTextValue() {
        return this.title.getValue();
    }

    private String getWatermarkText() {
        return this.title.getValue().replaceAll("%version%", "4.0").replaceAll("%hackname%", "Alien");
    }

    private Color getCurrentColor() {
        return this.color.getValue();
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
        FontRenderer artistFont;
        if (this.mode.is(Mode.Wurst)) {
            return (int)this.getWurstWidth();
        }
        if (this.mode.is(Mode.Forever)) {
            return (int)this.getForeverFontRenderer().getWidth(this.getWatermarkText()) + 4;
        }
        if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            return (int)artistFont.getWidth(this.getWatermarkText()) + 4;
        }
        if (this.isMinecraftFont()) {
            return (int)((float)WaterMarkHud.mc.font.method_1727(this.getWatermarkText()) * this.getMinecraftScale()) + 4;
        }
        return (int)this.getFont().getWidth(this.getWatermarkText()) + 4;
    }

    @Override
    public int getElementHeight() {
        FontRenderer artistFont;
        if (this.mode.is(Mode.Wurst)) {
            return (int)this.getWurstHeight();
        }
        if (this.mode.is(Mode.Forever)) {
            return (int)this.getForeverFontRenderer().getFontHeight() + 4;
        }
        if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            return (int)artistFont.getFontHeight() + 4;
        }
        if (this.isMinecraftFont()) {
            return (int)(9.0f * this.getMinecraftScale()) + 4;
        }
        return (int)this.getFont().getFontHeight() + 4;
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        if (!this.isOn()) {
            return;
        }
        if (this.mode.is(Mode.Wurst)) {
            this.cleanupGlowTexture();
            this.renderWurstWatermark(context);
            return;
        }
        String text = this.getWatermarkText();
        if (text == null || text.isEmpty()) {
            this.cleanupGlowTexture();
            return;
        }
        if (this.mode.is(Mode.Forever)) {
            this.cleanupGlowTexture();
            this.renderForeverWatermark(context, text);
            return;
        }
        this.renderWatermark(context, text);
    }

    private void renderForeverWatermark(GuiGraphics context, String text) {
        PoseStack ms = context.method_51448();
        FontRenderer fr = this.getForeverFontRenderer();
        float textWidth = fr.getWidth(text);
        float textHeight = fr.getFontHeight();
        long time = System.currentTimeMillis();
        long interval = (long)this.glitchInterval.getValue();
        long duration = (long)this.glitchDuration.getValue();
        long jitterRate = (long)this.jitterInterval.getValue();
        long timeSinceLast = time - this.lastGlitchTime;
        if (timeSinceLast > interval) {
            this.lastGlitchTime = time;
            this.lastJitterTime = time;
            this.isGlitched = true;
            this.generateGlitchOffsets(textHeight);
        } else if (this.isGlitched && timeSinceLast > duration) {
            this.isGlitched = false;
            this.resetGlitchOffsets();
        } else if (this.isGlitched && time - this.lastJitterTime > jitterRate) {
            this.lastJitterTime = time;
            this.jitterGlitchOffsets(textHeight);
        }
        this.drawForeverRGBText(ms, fr, text, this.hudX, this.hudY, this.rgbOffsetL, this.rgbOffsetR);
        if (this.isGlitched) {
            for (int i = 0; i < 3; ++i) {
                if (this.sliceX[i] == 0.0f) continue;
                int sx = this.hudX - 15;
                int sy = (int)((float)this.hudY + this.sliceY[i]);
                int sw = (int)textWidth + 30;
                int sh = (int)this.sliceH[i];
                context.method_44379(sx, sy, sx + sw, sy + sh);
                this.drawForeverRGBText(ms, fr, text, (float)this.hudX + this.sliceX[i], this.hudY, this.rgbOffsetL * 1.8f, this.rgbOffsetR * 1.8f);
                context.disableScissor();
            }
        }
    }

    private void generateGlitchOffsets(float textHeight) {
        this.rgbOffsetL = (float)((Math.random() - 0.5) * 6.0);
        this.rgbOffsetR = (float)((Math.random() - 0.5) * 6.0);
        for (int i = 0; i < 3; ++i) {
            this.sliceY[i] = (float)(Math.random() * (double)textHeight);
            this.sliceH[i] = (float)(Math.random() * ((double)textHeight / 2.5));
            this.sliceX[i] = (float)((Math.random() - 0.5) * 12.0);
        }
    }

    private void jitterGlitchOffsets(float textHeight) {
        this.rgbOffsetL = (float)((Math.random() - 0.5) * 6.0);
        this.rgbOffsetR = (float)((Math.random() - 0.5) * 6.0);
        if (Math.random() > 0.5) {
            for (int i = 0; i < 3; ++i) {
                this.sliceX[i] = (float)((Math.random() - 0.5) * 12.0);
            }
        }
    }

    private void resetGlitchOffsets() {
        this.rgbOffsetL = 0.0f;
        this.rgbOffsetR = 0.0f;
        for (int i = 0; i < 3; ++i) {
            this.sliceX[i] = 0.0f;
        }
    }

    private void drawForeverRGBText(PoseStack ms, FontRenderer fr, String text, float x, float y, float offL, float offR) {
        RenderSystem.enableBlend();
        if (offL != 0.0f || offR != 0.0f) {
            fr.drawString(ms, text, x + offL, y, 0.0f, 1.0f, 1.0f, 0.75f, false);
            fr.drawString(ms, text, x + offR, y, 1.0f, 0.0f, 0.15f, 0.75f, false);
        }
        fr.drawString(ms, text, x, y, 1.0f, 1.0f, 1.0f, 1.0f, this.shadow.getValue());
    }

    private FontRenderer getForeverFontRenderer() {
        float s = this.size.getValueFloat();
        FontRenderer fr = null;
        if (this.foreverFont.is(ForeverFontMode.OXANIUM)) {
            fr = FontManager.getOxaniumFont(s);
        } else if (this.foreverFont.is(ForeverFontMode.ORBITRON)) {
            fr = FontManager.getOrbitronFont(s);
        }
        return fr != null ? fr : this.getFont();
    }

    private void renderWatermark(GuiGraphics context, String text) {
        PoseStack ms = context.method_51448();
        Color textColor = this.getCurrentColor();
        if (!this.glow.getValue()) {
            this.cleanupGlowTexture();
            this.drawPlainText(context, ms, text, textColor);
            return;
        }
        if (!this.artist.getValue() && this.isMinecraftFont()) {
            this.cleanupGlowTexture();
            this.drawMinecraftGlow(context, ms, text, textColor);
            return;
        }
        Color glowColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), Math.min(255, (int)((double)textColor.getAlpha() * this.glowOpacity.getValue())));
        boolean artistFont = this.artist.getValue();
        String forceFont = artistFont ? "borel-regular.ttf" : null;
        float fontSize = this.size.getValueFloat();
        String config = String.format("%s_%.1f_%.2f_%.1f_%08x_%08x_%b_%b_%s_%.1f_%s", new Object[]{text, this.glowRadius.getValue(), this.glowOpacity.getValue(), this.glowYOffset.getValue(), textColor.getRGB(), glowColor.getRGB(), this.glowGradient.getValue(), artistFont, FontManager.getMode(), Float.valueOf(fontSize), forceFont});
        boolean needsUpdate = this.currentGlowTexture == null || !config.equals(this.lastGlowConfig) || System.currentTimeMillis() - this.lastGlowUpdate > 5000L;
        boolean bl = needsUpdate;
        if (needsUpdate) {
            this.cleanupGlowTexture();
            this.currentGlowTexture = SkijaGlowRenderer.createGlowTexture(text, fontSize, textColor, glowColor, this.glowRadius.getValueFloat(), this.glowGradient.getValue(), this.glowGradient.getValue() ? glowColor : null, forceFont);
            this.lastGlowConfig = config;
            this.lastGlowUpdate = System.currentTimeMillis();
        }
        if (this.currentGlowTexture != null && SkijaGlowRenderer.isTextureReady(this.currentGlowTexture)) {
            this.drawGlowTexture(context, ms, text);
            return;
        }
        this.drawPlainText(context, ms, text, textColor);
    }

    private void drawPlainText(GuiGraphics context, PoseStack matrixStack, String text, Color textColor) {
        FontRenderer artistFont;
        if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            artistFont.drawString(matrixStack, text, this.hudX, this.hudY, (float)textColor.getRed() / 255.0f, (float)textColor.getGreen() / 255.0f, (float)textColor.getBlue() / 255.0f, (float)textColor.getAlpha() / 255.0f, this.shadow.getValue());
            return;
        }
        if (this.isMinecraftFont()) {
            this.drawMinecraftText(context, matrixStack, text, textColor);
            return;
        }
        this.getFont().drawString(matrixStack, text, this.hudX, this.hudY, (float)textColor.getRed() / 255.0f, (float)textColor.getGreen() / 255.0f, (float)textColor.getBlue() / 255.0f, (float)textColor.getAlpha() / 255.0f, this.shadow.getValue());
    }

    private void drawGlowTexture(GuiGraphics context, PoseStack matrixStack, String text) {
        float textWidth = this.getTextWidth(text);
        float textHeight = this.getTextHeight();
        float glowPadding = this.glowRadius.getValueFloat() * 3.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        matrixStack.method_22903();
        matrixStack.method_46416((float)this.hudX - glowPadding, (float)this.hudY + this.glowYOffset.getValueFloat() - glowPadding, 0.0f);
        context.method_25290(this.currentGlowTexture, 0, 0, 0.0f, 0.0f, (int)(textWidth + glowPadding * 2.0f), (int)(textHeight + glowPadding * 2.0f), (int)(textWidth + glowPadding * 2.0f), (int)(textHeight + glowPadding * 2.0f));
        matrixStack.method_22909();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void renderWurstWatermark(GuiGraphics context) {
        PoseStack matrixStack = context.method_51448();
        float scale = this.wurstLogoScale.getValueFloat();
        float scaledWidth = 72.0f * scale;
        float scaledHeight = 18.0f * scale;
        float textScale = 0.95f;
        float textWidth = (float)WaterMarkHud.mc.font.method_1727(WURST_VERSION) * textScale;
        float backgroundWidth = textWidth + scaledWidth + 3.0f;
        float backgroundHeight = 11.0f * scale;
        float logoY = (float)this.hudY + 3.0f * scale;
        float backgroundY = logoY + 7.0f - 2.0f * scale;
        context.method_25294(this.hudX, (int)backgroundY, (int)((float)this.hudX + backgroundWidth), (int)(backgroundY + backgroundHeight), new Color(255, 255, 255, 80).getRGB());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        matrixStack.method_22903();
        matrixStack.method_46416((float)this.hudX, logoY, 0.0f);
        matrixStack.method_22905(scale, scale, 1.0f);
        context.method_25290(WURST_TEXTURE, 0, 0, 0.0f, 0.0f, 72, 18, 72, 18);
        matrixStack.method_22909();
        matrixStack.method_22903();
        matrixStack.method_46416((float)this.hudX + scaledWidth + scale, logoY + 7.0f, 0.0f);
        matrixStack.method_22905(textScale, textScale, 1.0f);
        context.method_51433(WaterMarkHud.mc.font, WURST_VERSION, 0, 0, Color.WHITE.getRGB(), false);
        matrixStack.method_22909();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableBlend();
    }

    private float getWurstWidth() {
        float scale = this.wurstLogoScale.getValueFloat();
        float textScale = 0.95f;
        return (float)WaterMarkHud.mc.font.method_1727(WURST_VERSION) * textScale + 72.0f * scale + 3.0f;
    }

    private float getWurstHeight() {
        return 21.0f * this.wurstLogoScale.getValueFloat();
    }

    private void drawMinecraftGlow(GuiGraphics context, PoseStack matrixStack, String text, Color textColor) {
        int layers;
        float radius = this.glowRadius.getValueFloat();
        float yOffset = this.glowYOffset.getValueFloat();
        for (int i = layers = Math.max(1, (int)Math.ceil(radius * 2.0f)); i > 0; --i) {
            float offset = (float)i * 0.35f;
            int alpha = Math.min(255, (int)((double)textColor.getAlpha() * this.glowOpacity.getValue() / (double)(i + 1)));
            Color glowColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha);
            this.drawMinecraftText(context, matrixStack, text, glowColor, -offset, yOffset - offset);
            this.drawMinecraftText(context, matrixStack, text, glowColor, offset, yOffset - offset);
            this.drawMinecraftText(context, matrixStack, text, glowColor, -offset, yOffset + offset);
            this.drawMinecraftText(context, matrixStack, text, glowColor, offset, yOffset + offset);
        }
        this.drawMinecraftText(context, matrixStack, text, textColor, 0.0f, yOffset);
    }

    private void drawMinecraftText(GuiGraphics context, PoseStack matrixStack, String text, Color textColor) {
        this.drawMinecraftText(context, matrixStack, text, textColor, 0.0f, 0.0f);
    }

    private void drawMinecraftText(GuiGraphics context, PoseStack matrixStack, String text, Color textColor, float offsetX, float offsetY) {
        float scale = this.getMinecraftScale();
        matrixStack.method_22903();
        matrixStack.method_46416((float)this.hudX + offsetX, (float)this.hudY + offsetY, 0.0f);
        matrixStack.method_22905(scale, scale, 1.0f);
        context.method_51433(WaterMarkHud.mc.font, text, 0, 0, textColor.getRGB(), this.shadow.getValue());
        matrixStack.method_22909();
    }

    private FontRenderer getArtistFont() {
        return FontManager.getArtistFont(this.size.getValueFloat());
    }

    private FontRenderer getFont() {
        return FontManager.get(this.size.getValueFloat());
    }

    private float getTextWidth(String text) {
        FontRenderer artistFont;
        if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            return artistFont.getWidth(text);
        }
        if (this.isMinecraftFont()) {
            return (float)WaterMarkHud.mc.font.method_1727(text) * this.getMinecraftScale();
        }
        return this.getFont().getWidth(text);
    }

    private float getTextHeight() {
        FontRenderer artistFont;
        if (this.artist.getValue() && (artistFont = this.getArtistFont()) != null) {
            return artistFont.getFontHeight();
        }
        if (this.isMinecraftFont()) {
            return 9.0f * this.getMinecraftScale();
        }
        return this.getFont().getFontHeight();
    }

    private boolean isMinecraftFont() {
        return FontManager.getMode() == Fonts.FontMode.MINECRAFT;
    }

    private float getMinecraftScale() {
        return this.size.getValueFloat() / 8.0f;
    }

    private void cleanupGlowTexture() {
        if (this.currentGlowTexture != null) {
            SkijaGlowRenderer.cleanupTexture(this.currentGlowTexture);
            this.currentGlowTexture = null;
        }
        this.lastGlowConfig = "";
        this.lastGlowUpdate = 0L;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.cleanupGlowTexture();
    }

    static {
        WURST_TEXTURE = ResourceLocation.method_60655((String)"forever", (String)"textures/images/wurst_128.png");
    }

    public static enum Mode {
        Custom,
        Wurst,
        Forever;

    }

    public static enum ForeverFontMode {
        Default,
        OXANIUM,
        ORBITRON;

    }
}

