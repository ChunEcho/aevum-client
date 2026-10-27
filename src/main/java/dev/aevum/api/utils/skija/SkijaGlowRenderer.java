/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImage$Format
 *  net.minecraft.DynamicTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.ResourceLocation
 *  net.minecraft.Minecraft
 *  net.minecraft.PoseStack
 */
package dev.aevum.api.utils.skija;

import dev.aevum.core.impl.FontManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.NativeImage;
import net.minecraft.DynamicTexture;
import net.minecraft.AbstractTexture;
import net.minecraft.ResourceLocation;
import net.minecraft.Minecraft;
import net.minecraft.PoseStack;

public final class SkijaGlowRenderer {
    private static final Map<ResourceLocation, DynamicTexture> TEXTURES = new ConcurrentHashMap<ResourceLocation, DynamicTexture>();
    private static final Map<ResourceLocation, int[]> SIZES = new ConcurrentHashMap<ResourceLocation, int[]>();
    private static int counter = 0;
    private static boolean disabled = false;

    private SkijaGlowRenderer() {
    }

    public static boolean isAvailable() {
        return !disabled;
    }

    public static void disable(Throwable t) {
        disabled = true;
    }

    public static ResourceLocation createGlowTexture(String text, float fontSize, Color textColor, Color glowColor, float glowRadius, boolean useGradient, Color gradientEndColor) {
        return SkijaGlowRenderer.createGlowTexture(text, fontSize, textColor, glowColor, glowRadius, useGradient, gradientEndColor, null);
    }

    public static ResourceLocation createGlowTexture(String text, float fontSize, Color textColor, Color glowColor, float glowRadius, boolean useGradient, Color gradientEndColor, String forceFontFile) {
        if (disabled || text == null || text.isEmpty()) {
            return null;
        }
        try {
            int pad = Math.max(2, (int)Math.ceil((double)glowRadius * 3.0));
            Font font = SkijaGlowRenderer.pickFont(forceFontFile, (int)Math.max(1.0f, fontSize));
            BufferedImage measure = new BufferedImage(1, 1, 2);
            Graphics2D mg = measure.createGraphics();
            mg.setFont(font);
            FontMetrics fm = mg.getFontMetrics();
            int textWidth = Math.max(1, fm.stringWidth(text));
            int textHeight = Math.max(1, fm.getHeight());
            mg.dispose();
            int width = textWidth + pad * 2;
            int height = textHeight + pad * 2;
            BufferedImage glowLayer = new BufferedImage(width, height, 2);
            Graphics2D g = glowLayer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            g.setColor(glowColor);
            g.drawString(text, pad, pad + fm.getAscent());
            g.dispose();
            BufferedImage out = glowRadius > 0.0f ? SkijaGlowRenderer.blur(glowLayer, glowRadius) : glowLayer;
            Graphics2D c = out.createGraphics();
            c.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            c.setFont(font);
            if (useGradient && gradientEndColor != null) {
                c.setPaint(new GradientPaint(0.0f, 0.0f, textColor, 0.0f, height, gradientEndColor));
            } else {
                c.setColor(textColor);
            }
            c.drawString(text, pad, pad + fm.getAscent());
            c.dispose();
            return SkijaGlowRenderer.register(out);
        }
        catch (Throwable t) {
            SkijaGlowRenderer.disable(t);
            return null;
        }
    }

    public static ResourceLocation createArcTexture(float radius, float thickness, float startAngle, float sweepAngle, Color color, float glowRadius, int glowIntensity) {
        if (disabled) {
            return null;
        }
        try {
            float r = Math.max(1.0f, radius);
            float t = Math.max(1.0f, thickness);
            float extra = Math.max(0.0f, glowRadius) * 2.5f + 4.0f;
            int size = (int)Math.ceil((r + t / 2.0f + extra) * 2.0f);
            BufferedImage layer = new BufferedImage(size, size, 2);
            Graphics2D g = layer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(t, 1, 1));
            g.setColor(color);
            float cx = (float)size / 2.0f;
            float cy = (float)size / 2.0f;
            g.draw(new Arc2D.Float(cx - r, cy - r, r * 2.0f, r * 2.0f, -startAngle - sweepAngle, sweepAngle, 0));
            g.dispose();
            BufferedImage out = glowRadius > 0.0f ? SkijaGlowRenderer.blur(layer, glowRadius) : layer;
            return SkijaGlowRenderer.register(out);
        }
        catch (Throwable t2) {
            SkijaGlowRenderer.disable(t2);
            return null;
        }
    }

    public static ResourceLocation createGlowDiamondTexture(float size, Color innerColor, Color glowColor, float glowRadius) {
        if (disabled) {
            return null;
        }
        try {
            float s = Math.max(2.0f, size);
            float extra = Math.max(0.0f, glowRadius) * 2.5f + 4.0f;
            int px = (int)Math.ceil((s + extra) * 2.0f);
            BufferedImage layer = new BufferedImage(px, px, 2);
            Graphics2D g = layer.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float cx = (float)px / 2.0f;
            float cy = (float)px / 2.0f;
            Path2D.Float path = new Path2D.Float();
            path.moveTo(cx, cy - s);
            path.lineTo(cx + s, cy);
            path.lineTo(cx, cy + s);
            path.lineTo(cx - s, cy);
            path.closePath();
            g.setColor(glowColor);
            g.fill(path);
            g.dispose();
            BufferedImage out = glowRadius > 0.0f ? SkijaGlowRenderer.blur(layer, glowRadius) : layer;
            Graphics2D c = out.createGraphics();
            c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            c.setColor(innerColor);
            c.fill(path);
            c.dispose();
            return SkijaGlowRenderer.register(out);
        }
        catch (Throwable t) {
            SkijaGlowRenderer.disable(t);
            return null;
        }
    }

    public static boolean isTextureReady(ResourceLocation id) {
        return id != null && TEXTURES.containsKey(id);
    }

    public static int[] getTextureSize(ResourceLocation id) {
        int[] arr = SIZES.get(id);
        if (arr == null) {
            return new int[]{0, 0};
        }
        return arr;
    }

    public static void renderSimpleGlow(PoseStack matrices, String text, float x, float y, Color color, float scale, float glowRadius, int layers, boolean shadow) {
        if (FontManager.ui == null) {
            return;
        }
        FontManager.draw(matrices, text, x, y, color.getRGB(), shadow);
    }

    public static void cleanupTexture(ResourceLocation id) {
        if (id == null) {
            return;
        }
        TEXTURES.remove(id);
        SIZES.remove(id);
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getTextureManager() != null) {
                mc.getTextureManager().method_4615(id);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void clearAllTextures() {
        for (ResourceLocation id : new ArrayList<ResourceLocation>(TEXTURES.keySet())) {
            SkijaGlowRenderer.cleanupTexture(id);
        }
    }

    private static Font pickFont(String forceFontFile, int size) {
        if (forceFontFile != null && !forceFontFile.isEmpty()) {
            try {
                Object path = forceFontFile.startsWith("assets/") ? forceFontFile : "assets/aevum/font/" + forceFontFile;
                InputStream in = SkijaGlowRenderer.class.getClassLoader().getResourceAsStream((String)path);
                if (in != null) {
                    return Font.createFont(0, in).deriveFont((float)size);
                }
            }
            catch (Throwable path) {
                // empty catch block
            }
        }
        try {
            Font f = new Font("Microsoft YaHei", 0, size);
            if (f.getFamily() != null) {
                return f;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new Font("SansSerif", 0, size);
    }

    private static BufferedImage blur(BufferedImage src, float radius) {
        int r = Math.max(1, Math.min(12, (int)Math.ceil(radius)));
        int k = r * 2 + 1;
        float[] kernel = new float[k * k];
        float sigma = Math.max(0.1f, radius / 2.0f);
        float sum = 0.0f;
        for (int y = -r; y <= r; ++y) {
            for (int x = -r; x <= r; ++x) {
                float v;
                kernel[(y + r) * k + (x + r)] = v = (float)Math.exp(-((double)(x * x + y * y)) / (double)(2.0f * sigma * sigma));
                sum += v;
            }
        }
        for (int i = 0; i < kernel.length; ++i) {
            kernel[i] = kernel[i] / sum;
        }
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), 2);
        new ConvolveOp(new Kernel(k, k, kernel), 1, null).filter(src, dst);
        return dst;
    }

    private static ResourceLocation register(BufferedImage img) {
        try {
            int w = img.getWidth();
            int h = img.getHeight();
            NativeImage ni = new NativeImage(NativeImage.NativeImage$Format.RGBA, w, h, false);
            int[] row = new int[w];
            for (int y = 0; y < h; ++y) {
                img.getRGB(0, y, w, 1, row, 0, w);
                for (int x = 0; x < w; ++x) {
                    int argb = row[x];
                    int a = argb >>> 24 & 0xFF;
                    int rr = argb >> 16 & 0xFF;
                    int gg = argb >> 8 & 0xFF;
                    int bb = argb & 0xFF;
                    int abgr = a << 24 | bb << 16 | gg << 8 | rr;
                    ni.method_4305(x, y, abgr);
                }
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                ni.close();
                return null;
            }
            ResourceLocation id = ResourceLocation.method_60655((String)"aevum", (String)("skija/glow_" + counter++));
            DynamicTexture tex = new DynamicTexture(ni);
            mc.getTextureManager().method_4616(id, (AbstractTexture)tex);
            TEXTURES.put(id, tex);
            SIZES.put(id, new int[]{w, h});
            return id;
        }
        catch (Throwable t) {
            return null;
        }
    }
}

