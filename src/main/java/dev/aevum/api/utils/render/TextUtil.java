/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3
 *  net.minecraft.GuiGraphics
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.lwjgl.opengl.GL11
 */
package dev.aevum.api.utils.render;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.core.impl.FontManager;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.Vec3;
import net.minecraft.GuiGraphics;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class TextUtil
implements Wrapper {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static float getWidth(String s) {
        return TextUtil.mc.font.method_1727(s);
    }

    public static float getHeight() {
        Objects.requireNonNull(TextUtil.mc.font);
        return 9.0f;
    }

    public static void drawStringWithScale(GuiGraphics drawContext, String text, float x, float y, int color, float scale) {
        PoseStack matrixStack = drawContext.method_51448();
        if (scale != 1.0f) {
            matrixStack.method_22903();
            matrixStack.method_22905(scale, scale, 1.0f);
            if (scale > 1.0f) {
                matrixStack.method_46416(-x / scale, -y / scale, 0.0f);
            } else {
                matrixStack.method_46416(x / scale / 2.0f, y / scale / 2.0f, 0.0f);
            }
        }
        TextUtil.drawString(drawContext, text, x, y, color);
        matrixStack.method_22909();
    }

    public static void drawStringScale(GuiGraphics drawContext, String text, float x, float y, int color, float scale, boolean shadow) {
        PoseStack matrixStack = drawContext.method_51448();
        if (scale != 1.0f) {
            matrixStack.method_22903();
            matrixStack.method_22905(scale, scale, 1.0f);
            if (scale > 1.0f) {
                matrixStack.method_46416(-x / scale, -y / scale, 0.0f);
            } else {
                matrixStack.method_46416(x / scale / 2.0f, y / scale / 2.0f, 0.0f);
            }
        }
        drawContext.method_51433(TextUtil.mc.font, text, (int)x, (int)y, color, shadow);
        matrixStack.method_22909();
    }

    public static void drawString(GuiGraphics drawContext, String text, double x, double y, int color) {
        TextUtil.drawString(drawContext, text, x, y, color, false);
    }

    public static void drawString(GuiGraphics drawContext, String text, double x, double y, int color, boolean customFont) {
        TextUtil.drawString(drawContext, text, x, y, color, customFont, true);
    }

    public static void drawString(GuiGraphics drawContext, String text, double x, double y, int color, boolean customFont, boolean shadow) {
        if (customFont) {
            FontManager.draw(drawContext.method_51448(), text, (int)x, (int)y, color, shadow);
        } else {
            drawContext.method_51433(TextUtil.mc.font, text, (int)x, (int)y, color, shadow);
        }
    }

    public static void drawStringPulse(GuiGraphics drawContext, String text, double x, double y, Color startColor, Color endColor, double speed, int counter, boolean customFont) {
        char[] stringToCharArray = text.toCharArray();
        int index = 0;
        boolean color = false;
        String s = null;
        for (char c : stringToCharArray) {
            if (c == '\u00a7') {
                color = true;
                continue;
            }
            if (color) {
                s = c == 'r' ? null : "\u00a7" + c;
                color = false;
                continue;
            }
            ++index;
            if (s != null) {
                TextUtil.drawString(drawContext, s + c, x, y, startColor.getRGB(), customFont);
            } else {
                TextUtil.drawString(drawContext, String.valueOf(c), x, y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB(), customFont);
            }
            x += customFont ? (double)FontManager.ui.getWidth(String.valueOf(c)) : (double)TextUtil.mc.font.method_1727(String.valueOf(c));
        }
    }

    public static void drawStringPulse(GuiGraphics drawContext, String text, double x, double y, Color startColor, Color endColor, double speed, int counter, boolean customFont, boolean shadow) {
        char[] stringToCharArray = text.toCharArray();
        int index = 0;
        boolean color = false;
        String s = null;
        for (char c : stringToCharArray) {
            if (c == '\u00a7') {
                color = true;
                continue;
            }
            if (color) {
                s = c == 'r' ? null : "\u00a7" + c;
                color = false;
                continue;
            }
            ++index;
            if (s != null) {
                TextUtil.drawString(drawContext, s + c, x, y, startColor.getRGB(), customFont, shadow);
            } else {
                TextUtil.drawString(drawContext, String.valueOf(c), x, y, ColorUtil.pulseColor(startColor, endColor, index, counter, speed).getRGB(), customFont, shadow);
            }
            x += customFont ? (double)FontManager.ui.getWidth(String.valueOf(c)) : (double)TextUtil.mc.font.method_1727(String.valueOf(c));
        }
    }

    public static Vec3 worldSpaceToScreenSpace(Vec3 pos) {
        Camera camera = TextUtil.mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getScreenHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv((int)2978, (int[])viewport);
        Vector3f target = new Vector3f();
        double deltaX = pos.x - camera.getPosition().x;
        double deltaY = pos.y - camera.getPosition().y;
        double deltaZ = pos.z - camera.getPosition().z;
        Vector4f transformedCoordinates = new Vector4f((float)deltaX, (float)deltaY, (float)deltaZ, 1.0f).mul((Matrix4fc)lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f((Matrix4fc)lastProjMat);
        Matrix4f matrixModel = new Matrix4f((Matrix4fc)lastModMat);
        matrixProj.mul((Matrix4fc)matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3((double)target.x / mc.getWindow().getGuiScale(), (double)((float)displayHeight - target.y) / mc.getWindow().getGuiScale(), (double)target.z);
    }
}

