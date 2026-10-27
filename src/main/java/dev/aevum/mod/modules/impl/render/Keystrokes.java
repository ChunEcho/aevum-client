/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Component
 *  net.minecraft.KeyMapping
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.Render2DEvent;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.Component;
import net.minecraft.KeyMapping;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class Keystrokes
extends Module {
    public static Keystrokes INSTANCE;
    public final SliderSetting posX = this.add(new SliderSetting("PosX", 26.0, 0.0, 1920.0, 1.0));
    public final SliderSetting posY = this.add(new SliderSetting("PosY", 96.0, 0.0, 1080.0, 1.0));
    public final SliderSetting keyWidth = this.add(new SliderSetting("KeyWidth", 52.0, 20.0, 90.0, 1.0));
    public final SliderSetting keyHeight = this.add(new SliderSetting("KeyHeight", 34.0, 16.0, 60.0, 1.0));
    public final SliderSetting gap = this.add(new SliderSetting("Gap", 2.0, 0.0, 12.0, 1.0));
    private final SliderSetting radius = this.add(new SliderSetting("Radius", 2.0, 0.0, 12.0, 1.0));
    private final SliderSetting activeGlow = this.add(new SliderSetting("Glow", 10.0, 0.0, 20.0, 1.0));
    private final ColorSetting idleColor = this.add(new ColorSetting("IdleColor", new Color(189, 153, 255, 170)));
    private final ColorSetting idleStroke = this.add(new ColorSetting("IdleStroke", new Color(189, 153, 255, 34)));
    private final ColorSetting textColor = this.add(new ColorSetting("TextColor", new Color(189, 153, 255, 255)));
    private final KeyVisual forwardVisual = new KeyVisual();
    private final KeyVisual leftVisual = new KeyVisual();
    private final KeyVisual backVisual = new KeyVisual();
    private final KeyVisual rightVisual = new KeyVisual();
    private final KeyVisual leftMouseVisual = new KeyVisual();
    private final KeyVisual rightMouseVisual = new KeyVisual();
    private final KeyVisual jumpVisual = new KeyVisual();

    public Keystrokes() {
        super("Keystrokes", Module.Category.Render);
        this.setChinese("\u6309\u952e\u663e\u793a");
        INSTANCE = this;
    }

    @EventListener
    public void onRender2D(Render2DEvent render2DEvent) {
        if (Keystrokes.mc.player == null || Keystrokes.mc.level == null || Keystrokes.mc.options.hideGui) {
            return;
        }
        GuiGraphics drawContext = render2DEvent.drawContext;
        PoseStack getMatrices = render2DEvent.drawContext.method_51448();
        float valueFloat = this.posX.getValueFloat();
        float valueFloat2 = this.posY.getValueFloat();
        float valueFloat3 = this.keyWidth.getValueFloat();
        float valueFloat4 = this.keyHeight.getValueFloat();
        float valueFloat5 = this.gap.getValueFloat();
        float n = valueFloat2 + valueFloat4 + valueFloat5;
        float n2 = n + valueFloat4 + valueFloat5;
        float n3 = n2 + valueFloat4 + valueFloat5;
        this.update(this.forwardVisual, Keystrokes.mc.options.keyUp);
        this.update(this.leftVisual, Keystrokes.mc.options.keyLeft);
        this.update(this.backVisual, Keystrokes.mc.options.keyDown);
        this.update(this.rightVisual, Keystrokes.mc.options.keyRight);
        this.update(this.leftMouseVisual, Keystrokes.mc.options.keyAttack);
        this.update(this.rightMouseVisual, Keystrokes.mc.options.keyUse);
        this.update(this.jumpVisual, Keystrokes.mc.options.keyJump);
        this.drawKey(drawContext, getMatrices, valueFloat + valueFloat3 + valueFloat5, valueFloat2, valueFloat3, valueFloat4, "W", this.forwardVisual);
        this.drawKey(drawContext, getMatrices, valueFloat, n, valueFloat3, valueFloat4, "A", this.leftVisual);
        this.drawKey(drawContext, getMatrices, valueFloat + valueFloat3 + valueFloat5, n, valueFloat3, valueFloat4, "S", this.backVisual);
        this.drawKey(drawContext, getMatrices, valueFloat + (valueFloat3 + valueFloat5) * 2.0f, n, valueFloat3, valueFloat4, "D", this.rightVisual);
        this.drawKey(drawContext, getMatrices, valueFloat, n2, valueFloat3 * 1.5f + valueFloat5 * 0.5f, valueFloat4, "LMB", this.leftMouseVisual);
        this.drawKey(drawContext, getMatrices, valueFloat + valueFloat3 * 1.5f + valueFloat5 * 1.5f, n2, valueFloat3 * 1.5f + valueFloat5 * 0.5f, valueFloat4, "RMB", this.rightMouseVisual);
        this.drawKey(drawContext, getMatrices, valueFloat, n3, valueFloat3 * 3.0f + valueFloat5 * 2.0f, valueFloat4, "SPACE", this.jumpVisual);
    }

    private void update(KeyVisual keyVisual, KeyMapping KeyBinding) {
        keyVisual.progress = keyVisual.progress + ((KeyBinding.isDown() ? 1.0f : 0.0f) - keyVisual.progress) * 0.22f;
    }

    private void drawKey(GuiGraphics DrawContext, PoseStack NarratorManager, float n, float n2, float n3, float n4, String s, KeyVisual keyVisual) {
        Color themeColor = this.getThemeColor();
        float clamp = this.clamp(keyVisual.progress);
        float min = Math.min(this.radius.getValueFloat(), n4 / 2.0f);
        Color blend = this.blend(new Color(38, 56, 92, 220), ColorUtil.injectAlpha(themeColor.brighter(), 255), clamp);
        Color color = clamp > 0.01f ? blend : this.idleColor.getValue();
        Color color2 = clamp > 0.01f ? ColorUtil.injectAlpha(Color.WHITE, (int)(100.0f + 90.0f * clamp)) : this.idleStroke.getValue();
        Render2DUtil.drawRoundedRect(NarratorManager, n, n2, n3, n4, min, color);
        Render2DUtil.drawRoundedStroke(NarratorManager, n, n2, n3, n4, min, color2, 18);
        if (clamp > 0.01f) {
            float n5 = this.activeGlow.getValueFloat() * clamp;
            Render2DUtil.drawRoundedRect(NarratorManager, n - n5 * 0.18f, n2 - n5 * 0.18f, n3 + n5 * 0.36f, n4 + n5 * 0.36f, min + n5 * 0.12f, ColorUtil.injectAlpha(themeColor, (int)(95.0f * clamp)));
            Render2DUtil.drawRoundedRect(NarratorManager, n + 1.0f, n2 + 1.0f, n3 - 2.0f, Math.max(2.0f, n4 * 0.22f), Math.max(1.0f, min - 1.0f), new Color(255, 255, 255, (int)(22.0f * clamp)));
        }
        DrawContext.method_51439(Keystrokes.mc.font, (Component)Component.method_43470((String)s), Math.round(n + (n3 - (float)Keystrokes.mc.font.method_1727(s)) / 2.0f), Math.round(n2 + (n4 - 8.0f) / 2.0f), this.textColor.getValue().getRGB(), false);
    }

    private Color getThemeColor() {
        return new Color(95, 155, 255, 255);
    }

    private float clamp(float b) {
        return Math.max(0.0f, Math.min(1.0f, b));
    }

    private Color blend(Color color, Color color2, float n) {
        float clamp = this.clamp(n);
        return new Color((int)((float)color.getRed() + (float)(color2.getRed() - color.getRed()) * clamp), (int)((float)color.getGreen() + (float)(color2.getGreen() - color.getGreen()) * clamp), (int)((float)color.getBlue() + (float)(color2.getBlue() - color.getBlue()) * clamp), (int)((float)color.getAlpha() + (float)(color2.getAlpha() - color.getAlpha()) * clamp));
    }

    private static class KeyVisual {
        private float progress;

        private KeyVisual() {
        }
    }
}

