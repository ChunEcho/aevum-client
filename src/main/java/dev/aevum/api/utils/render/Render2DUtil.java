/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.ResourceLocation
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 */
package dev.aevum.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.ColorUtil;
import java.awt.Color;
import java.util.function.DoubleFunction;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;

public class Render2DUtil
implements Wrapper {
    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color) {
        Render2DUtil.rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void arrow(PoseStack matrixStack, float x, float y, Color color) {
        Render2DUtil.drawRectWithOutline(matrixStack, x - 1.0f, y - 1.0f, 2.0f, 2.0f, color, Color.BLACK);
    }

    public static void rectFilled(PoseStack matrix, float x1, float y1, float x2, float y2, int color) {
        float i;
        float f = (float)(color >> 24 & 0xFF) / 255.0f;
        float g = (float)(color >> 16 & 0xFF) / 255.0f;
        float h = (float)(color >> 8 & 0xFF) / 255.0f;
        float j = (float)(color & 0xFF) / 255.0f;
        if ((double)f <= 0.01) {
            return;
        }
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix.last().method_23761(), x1, y2, 0.0f).method_22915(g, h, j, f);
        bufferBuilder.method_22918(matrix.last().method_23761(), x2, y2, 0.0f).method_22915(g, h, j, f);
        bufferBuilder.method_22918(matrix.last().method_23761(), x2, y1, 0.0f).method_22915(g, h, j, f);
        bufferBuilder.method_22918(matrix.last().method_23761(), x1, y1, 0.0f).method_22915(g, h, j, f);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void horizontalGradient(PoseStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, x1, y1, 0.0f).method_39415(startColor.getRGB());
        bufferBuilder.method_22918(matrix, x1, y2, 0.0f).method_39415(startColor.getRGB());
        bufferBuilder.method_22918(matrix, x2, y2, 0.0f).method_39415(endColor.getRGB());
        bufferBuilder.method_22918(matrix, x2, y1, 0.0f).method_39415(endColor.getRGB());
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void horizontalGradient(PoseStack matrices, float x1, float y1, float x2, float y2, int startColor, int endColor) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, x1, y1, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x1, y2, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x2, y2, 0.0f).method_39415(endColor);
        bufferBuilder.method_22918(matrix, x2, y1, 0.0f).method_39415(endColor);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void verticalGradient(PoseStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, x1, y1, 0.0f).method_39415(startColor.getRGB());
        bufferBuilder.method_22918(matrix, x2, y1, 0.0f).method_39415(startColor.getRGB());
        bufferBuilder.method_22918(matrix, x2, y2, 0.0f).method_39415(endColor.getRGB());
        bufferBuilder.method_22918(matrix, x1, y2, 0.0f).method_39415(endColor.getRGB());
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void verticalGradient(PoseStack matrices, float x1, float y1, float x2, float y2, int startColor, int endColor) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, x1, y1, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x2, y1, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x2, y2, 0.0f).method_39415(endColor);
        bufferBuilder.method_22918(matrix, x1, y2, 0.0f).method_39415(endColor);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void drawLine(PoseStack matrices, float x, float y, float x1, float y1, int color) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, x, y, 0.0f).method_39415(color);
        bufferBuilder.method_22918(matrix, x1, y1, 0.0f).method_39415(color);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void drawRectWithOutline(PoseStack matrices, float x, float y, float width, float height, Color c, Color c2) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, x, y + height, 0.0f).method_39415(c.getRGB());
        buffer.method_22918(matrix, x + width, y + height, 0.0f).method_39415(c.getRGB());
        buffer.method_22918(matrix, x + width, y, 0.0f).method_39415(c.getRGB());
        buffer.method_22918(matrix, x, y, 0.0f).method_39415(c.getRGB());
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, x, y + height, 0.0f).method_39415(c2.getRGB());
        buffer.method_22918(matrix, x + width, y + height, 0.0f).method_39415(c2.getRGB());
        buffer.method_22918(matrix, x + width, y, 0.0f).method_39415(c2.getRGB());
        buffer.method_22918(matrix, x, y, 0.0f).method_39415(c2.getRGB());
        buffer.method_22918(matrix, x, y + height, 0.0f).method_39415(c2.getRGB());
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.disableBlend();
    }

    public static void drawRect(PoseStack matrices, float x, float y, float width, float height, int c) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, x, y + height, 0.0f).method_39415(c);
        bufferBuilder.method_22918(matrix, x + width, y + height, 0.0f).method_39415(c);
        bufferBuilder.method_22918(matrix, x + width, y, 0.0f).method_39415(c);
        bufferBuilder.method_22918(matrix, x, y, 0.0f).method_39415(c);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static void drawRect(PoseStack matrices, float x, float y, float width, float height, Color c) {
        Render2DUtil.drawRect(matrices, x, y, width, height, c.getRGB());
    }

    public static void drawRect(GuiGraphics drawContext, float x, float y, float width, float height, Color c) {
        Render2DUtil.drawRect(drawContext.method_51448(), x, y, width, height, c);
    }

    public static void drawSegmentedRect(PoseStack matrices, float x, float y, float width, float height, float segment, DoubleFunction<Integer> colorAtY) {
        if (width <= 0.0f || height <= 0.0f || segment <= 0.0f) {
            return;
        }
        float y1 = y;
        float y2 = y + height;
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);
        for (float yy = top; yy < bottom; yy += segment) {
            float yy2 = Math.min(yy + segment, bottom);
            int c = colorAtY.apply(yy);
            Render2DUtil.drawRect(matrices, x, yy, width, yy2 - yy, c);
        }
    }

    public static void drawLutRect(PoseStack matrices, float x, float y, float width, float height, ResourceLocation texture, int lutHeight, int alpha) {
        if (width == 0.0f || height == 0.0f || lutHeight <= 0) {
            return;
        }
        float x1 = x;
        float x2 = x + width;
        float y1 = y;
        float y2 = y + height;
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);
        float denom = Math.max(1, lutHeight - 1);
        float vTop = Math.max(0.0f, Math.min(1.0f, top / denom));
        float vBottom = Math.max(0.0f, Math.min(1.0f, bottom / denom));
        float a = (float)Math.max(0, Math.min(255, alpha)) / 255.0f;
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34543);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)texture);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float u = 0.5f;
        bufferBuilder.method_22918(matrix, left, bottom, 0.0f).method_22913(u, vBottom).method_22915(1.0f, 1.0f, 1.0f, a);
        bufferBuilder.method_22918(matrix, right, bottom, 0.0f).method_22913(u, vBottom).method_22915(1.0f, 1.0f, 1.0f, a);
        bufferBuilder.method_22918(matrix, right, top, 0.0f).method_22913(u, vTop).method_22915(1.0f, 1.0f, 1.0f, a);
        bufferBuilder.method_22918(matrix, left, top, 0.0f).method_22913(u, vTop).method_22915(1.0f, 1.0f, 1.0f, a);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static void drawGlow(PoseStack matrices, float x, float y, float width, float height, int color) {
        Matrix4f matrix = matrices.last().method_23761();
        int startColor = ColorUtil.injectAlpha(color, 20);
        RenderSystem.setShader(GameRenderer::method_34540);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float centerX = x + halfWidth;
        float centerY = y + halfHeight;
        float x2 = x + width;
        float y2 = y + height;
        bufferBuilder.method_22918(matrix, centerX, centerY, 0.0f).method_39415(color);
        bufferBuilder.method_22918(matrix, x, centerY, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x, y, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, centerX, y, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, centerX, centerY, 0.0f).method_39415(color);
        bufferBuilder.method_22918(matrix, centerX, y, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x2, y, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x2, centerY, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, centerX, centerY, 0.0f).method_39415(color);
        bufferBuilder.method_22918(matrix, x, centerY, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x, y2, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, centerX, y2, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, centerX, centerY, 0.0f).method_39415(color);
        bufferBuilder.method_22918(matrix, x2, centerY, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, x2, y2, 0.0f).method_39415(startColor);
        bufferBuilder.method_22918(matrix, centerX, y2, 0.0f).method_39415(startColor);
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public static void drawCircle(PoseStack matrices, float cx, float cy, float r, Color c, int segments) {
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (int i = 0; i <= segments; ++i) {
            double keyCodec = (double)i * (Math.PI * 2) / (double)segments;
            float x = (float)((double)cx + Math.cos(keyCodec) * (double)r);
            float y = (float)((double)cy + Math.sin(keyCodec) * (double)r);
            bufferBuilder.method_22918(matrix, x, y, 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawPill(PoseStack matrices, float x, float y, float width, float height, Color c) {
        float r = height / 2.0f;
        Render2DUtil.drawRect(matrices, x + r, y, width - 2.0f * r, height, c);
        Render2DUtil.drawCircle(matrices, x + r, y + r, r, c, 64);
        Render2DUtil.drawCircle(matrices, x + width - r, y + r, r, c, 64);
    }

    public static void drawRoundedStroke(PoseStack matrices, float x, float y, float width, float height, float radius, Color c, int seg) {
        double keyCodec;
        int i;
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        float x1 = x + r;
        float y1 = y + r;
        float x2 = x + width - r;
        float y2 = y + height - r;
        for (i = 0; i <= seg; ++i) {
            keyCodec = -1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        for (i = 0; i <= seg; ++i) {
            keyCodec = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        for (i = 0; i <= seg; ++i) {
            keyCodec = 1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        for (i = 0; i <= seg; ++i) {
            keyCodec = Math.PI + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.disableBlend();
    }

    public static void drawRainbowRoundedStroke(PoseStack matrices, float x, float y, float width, float height, float radius, int seg, float speed, int alpha) {
        int argb;
        int rgb;
        int i;
        int argb2;
        int rgb2;
        float hue;
        double keyCodec;
        int i2;
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        float x1 = x + r;
        float y1 = y + r;
        float x2 = x + width - r;
        float y2 = y + height - r;
        double t = (double)(System.currentTimeMillis() % (long)(1000.0f / Math.max(0.001f, speed))) / (double)(1000.0f / Math.max(0.001f, speed));
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = -1.5707963267948966 + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.method_22918(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(argb2);
        }
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = 0.0 + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.method_22918(matrix, (float)((double)x2 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(argb2);
        }
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = 1.5707963267948966 + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.method_22918(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y2 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(argb2);
        }
        for (i2 = 0; i2 <= seg; ++i2) {
            keyCodec = Math.PI + (double)i2 * 1.5707963267948966 / (double)seg;
            hue = (float)((keyCodec + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb2 = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            argb2 = alpha << 24 | rgb2 & 0xFFFFFF;
            buffer.method_22918(matrix, (float)((double)x1 + Math.cos(keyCodec) * (double)r), (float)((double)y1 + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(argb2);
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        BufferBuilder inner = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        float rIn = Math.max(0.5f, r - 1.2f);
        for (i = 0; i <= seg; ++i) {
            double a2 = -1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            float hue2 = (float)((a2 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue2, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.method_22918(matrix, (float)((double)x2 + Math.cos(a2) * (double)rIn), (float)((double)y1 + Math.sin(a2) * (double)rIn), 0.0f).method_39415(argb);
        }
        for (i = 0; i <= seg; ++i) {
            double a3 = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            float hue3 = (float)((a3 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue3, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.method_22918(matrix, (float)((double)x2 + Math.cos(a3) * (double)rIn), (float)((double)y2 + Math.sin(a3) * (double)rIn), 0.0f).method_39415(argb);
        }
        for (i = 0; i <= seg; ++i) {
            double a4 = 1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            float hue4 = (float)((a4 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue4, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.method_22918(matrix, (float)((double)x1 + Math.cos(a4) * (double)rIn), (float)((double)y2 + Math.sin(a4) * (double)rIn), 0.0f).method_39415(argb);
        }
        for (i = 0; i <= seg; ++i) {
            double a5 = Math.PI + (double)i * 1.5707963267948966 / (double)seg;
            float hue5 = (float)((a5 + Math.PI * 2) / (Math.PI * 2) + t) % 1.0f;
            rgb = Color.HSBtoRGB(hue5, 1.0f, 1.0f);
            argb = alpha << 24 | rgb & 0xFFFFFF;
            inner.method_22918(matrix, (float)((double)x1 + Math.cos(a5) * (double)rIn), (float)((double)y1 + Math.sin(a5) * (double)rIn), 0.0f).method_39415(argb);
        }
        BufferUploader.method_43433((MeshData)inner.method_60800());
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(PoseStack matrices, float x, float y, float width, float height, float radius, Color c) {
        double keyCodec;
        int i;
        if (radius <= 0.0f) {
            Render2DUtil.drawRect(matrices, x, y, width, height, c);
            return;
        }
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        Render2DUtil.drawRect(matrices, x + r, y, width - 2.0f * r, height, c);
        Render2DUtil.drawRect(matrices, x, y + r, r, height - 2.0f * r, c);
        Render2DUtil.drawRect(matrices, x + width - r, y + r, r, height - 2.0f * r, c);
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::method_34540);
        int seg = 48;
        float cx = x + r;
        float cy = y + r;
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = Math.PI + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        cx = x + width - r;
        cy = y + r;
        buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = 4.71238898038469 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        cx = x + width - r;
        cy = y + height - r;
        buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        cx = x + r;
        cy = y + height - r;
        buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            keyCodec = 1.5707963267948966 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)cx + Math.cos(keyCodec) * (double)r), (float)((double)cy + Math.sin(keyCodec) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawRightRoundedRect(PoseStack matrices, float x, float y, float width, float height, float radius, Color c) {
        double a;
        int i;
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        if (radius <= 0.0f) {
            Render2DUtil.drawRect(matrices, x, y, width, height, c);
            return;
        }
        float r = Math.min(radius, Math.min(width, height) / 2.0f);
        int capR = Math.max(0, Math.min(255, (int)Math.round((double)c.getRed() * 0.85)));
        int capG = Math.max(0, Math.min(255, (int)Math.round((double)c.getGreen() * 0.85)));
        int capB = Math.max(0, Math.min(255, (int)Math.round((double)c.getBlue() * 0.85)));
        Render2DUtil.drawCircle(matrices, x + width - r, y + height / 2.0f, r, new Color(capR, capG, capB, c.getAlpha()), 64);
        Render2DUtil.drawRect(matrices, x, y, width - r, height, c);
        if (height > 2.0f * r) {
            Render2DUtil.drawRect(matrices, x + width - r, y + r, r, height - 2.0f * r, c);
        }
        Matrix4f matrix = matrices.last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::method_34540);
        int seg = 48;
        float cx = x + width - r;
        float cy = y + r;
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            a = 4.71238898038469 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)cx + Math.cos(a) * (double)r), (float)((double)cy + Math.sin(a) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        cx = x + width - r;
        cy = y + height - r;
        buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.method_22918(matrix, cx, cy, 0.0f).method_39415(c.getRGB());
        for (i = 0; i <= seg; ++i) {
            a = 0.0 + (double)i * 1.5707963267948966 / (double)seg;
            buffer.method_22918(matrix, (float)((double)cx + Math.cos(a) * (double)r), (float)((double)cy + Math.sin(a) * (double)r), 0.0f).method_39415(c.getRGB());
        }
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawDropShadow(PoseStack matrices, float x, float y, float width, float height, float radius) {
    }
}

