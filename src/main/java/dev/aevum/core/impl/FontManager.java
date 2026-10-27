/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Minecraft
 *  net.minecraft.Font$DisplayMode
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.MultiBufferSource$BufferSource
 */
package dev.aevum.core.impl;

import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.mod.gui.fonts.FontRenderer;
import dev.aevum.mod.modules.impl.client.Fonts;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.Minecraft;
import net.minecraft.Font;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;

public class FontManager {
    public static FontRenderer ui;
    public static FontRenderer small;
    public static FontRenderer icon;
    public static boolean available;
    private static final HashMap<Float, FontRenderer> FONT_SIZE_CACHE;

    public static boolean isCustomFontEnabled() {
        return available && Fonts.INSTANCE != null && Fonts.INSTANCE.isOn();
    }

    public static boolean isShadowEnabled() {
        return Fonts.INSTANCE == null || Fonts.INSTANCE.shadow.getValue();
    }

    public static void init() {
        try {
            ui = FontManager.assets(8.0f, "default", 0);
            small = FontManager.assets(6.0f, "default", 0);
            icon = FontManager.assetsWithoutOffset(8.0f, "icon", 0);
            available = true;
        }
        catch (Throwable e) {
            available = false;
            ui = null;
            small = null;
            icon = null;
            System.out.println("[Aevum] AWT font unavailable, fallback to vanilla font: " + String.valueOf(e));
        }
    }

    public static boolean custom() {
        return available && FontManager.isCustomFontEnabled();
    }

    private static void vanilla(PoseStack st, String text, float x, float y, int color, boolean shadow) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null) {
            return;
        }
        MultiBufferSource.MultiBufferSource$BufferSource vcp = mc.renderBuffers().bufferSource();
        mc.font.method_27521(text, x, y, color, shadow, st.last().method_23761(), (MultiBufferSource)vcp, Font.Font$DisplayMode.NORMAL, 0, 0xF000F0);
        vcp.endBatch();
    }

    public static void draw(PoseStack st, String text, double x, double y, int color, boolean shadow) {
        if (FontManager.custom() && ui != null) {
            ui.drawString(st, text, x, y, color, shadow);
        } else {
            FontManager.vanilla(st, text, (float)x, (float)y, color, shadow);
        }
    }

    public static void draw(PoseStack st, String text, double x, double y, int color) {
        FontManager.draw(st, text, x, y, color, true);
    }

    public static void drawSmall(PoseStack st, String text, double x, double y, int color, boolean shadow) {
        if (FontManager.custom() && small != null) {
            small.drawString(st, text, x, y, color);
        } else {
            FontManager.vanilla(st, text, (float)x, (float)y, color, shadow);
        }
    }

    public static void drawSmall(PoseStack st, String text, double x, double y, int color) {
        if (FontManager.custom() && small != null) {
            small.drawString(st, text, x, y, color);
        } else {
            FontManager.vanilla(st, text, (float)x, (float)y, color, true);
        }
    }

    public static void drawCentered(PoseStack st, String text, double x, double y, int color) {
        if (FontManager.custom() && ui != null) {
            ui.drawCenteredString(st, text, x, y, color);
        } else {
            int w = (int)TextUtil.getWidth(text);
            FontManager.vanilla(st, text, (float)x - (float)w / 2.0f, (float)y, color, true);
        }
    }

    public static void drawIcon(PoseStack st, String text, double x, double y, int color) {
        if (icon != null) {
            icon.drawString(st, text, x, y, color);
        } else {
            FontManager.vanilla(st, text, (float)x, (float)y, color, false);
        }
    }

    public static int width(String text) {
        return FontManager.custom() && ui != null ? (int)ui.getWidth(text) : (int)TextUtil.getWidth(text);
    }

    public static float height() {
        return FontManager.custom() && ui != null ? ui.getFontHeight() : TextUtil.getHeight();
    }

    public static float iconWidth(String text) {
        return icon != null ? icon.getWidth(text) : TextUtil.getWidth(text);
    }

    public static float iconHeight(String text) {
        return icon != null ? icon.getFontHeight(text) : TextUtil.getHeight();
    }

    public static FontRenderer assets(float size, String font, int style, String alternate) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/aevum/font/" + font + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), FontManager.getFont(alternate, style, (int)size), size){

            @Override
            public void drawString(PoseStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = Fonts.INSTANCE.translate.getValueInt();
                    dy = Fonts.INSTANCE.shift.getValueInt();
                }
                super.drawString(stack, s, x + dx, y + dy, r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer assetsWithoutOffset(float size, String name, int style) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/aevum/icon/" + name + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), size);
    }

    public static FontRenderer assets(float size, String name, int style) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/aevum/font/" + name + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), size){

            @Override
            public void drawString(PoseStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = Fonts.INSTANCE.translate.getValueInt();
                    dy = Fonts.INSTANCE.shift.getValueInt();
                }
                super.drawString(stack, s, x + dx, y + dy, r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer create(int size, String font, int style, String alternate) {
        return new FontRenderer(FontManager.getFont(font, style, size), FontManager.getFont(alternate, style, size), size){

            @Override
            public void drawString(PoseStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = Fonts.INSTANCE.translate.getValueInt();
                    dy = Fonts.INSTANCE.shift.getValueInt();
                }
                super.drawString(stack, s, x + dx, y + dy, r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer create(int size, String font, int style) {
        return new FontRenderer(FontManager.getFont(font, style, size), size){

            @Override
            public void drawString(PoseStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = Fonts.INSTANCE.translate.getValueInt();
                    dy = Fonts.INSTANCE.shift.getValueInt();
                }
                super.drawString(stack, s, x + dx, y + dy, r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    private static Font getFont(String font, int style, int size) {
        File fontDir = new File("C:\\Windows\\Fonts");
        try {
            for (File file : fontDir.listFiles()) {
                if (!file.getName().replace(".ttf", "").replace(".ttc", "").replace(".otf", "").equalsIgnoreCase(font)) continue;
                try {
                    return Font.createFont(0, file).deriveFont(style, size);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (File file : fontDir.listFiles()) {
                if (!file.getName().startsWith(font)) continue;
                try {
                    return Font.createFont(0, file).deriveFont(style, size);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new Font(null, style, size);
    }

    public static FontRenderer getSmall() {
        return small;
    }

    public static boolean isCustom() {
        return FontManager.isCustomFontEnabled();
    }

    public static Fonts.FontMode getMode() {
        return Fonts.INSTANCE != null && Fonts.INSTANCE.isOn() ? Fonts.FontMode.CUSTOM : Fonts.FontMode.MINECRAFT;
    }

    public static float getSize() {
        return Fonts.INSTANCE != null ? (float)Fonts.INSTANCE.size.getValue() : 8.0f;
    }

    public static float getWidth(String text) {
        return ui == null ? 0.0f : ui.getWidth(text);
    }

    public static float getFontHeight() {
        return ui == null ? 9.0f : ui.getFontHeight();
    }

    public static FontRenderer get() {
        return ui;
    }

    public static FontRenderer get(float size) {
        return FONT_SIZE_CACHE.computeIfAbsent(Float.valueOf(size), s -> {
            try {
                return FontManager.assets(s.floatValue(), "default", 0);
            }
            catch (Throwable t) {
                return ui;
            }
        });
    }

    public static FontRenderer getNotificationFont() {
        return ui;
    }

    public static FontRenderer getNotificationFont(float size) {
        return FontManager.get(size);
    }

    public static FontRenderer getOxaniumFont(float size) {
        try {
            return FontManager.assets(size, "oxanium-light", 0);
        }
        catch (Throwable t) {
            return FontManager.get(size);
        }
    }

    public static FontRenderer getOrbitronFont(float size) {
        try {
            return FontManager.assets(size, "orbitron", 0);
        }
        catch (Throwable t) {
            return FontManager.get(size);
        }
    }

    public static FontRenderer getArtistFont(float size) {
        return FontManager.get(size);
    }

    static {
        available = false;
        FONT_SIZE_CACHE = new HashMap();
    }
}

