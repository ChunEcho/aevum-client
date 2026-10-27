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
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.InitEvent;
import dev.aevum.api.utils.math.Animation;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.api.utils.skija.SkijaGlowRenderer;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.gui.fonts.FontRenderer;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class ArrayListHud
extends Module
implements HudElement {
    public static ArrayListHud INSTANCE;
    private int hudX = 2;
    private int hudY = 1;
    private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
    private final BooleanSetting sortByLength = this.add(new BooleanSetting("Sort", true));
    private final BooleanSetting renderUp = this.add(new BooleanSetting("RenderUp", false));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false));
    private final BooleanSetting showSpace = this.add(new BooleanSetting("Space", true));
    private final BooleanSetting forgeHax = this.add(new BooleanSetting("ForgeHax", false));
    private final BooleanSetting listGlow = this.add(new BooleanSetting("Glow", false).setParent());
    private final SliderSetting listGlowRadius = this.add(new SliderSetting("GlowRadius", 3.0, 0.5, 8.0, 0.1, () -> this.listGlow.isOpen()));
    private final SliderSetting listGlowOpacity = this.add(new SliderSetting("GlowOpacity", 0.6, 0.1, 1.0, 0.05, () -> this.listGlow.isOpen()));
    private final SliderSetting interval = this.add(new SliderSetting("Interval", 0.0, 0.0, 15.0, 0.1));
    private final SliderSetting enableLength = this.add(new SliderSetting("EnableLength", 200, 0, 1000));
    private final SliderSetting disableLength = this.add(new SliderSetting("DisableLength", 200, 0, 1000));
    private final SliderSetting fadeLength = this.add(new SliderSetting("FadeLength", 200, 0, 1000));
    private final EnumSetting<Easing> easing = this.add(new EnumSetting<Easing>("Easing", Easing.CircInOut));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
    private final EnumSetting<ColorMode> colorMode = this.add(new EnumSetting<ColorMode>("ColorMode", ColorMode.Pulse));
    private final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0, 0.0, 5.0, 0.1));
    private final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50));
    private final ColorSetting endColor = this.add(new ColorSetting("SecondColor", new Color(255, 0, 0)).injectBoolean(true));
    private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 4.0, 1.0, 10.0, 0.1));
    private final SliderSetting saturation = this.add(new SliderSetting("Saturation", 130, 1, 255));
    private final SliderSetting rainbowDelay = this.add(new SliderSetting("Delay", 350, 0, 1000));
    private final BooleanSetting backGround = this.add(new BooleanSetting("BackGround", false).setParent());
    private final SliderSetting bgWidth = this.add(new SliderSetting("BGWidth", 0.0, 0.0, 15.0, () -> this.backGround.isOpen()));
    private final ColorSetting bgColor = this.add(new ColorSetting("BGColor", new Color(0, 0, 0, 100), () -> this.backGround.isOpen()));
    private final ColorSetting rect = this.add(new ColorSetting("Rect", new Color(208, 0, 0)).injectBoolean(false));
    private final ArrayList<ModuleInfo> moduleList = new ArrayList();
    private boolean initialized = false;
    private final ConcurrentHashMap<String, ResourceLocation> listGlowCache = new ConcurrentHashMap();
    private final ConcurrentHashMap<String, Long> listGlowUpdateTime = new ConcurrentHashMap();

    public ArrayListHud() {
        super("ArrayList", Module.Category.HUD);
        this.setChinese("\u6a21\u5757\u5217\u8868");
        INSTANCE = this;
        Aevum.EVENT_BUS.subscribe(new InitHandler());
    }

    private boolean isRightSide() {
        return this.alignMode == HudElement.AlignMode.RIGHT;
    }

    private int getColor(double counter) {
        if (this.colorMode.getValue() == ColorMode.Pulse) {
            return this.endColor.booleanValue ? ColorUtil.pulseColor(this.color.getValue(), this.endColor.getValue(), counter, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue()).getRGB() : ColorUtil.pulseColor(this.color.getValue(), counter, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue()).getRGB();
        }
        if (this.colorMode.getValue() == ColorMode.Rainbow) {
            double state = Math.ceil(((double)System.currentTimeMillis() * this.rainbowSpeed.getValue() + counter * this.rainbowDelay.getValue()) / 20.0);
            return Color.getHSBColor((float)(state % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0f, 1.0f).getRGB();
        }
        return this.color.getValue().getRGB();
    }

    @EventListener(priority=-999)
    public void onTick(ClientTickEvent event) {
        if (ArrayListHud.nullCheck() || !this.initialized) {
            return;
        }
        if (event.isPost()) {
            for (ModuleInfo info : this.moduleList) {
                info.update();
            }
            if (this.sortByLength.getValue()) {
                this.moduleList.sort(Comparator.comparingInt(i -> i.displayText == null ? 0 : -((int)FontManager.getWidth(i.displayText))));
            }
            if (this.listGlow.getValue()) {
                this.cleanupStaleListGlowEntries();
            }
        }
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
        if (this.alignMode == HudElement.AlignMode.RIGHT) {
            return this.hudX - this.getElementWidth();
        }
        return this.hudX;
    }

    @Override
    public void setElementX(int x) {
        this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? x + this.getElementWidth() : x;
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
        int maxW = 60;
        for (ModuleInfo info : this.moduleList) {
            int w;
            if (info.displayText == null || !info.isOn && !(info.animX > 0.0) || (w = (int)FontManager.getWidth(info.displayText)) <= maxW) continue;
            maxW = w;
        }
        return maxW + 4;
    }

    @Override
    public int getElementHeight() {
        int count = 0;
        for (ModuleInfo info : this.moduleList) {
            if (!info.isOn && !(info.animX > 0.0)) continue;
            ++count;
        }
        return Math.max(20, (int)(((double)FontManager.getFontHeight() + this.interval.getValue()) * (double)Math.max(count, 1)));
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public HudElement.AlignMode getAlignMode() {
        return this.alignMode;
    }

    @Override
    public void setAlignMode(HudElement.AlignMode mode) {
        if (mode == this.alignMode) {
            return;
        }
        if (mode == HudElement.AlignMode.RIGHT && this.alignMode == HudElement.AlignMode.LEFT) {
            this.hudX += this.getElementWidth();
        } else if (mode == HudElement.AlignMode.LEFT && this.alignMode == HudElement.AlignMode.RIGHT) {
            this.hudX -= this.getElementWidth();
        }
        this.alignMode = mode;
    }

    @Override
    public int getRightAnchorX() {
        if (this.alignMode == HudElement.AlignMode.RIGHT) {
            return this.hudX;
        }
        return this.hudX + this.getElementWidth();
    }

    @Override
    public void setRightAnchorX(int rightX) {
        this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? rightX : rightX - this.getElementWidth();
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        double currentY;
        if (!this.initialized) {
            return;
        }
        float fontHeight = FontManager.getFontHeight();
        boolean fromUp = this.renderUp.getValue();
        float anchorX = this.getElementX();
        int boxWidth = this.getElementWidth();
        boolean rightSide = this.isRightSide();
        if (fromUp) {
            currentY = (double)this.hudY + 1.0;
        } else {
            int visCount = 0;
            for (ModuleInfo info : this.moduleList) {
                if (!info.isOn && !(info.animX > 0.0)) continue;
                ++visCount;
            }
            currentY = (double)this.hudY + ((double)fontHeight + this.interval.getValue()) * (double)visCount - (double)fontHeight - 1.0;
        }
        double counter = 20.0;
        for (ModuleInfo info : this.moduleList) {
            if (info.animX <= 0.0 && !info.isOn) continue;
            double textWidth = FontManager.getWidth(info.displayText);
            info.animX = info.animation.get(info.isOn ? textWidth + 1.0 : 0.0, info.isOn ? (long)this.enableLength.getValueInt() : (long)this.disableLength.getValueInt(), this.easing.getValue());
            Animation animation = info.fadeAnimation;
            double d = info.isOn ? 1.0 : 0.0;
            double d2 = d;
            double fade = animation.get(d2, this.fadeLength.getValueInt(), this.easing.getValue());
            if (!(fade > 0.04)) continue;
            int c = ColorUtil.injectAlpha(this.getColor(counter += fromUp ? fade : -fade), (int)((double)this.color.getValue().getAlpha() * fade));
            double displayWidth = info.animX;
            float textX = rightSide ? anchorX + (float)boxWidth - (float)displayWidth : anchorX;
            float f = textX;
            if (this.backGround.getValue()) {
                float bgX = rightSide ? anchorX + (float)boxWidth - (float)displayWidth - this.bgWidth.getValueFloat() / 2.0f : anchorX - this.bgWidth.getValueFloat() / 2.0f;
                Render2DUtil.drawRect(context.method_51448(), bgX, (float)currentY - 1.0f - this.interval.getValueFloat() / 2.0f, (float)displayWidth + this.bgWidth.getValueFloat(), fontHeight + this.interval.getValueFloat(), ColorUtil.injectAlpha(this.bgColor.sync ? c : this.bgColor.getValue().getRGB(), (int)((double)this.bgColor.getValue().getAlpha() * fade)));
            }
            boolean textDrawnByGlow = false;
            if (this.listGlow.getValue() && info.displayText != null && !info.displayText.isEmpty()) {
                Color itemColor = new Color(c, true);
                textDrawnByGlow = this.renderListItemGlow(context, context.method_51448(), info.displayText, textX, (float)currentY, itemColor, (float)fade);
            }
            if (!textDrawnByGlow) {
                TextUtil.drawString(context, info.displayText, textX, (float)currentY, c, this.shadow.getValue());
            }
            if (this.rect.booleanValue) {
                float rectX = rightSide ? anchorX + (float)boxWidth + this.bgWidth.getValueFloat() / 2.0f : anchorX - this.bgWidth.getValueFloat() / 2.0f - 1.0f;
                Render2DUtil.drawRect(context.method_51448(), rectX, (float)currentY - 1.0f - this.interval.getValueFloat() / 2.0f, 1.0f, fontHeight + this.interval.getValueFloat(), this.rect.sync ? c : ColorUtil.injectAlpha(this.rect.getValue(), (int)((double)this.rect.getValue().getAlpha() * fade)).getRGB());
            }
            currentY += fromUp ? ((double)fontHeight + this.interval.getValue()) * fade : -((double)fontHeight + this.interval.getValue()) * fade;
        }
    }

    private void drawGlowTexture(GuiGraphics drawContext, PoseStack matrices, ResourceLocation texture, float screenX, float screenY, float screenW, float screenH, float alpha) {
        if (!SkijaGlowRenderer.isTextureReady(texture)) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        matrices.method_22903();
        matrices.method_46416(screenX, screenY, 0.0f);
        int w = (int)screenW;
        int h = (int)screenH;
        drawContext.method_25290(texture, 0, 0, 0.0f, 0.0f, w, h, w, h);
        matrices.method_22909();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private boolean renderListItemGlow(GuiGraphics drawContext, PoseStack matrices, String text, float x, float y, Color itemColor, float fade) {
        if (text == null || text.isEmpty() || fade < 0.05f) {
            return false;
        }
        String strippedText = FontRenderer.stripControlCodes(text);
        if (strippedText.isEmpty()) {
            return false;
        }
        float glowRadius = (float)this.listGlowRadius.getValue();
        float glowOpacity = (float)this.listGlowOpacity.getValue();
        Color textColor = itemColor;
        Color glowColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), Math.min(255, (int)((float)textColor.getAlpha() * glowOpacity)));
        return this.renderListItemSkijaGlow(drawContext, matrices, strippedText, x, y, textColor, glowColor, glowRadius, fade);
    }

    private boolean renderListItemSkijaGlow(GuiGraphics drawContext, PoseStack matrices, String text, float x, float y, Color textColor, Color glowColor, float glowRadius, float fade) {
        String cacheKey = String.format("list_%s_%.1f_%08x_%08x_%.2f_%s_%.1f", new Object[]{text, Float.valueOf(glowRadius), textColor.getRGB(), glowColor.getRGB(), this.listGlowOpacity.getValue(), FontManager.getMode(), Float.valueOf(FontManager.getSize())});
        ResourceLocation glowTexture = this.listGlowCache.get(cacheKey);
        Long lastUpdate = this.listGlowUpdateTime.get(cacheKey);
        boolean needsUpdate = glowTexture == null || lastUpdate == null || System.currentTimeMillis() - lastUpdate > 5000L;
        boolean bl = needsUpdate;
        if (needsUpdate) {
            float fontSize;
            ResourceLocation newTexture;
            if (glowTexture != null) {
                SkijaGlowRenderer.cleanupTexture(glowTexture);
            }
            if ((newTexture = SkijaGlowRenderer.createGlowTexture(text, fontSize = FontManager.isCustom() ? FontManager.getSize() : 9.0f, textColor, glowColor, glowRadius, false, null, null)) != null) {
                this.listGlowCache.put(cacheKey, newTexture);
                this.listGlowUpdateTime.put(cacheKey, System.currentTimeMillis());
                glowTexture = newTexture;
            } else {
                return false;
            }
        }
        if (glowTexture != null && SkijaGlowRenderer.isTextureReady(glowTexture)) {
            float textWidth = FontManager.getWidth(text);
            float textHeight = FontManager.getFontHeight();
            float glowPadding = glowRadius * 3.0f;
            this.drawGlowTexture(drawContext, matrices, glowTexture, x - glowPadding, y - glowPadding, textWidth + glowPadding * 2.0f, textHeight + glowPadding * 2.0f, fade);
            return true;
        }
        return false;
    }

    private void cleanupStaleListGlowEntries() {
        long now = System.currentTimeMillis();
        this.listGlowUpdateTime.entrySet().removeIf(entry -> {
            if (now - (Long)entry.getValue() > 30000L) {
                ResourceLocation id = this.listGlowCache.remove(entry.getKey());
                if (id != null) {
                    SkijaGlowRenderer.cleanupTexture(id);
                }
                return true;
            }
            return false;
        });
    }

    private static enum ColorMode {
        Custom,
        Pulse,
        Rainbow;

    }

    public class InitHandler {
        @EventListener
        public void onInit(InitEvent event) {
            ArrayListHud.this.moduleList.clear();
            for (Module m : Aevum.MODULE.getModules()) {
                ArrayListHud.this.moduleList.add(new ModuleInfo(m));
            }
            ArrayListHud.this.initialized = true;
            Aevum.EVENT_BUS.unsubscribe(this);
        }
    }

    private class ModuleInfo {
        final Module module;
        String displayText;
        boolean isOn;
        double animX = 0.0;
        final Animation animation = new Animation();
        final Animation fadeAnimation = new Animation();

        ModuleInfo(Module module) {
            this.module = module;
            this.displayText = module.getArrayName();
        }

        void update() {
            this.isOn = this.module.isOn() && this.module.drawn.getValue();
            boolean bl = this.isOn;
            if (this.isOn) {
                Object text = this.module.getArrayName();
                if (ArrayListHud.this.showSpace.getValue() && ((String)(text = ((String)text).replaceAll("([a-z])([A-Z])", "$1 $2"))).startsWith(" ")) {
                    text = ((String)text).replaceFirst(" ", "");
                }
                if (ArrayListHud.this.lowerCase.getValue()) {
                    text = ((String)text).toLowerCase();
                }
                if (ArrayListHud.this.forgeHax.getValue()) {
                    text = (String)text + "\u00a7r<";
                }
                this.displayText = text;
            }
        }
    }
}

