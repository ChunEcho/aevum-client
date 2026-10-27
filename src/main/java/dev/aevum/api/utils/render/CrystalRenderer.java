/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.EndCrystal
 *  net.minecraft.Vec3
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.Minecraft
 *  net.minecraft.Mth
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.Axis
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.lwjgl.opengl.GL11
 */
package dev.aevum.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.events.impl.Render3DEvent;
import dev.aevum.mod.modules.impl.render.Chams;
import java.awt.Color;
import net.minecraft.EndCrystal;
import net.minecraft.Vec3;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.Minecraft;
import net.minecraft.Mth;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.Axis;
import net.minecraft.MeshData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

public class CrystalRenderer {
    private static final float N = -0.25f;
    private static final float P = 0.25f;
    private static final float[][] CUBE_VERTICES = new float[][]{{-0.25f, -0.25f, -0.25f}, {0.25f, -0.25f, -0.25f}, {0.25f, 0.25f, -0.25f}, {-0.25f, 0.25f, -0.25f}, {-0.25f, -0.25f, 0.25f}, {0.25f, -0.25f, 0.25f}, {0.25f, 0.25f, 0.25f}, {-0.25f, 0.25f, 0.25f}};
    private static final int[][] CUBE_FACES = new int[][]{{0, 3, 2}, {0, 2, 1}, {4, 5, 6}, {4, 6, 7}, {0, 4, 7}, {0, 7, 3}, {1, 2, 6}, {1, 6, 5}, {3, 7, 6}, {3, 6, 2}, {0, 1, 5}, {0, 5, 4}};
    private static final int[][] CUBE_EDGES = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

    public static void renderCrystal(Render3DEvent event, EndCrystal entity, Chams module) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) {
            return;
        }
        PoseStack matrices = event.matrixStack;
        float tickDelta = event.tickDelta;
        Camera camera = mc.gameRenderer.method_19418();
        Vec3 camPos = camera.getPosition();
        float age = (float)entity.time + tickDelta;
        float g = Mth.method_15374((float)(age * 0.2f)) / 2.0f + 0.5f;
        g = (g * g + g) * 0.4f;
        float bounceOffset = g - 1.4f;
        float rotY = age * 3.0f;
        double renderX = Mth.method_16436((double)tickDelta, (double)entity.xOld, (double)entity.getX()) - camPos.x;
        double renderY = Mth.method_16436((double)tickDelta, (double)entity.yOld, (double)entity.getY()) - camPos.y;
        double renderZ = Mth.method_16436((double)tickDelta, (double)entity.zOld, (double)entity.getZ()) - camPos.z;
        matrices.method_22903();
        matrices.method_22904(renderX, renderY, renderZ);
        float scale = 2.0f * module.crystalScale.getValueFloat();
        matrices.method_22905(scale, scale, scale);
        matrices.method_46416(0.0f, -0.5f, 0.0f);
        matrices.method_22907(Axis.YP.rotationDegrees(rotY));
        matrices.method_46416(0.0f, 1.5f + bounceOffset / 2.0f, 0.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        float prevLineWidth = GL11.glGetFloat((int)2849);
        GL11.glLineWidth((float)module.lineWidth.getValueFloat());
        float sine45 = (float)Math.sin(0.7853981633974483);
        Quaternionf tilt = new Quaternionf().setAngleAxis(1.0471976f, sine45, 0.0f, sine45);
        Color fill = module.crystalFill.getValue();
        Color outline = module.crystalOutline.getValue();
        matrices.method_22907(tilt);
        CrystalRenderer.renderCube(matrices.last().method_23761(), fill, outline);
        matrices.method_22905(0.875f, 0.875f, 0.875f);
        matrices.method_22907(tilt);
        matrices.method_22907(Axis.YP.rotationDegrees(rotY));
        CrystalRenderer.renderCube(matrices.last().method_23761(), fill, outline);
        matrices.method_22905(0.875f, 0.875f, 0.875f);
        matrices.method_22907(tilt);
        matrices.method_22907(Axis.YP.rotationDegrees(rotY));
        CrystalRenderer.renderCube(matrices.last().method_23761(), fill, outline);
        GL11.glLineWidth((float)prevLineWidth);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.method_22909();
    }

    private static void renderCube(Matrix4f matrix, Color fill, Color outline) {
        CrystalRenderer.drawCubeFill(matrix, fill);
        CrystalRenderer.drawCubeOutline(matrix, outline);
    }

    private static void drawCubeFill(Matrix4f matrix, Color color) {
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float b = (float)color.getBlue() / 255.0f;
        float a = (float)color.getAlpha() / 255.0f;
        Tesselator tess = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buf = tess.method_60827(VertexFormat.VertexFormat$Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int[] face : CUBE_FACES) {
            float[] v0 = CUBE_VERTICES[face[0]];
            float[] v1 = CUBE_VERTICES[face[1]];
            float[] v2 = CUBE_VERTICES[face[2]];
            buf.method_22918(matrix, v0[0], v0[1], v0[2]).method_22915(r, g, b, a);
            buf.method_22918(matrix, v1[0], v1[1], v1[2]).method_22915(r, g, b, a);
            buf.method_22918(matrix, v2[0], v2[1], v2[2]).method_22915(r, g, b, a);
        }
        BufferUploader.method_43433((MeshData)buf.method_60800());
    }

    private static void drawCubeOutline(Matrix4f matrix, Color color) {
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float b = (float)color.getBlue() / 255.0f;
        float a = (float)color.getAlpha() / 255.0f;
        Tesselator tess = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buf = tess.method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (int[] edge : CUBE_EDGES) {
            float[] v1 = CUBE_VERTICES[edge[0]];
            float[] v2 = CUBE_VERTICES[edge[1]];
            buf.method_22918(matrix, v1[0], v1[1], v1[2]).method_22915(r, g, b, a);
            buf.method_22918(matrix, v2[0], v2[1], v2[2]).method_22915(r, g, b, a);
        }
        BufferUploader.method_43433((MeshData)buf.method_60800());
    }
}

