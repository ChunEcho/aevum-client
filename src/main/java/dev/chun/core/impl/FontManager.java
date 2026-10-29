/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package dev.chun.core.impl;

import dev.chun.mod.gui.fonts.FontRenderer;
import dev.chun.mod.modules.impl.client.Fonts;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.client.util.math.MatrixStack;

public class FontManager {
    public static FontRenderer ui;
    public static FontRenderer small;
    public static FontRenderer icon;
    public static boolean available = false;
    private static final java.util.HashMap<Float, FontRenderer> FONT_SIZE_CACHE = new java.util.HashMap<>();

    public static boolean isCustomFontEnabled() {
        return Fonts.INSTANCE != null && Fonts.INSTANCE.isOn();
    }

    public static boolean isShadowEnabled() {
        return Fonts.INSTANCE == null || Fonts.INSTANCE.shadow.getValue();
    }

    public static void init() {
        try {
            ui = FontManager.assets(8.0f, "default", 0);
            small = FontManager.assets(6.0f, "default", 0);
            icon = FontManager.assetsWithoutOffset(8.0f, "icon", 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
            available = true;
}

    public static FontRenderer assets(float size, String font, int style, String alternate) throws IOException, FontFormatException {
        ClassLoader classLoader = FontManager.class.getClassLoader();
        InputStream primary = classLoader.getResourceAsStream("assets/aevumclient/font/" + font + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), FontManager.getFont(alternate, style, (int)size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = (float)Fonts.INSTANCE.translate.getValueInt();
                    dy = (float)Fonts.INSTANCE.shift.getValueInt();
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
        InputStream primary = classLoader.getResourceAsStream("assets/aevumclient/font/" + name + ".ttf");
        InputStream fallback = classLoader.getResourceAsStream("assets/minecraft/font/font.ttf");
        InputStream stream = primary != null ? primary : fallback;
        return new FontRenderer(Font.createFont(0, Objects.requireNonNull(stream)).deriveFont(style, size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = (float)Fonts.INSTANCE.translate.getValueInt();
                    dy = (float)Fonts.INSTANCE.shift.getValueInt();
                }
                super.drawString(stack, s, x + dx, y + dy, r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer create(int size, String font, int style, String alternate) {
        return new FontRenderer(FontManager.getFont(font, style, size), FontManager.getFont(alternate, style, size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = (float)Fonts.INSTANCE.translate.getValueInt();
                    dy = (float)Fonts.INSTANCE.shift.getValueInt();
                }
                super.drawString(stack, s, x + dx, y + dy, r, g, elementCodec, keyCodec, shadow);
            }
        };
    }

    public static FontRenderer create(int size, String font, int style) {
        return new FontRenderer(FontManager.getFont(font, style, size), size){

            @Override
            public void drawString(MatrixStack stack, String s, float x, float y, float r, float g, float elementCodec, float keyCodec, boolean shadow) {
                float dx = 0.0f;
                float dy = 0.0f;
                if (FontManager.isCustomFontEnabled()) {
                    dx = (float)Fonts.INSTANCE.translate.getValueInt();
                    dy = (float)Fonts.INSTANCE.shift.getValueInt();
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

    public static boolean custom() {
        return available && isCustomFontEnabled();
    }
    public static FontRenderer getSmall() {
        return small;
    }
    public static boolean isCustom() {
        return isCustomFontEnabled();
    }
    public static Fonts.FontMode getMode() {
        return Fonts.INSTANCE != null && Fonts.INSTANCE.isOn() ? Fonts.FontMode.CUSTOM : Fonts.FontMode.MINECRAFT;
    }
    public static float getSize() {
        return Fonts.INSTANCE != null ? (float)Fonts.INSTANCE.size.getValue() : 8.0F;
    }
    public static float getWidth(String text) {
        return ui == null ? 0.0F : ui.getWidth(text);
    }
    public static float getFontHeight() {
        return ui == null ? 9.0F : ui.getFontHeight();
    }
    public static FontRenderer get() {
        return ui;
    }
    public static FontRenderer get(float size) {
        return FONT_SIZE_CACHE.computeIfAbsent(size, s2 -> {
            try {
                return assets(s2, "default", 0);
            } catch (Throwable t) {
                return ui;
            }
        });
    }
    public static FontRenderer getNotificationFont() {
        return ui;
    }
    public static FontRenderer getNotificationFont(float size) {
        return get(size);
    }
    public static FontRenderer getOxaniumFont(float size) {
        try {
            return assets(size, "oxanium-light", 0);
        } catch (Throwable t) {
            return get(size);
        }
    }
    public static FontRenderer getOrbitronFont(float size) {
        try {
            return assets(size, "orbitron", 0);
        } catch (Throwable t) {
            return get(size);
        }
    }
    public static FontRenderer getArtistFont(float size) {
        return get(size);
    }

    public static void draw(MatrixStack st, String text, double x, double y, int color, boolean shadow) {
        if (custom()) {
            ui.drawString(st, text, (float)x, (float)y, color, shadow);
        } else {
            vanilla(st, text, (float)x, (float)y, color, shadow);
        }
    }

    public static void draw(MatrixStack st, String text, double x, double y, int color) {
        draw(st, text, x, y, color, isShadowEnabled());
    }

    private static void vanilla(MatrixStack st, String text, float x, float y, int color, boolean shadow) {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.textRenderer != null) {
            mc.textRenderer.draw(text, x, y, color, shadow, st.peek().getPositionMatrix(),
                mc.getBufferBuilders().getEntityVertexConsumers(),
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            mc.getBufferBuilders().getEntityVertexConsumers().draw();
        }
    }
}
