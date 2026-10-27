/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Entity
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Component
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.Font$DisplayMode
 *  net.minecraft.Mth
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.PoseStack$Pose
 *  net.minecraft.VertexConsumer
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.MultiBufferSource$BufferSource
 *  net.minecraft.FormattedText
 *  net.minecraft.GameRenderer
 *  net.minecraft.Axis
 *  net.minecraft.ByteBufferBuilder
 *  net.minecraft.MeshData
 *  org.jetbrains.annotations.NotNull
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package dev.aevum.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.ColorUtil;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.Entity;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Component;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.Font;
import net.minecraft.Mth;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import net.minecraft.VertexConsumer;
import net.minecraft.MultiBufferSource;
import net.minecraft.FormattedText;
import net.minecraft.GameRenderer;
import net.minecraft.Axis;
import net.minecraft.ByteBufferBuilder;
import net.minecraft.MeshData;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Render3DUtil
implements Wrapper {
    public static void endBuilding(BufferBuilder bb) {
        MeshData builtBuffer = bb.method_60800();
        if (builtBuffer != null) {
            BufferUploader.method_43433((MeshData)builtBuffer);
        }
    }

    public static PoseStack matrixFrom(double x, double y, double z) {
        PoseStack matrices = new PoseStack();
        Camera camera = Render3DUtil.mc.gameRenderer.method_19418();
        matrices.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.method_22907(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
        matrices.method_22904(x - camera.getPosition().x, y - camera.getPosition().y, z - camera.getPosition().z);
        return matrices;
    }

    public static void drawText3D(String text, Vec3 vec3d, Color color) {
        Render3DUtil.drawText3D(Component.method_30163((String)text), vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 1.0, color.getRGB());
    }

    public static void drawText3D(String text, Vec3 vec3d, int color) {
        Render3DUtil.drawText3D(Component.method_30163((String)text), vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 1.0, color);
    }

    public static void drawText3D(Component text, Vec3 vec3d, double offX, double offY, double scale, Color color) {
        Render3DUtil.drawText3D(text, vec3d.x, vec3d.y, vec3d.z, offX, offY, scale, color.getRGB());
    }

    public static void drawText3D(Component text, double x, double y, double z, double offX, double offY, double scale, int color) {
        RenderSystem.disableDepthTest();
        PoseStack matrices = Render3DUtil.matrixFrom(x, y, z);
        Camera camera = Render3DUtil.mc.gameRenderer.method_19418();
        matrices.method_22907(Axis.YP.rotationDegrees(-camera.getYRot()));
        matrices.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        RenderSystem.enableBlend();
        matrices.method_22904(offX, offY, 0.0);
        matrices.method_22905(-0.025f * (float)scale, -0.025f * (float)scale, 1.0f);
        int halfWidth = Render3DUtil.mc.font.method_27525((FormattedText)text) / 2;
        MultiBufferSource.MultiBufferSource$BufferSource immediate = MultiBufferSource.method_22991((ByteBufferBuilder)new ByteBufferBuilder(1536));
        Render3DUtil.mc.font.method_27521(text.getString(), (float)(-halfWidth), 0.0f, -1, true, matrices.last().method_23761(), (MultiBufferSource)immediate, Font.Font$DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        immediate.endBatch();
        Render3DUtil.mc.font.method_30882((Component)text.copy(), (float)(-halfWidth), 0.0f, color, false, matrices.last().method_23761(), (MultiBufferSource)immediate, Font.Font$DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        immediate.endBatch();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static void drawFill(PoseStack matrixStack, AABB bb, Color fillColor) {
        Render3DUtil.draw3DBox(matrixStack, bb, fillColor, new Color(0, 0, 0, 0), false, true);
    }

    public static void drawBox(PoseStack matrixStack, AABB bb, Color outlineColor) {
        Render3DUtil.draw3DBox(matrixStack, bb, new Color(0, 0, 0, 0), outlineColor, true, false);
    }

    public static void drawBox(PoseStack matrixStack, AABB bb, Color outlineColor, float lineWidth) {
        Render3DUtil.draw3DBox(matrixStack, bb, new Color(0, 0, 0, 0), outlineColor, true, false, lineWidth);
    }

    public static void draw3DBox(PoseStack matrixStack, AABB box, Color fillColor, Color outlineColor) {
        Render3DUtil.draw3DBox(matrixStack, box, fillColor, outlineColor, true, true);
    }

    public static void draw3DBox(PoseStack matrixStack, AABB box, Color fillColor, Color outlineColor, boolean outline, boolean fill) {
        Render3DUtil.draw3DBox(matrixStack, box, fillColor, outlineColor, outline, fill, 1.5f);
    }

    public static void draw3DBox(PoseStack matrixStack, AABB box, Color fillColor, Color outlineColor, boolean outline, boolean fill, float lineWidth) {
        BufferBuilder bufferBuilder;
        float elementCodec;
        float g;
        float r;
        float keyCodec;
        box = box.method_997(Render3DUtil.mc.gameRenderer.method_19418().getPosition().method_22882());
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        Matrix4f matrix = matrixStack.last().method_23761();
        if (outline) {
            keyCodec = (float)outlineColor.getAlpha() / 255.0f;
            r = (float)outlineColor.getRed() / 255.0f;
            g = (float)outlineColor.getGreen() / 255.0f;
            elementCodec = (float)outlineColor.getBlue() / 255.0f;
            RenderSystem.setShader(GameRenderer::method_34540);
            RenderSystem.lineWidth((float)lineWidth);
            bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        }
        if (fill) {
            keyCodec = (float)fillColor.getAlpha() / 255.0f;
            r = (float)fillColor.getRed() / 255.0f;
            g = (float)fillColor.getGreen() / 255.0f;
            elementCodec = (float)fillColor.getBlue() / 255.0f;
            RenderSystem.setShader(GameRenderer::method_34540);
            bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, elementCodec, keyCodec);
            bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, elementCodec, keyCodec);
            BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFadeFill(PoseStack stack, AABB box, Color c, Color c1) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f posMatrix = stack.last().method_23761();
        float minX = (float)(box.minX - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10216());
        float minY = (float)(box.minY - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10214());
        float minZ = (float)(box.minZ - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10215());
        float maxX = (float)(box.maxX - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10216());
        float maxY = (float)(box.maxY - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10214());
        float maxZ = (float)(box.maxZ - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10215());
        buffer.method_22918(posMatrix, minX, minY, minZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, maxX, minY, minZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, maxX, minY, maxZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, minX, minY, maxZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, minX, minY, minZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, minX, maxY, minZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, maxX, maxY, minZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, maxX, minY, minZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, maxX, minY, minZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, maxX, maxY, minZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, maxX, maxY, maxZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, maxX, minY, maxZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, minX, minY, maxZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, maxX, minY, maxZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, maxX, maxY, maxZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, minX, maxY, maxZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, minX, minY, minZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, minX, minY, maxZ).method_39415(c.getRGB());
        buffer.method_22918(posMatrix, minX, maxY, maxZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, minX, maxY, minZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, minX, maxY, minZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, minX, maxY, maxZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, maxX, maxY, maxZ).method_39415(c1.getRGB());
        buffer.method_22918(posMatrix, maxX, maxY, minZ).method_39415(c1.getRGB());
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void drawLine(Vec3 start, Vec3 end, Color color) {
        Render3DUtil.drawLine(start.x, start.method_10214(), start.z, end.method_10216(), end.method_10214(), end.method_10215(), color, 1.0f);
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        RenderSystem.enableBlend();
        PoseStack matrices = Render3DUtil.matrixFrom(x1, y1, z1);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth((float)width);
        BufferBuilder buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        Render3DUtil.vertexLine(matrices, (VertexConsumer)buffer, 0.0, 0.0, 0.0, (float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1), color);
        BufferUploader.method_43433((MeshData)buffer.method_60800());
        RenderSystem.enableCull();
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.disableBlend();
    }

    public static void vertexLine(PoseStack matrices, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, Color lineColor) {
        Matrix4f model = matrices.last().method_23761();
        PoseStack.PoseStack$Pose entry = matrices.last();
        Vector3f normalVec = Render3DUtil.getNormal((float)x1, (float)y1, (float)z1, (float)x2, (float)y2, (float)z2);
        buffer.method_22918(model, (float)x1, (float)y1, (float)z1).method_1336(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).method_60831(entry, normalVec.x(), normalVec.y(), normalVec.z());
        buffer.method_22918(model, (float)x2, (float)y2, (float)z2).method_1336(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).method_60831(entry, normalVec.x(), normalVec.y(), normalVec.z());
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = Mth.method_15355((float)(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal));
        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static void drawTargetEsp(PoseStack stack, @NotNull Entity target, Color color) {
        float alpha;
        int j;
        ArrayList<Vec3> vecs = new ArrayList<Vec3>();
        ArrayList<Vec3> vecs1 = new ArrayList<Vec3>();
        ArrayList<Vec3> vecs2 = new ArrayList<Vec3>();
        double x = target.xo + (target.getX() - target.xo) * (double)Render3DUtil.getTickDelta() - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10216();
        double y = target.yo + (target.getY() - target.yo) * (double)Render3DUtil.getTickDelta() - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10214();
        double z = target.zo + (target.getZ() - target.zo) * (double)Render3DUtil.getTickDelta() - Render3DUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10215();
        double height = target.getBbHeight();
        for (int i = 0; i <= 361; ++i) {
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));
            Vec3 vec = new Vec3((double)((float)(u * 0.5)), height, (double)((float)(v * 0.5)));
            vecs.add(vec);
            double v1 = Math.sin(Math.toRadians((i + 120) % 360));
            double u1 = Math.cos(Math.toRadians(i + 120) % 360.0);
            Vec3 vec1 = new Vec3((double)((float)(u1 * 0.5)), height, (double)((float)(v1 * 0.5)));
            vecs1.add(vec1);
            double v2 = Math.sin(Math.toRadians((i + 240) % 360));
            double u2 = Math.cos(Math.toRadians((i + 240) % 360));
            Vec3 vec2 = new Vec3((double)((float)(u2 * 0.5)), height, (double)((float)(v2 * 0.5)));
            vecs2.add(vec2);
            height -= (double)0.004f;
        }
        stack.method_22903();
        stack.method_22904(x, y, z);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = stack.last().method_23761();
        for (j = 0; j < vecs.size() - 1; ++j) {
            alpha = 1.0f - ((float)j + (float)(System.currentTimeMillis() - Aevum.initTime) / 5.0f) % 360.0f / 60.0f;
            bufferBuilder.method_22918(matrix, (float)((Vec3)vecs.get((int)j)).x, (float)((Vec3)vecs.get((int)j)).y, (float)((Vec3)vecs.get((int)j)).z).method_39415(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
            bufferBuilder.method_22918(matrix, (float)((Vec3)vecs.get((int)(j + 1))).x, (float)((Vec3)vecs.get((int)(j + 1))).y + 0.1f, (float)((Vec3)vecs.get((int)(j + 1))).z).method_39415(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
        }
        Render3DUtil.endBuilding(bufferBuilder);
        RenderSystem.setShader(GameRenderer::method_34540);
        bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (j = 0; j < vecs1.size() - 1; ++j) {
            alpha = 1.0f - ((float)j + (float)(System.currentTimeMillis() - Aevum.initTime) / 5.0f) % 360.0f / 60.0f;
            bufferBuilder.method_22918(matrix, (float)((Vec3)vecs1.get((int)j)).x, (float)((Vec3)vecs1.get((int)j)).y, (float)((Vec3)vecs1.get((int)j)).z).method_39415(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
            bufferBuilder.method_22918(matrix, (float)((Vec3)vecs1.get((int)(j + 1))).x, (float)((Vec3)vecs1.get((int)(j + 1))).y + 0.1f, (float)((Vec3)vecs1.get((int)(j + 1))).z).method_39415(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
        }
        Render3DUtil.endBuilding(bufferBuilder);
        RenderSystem.setShader(GameRenderer::method_34540);
        bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (j = 0; j < vecs2.size() - 1; ++j) {
            alpha = 1.0f - ((float)j + (float)(System.currentTimeMillis() - Aevum.initTime) / 5.0f) % 360.0f / 60.0f;
            bufferBuilder.method_22918(matrix, (float)((Vec3)vecs2.get((int)j)).x, (float)((Vec3)vecs2.get((int)j)).y, (float)((Vec3)vecs2.get((int)j)).z).method_39415(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
            bufferBuilder.method_22918(matrix, (float)((Vec3)vecs2.get((int)(j + 1))).x, (float)((Vec3)vecs2.get((int)(j + 1))).y + 0.1f, (float)((Vec3)vecs2.get((int)(j + 1))).z).method_39415(ColorUtil.injectAlpha(ColorUtil.pulseColor(color, (int)((float)j / 20.0f), 10, 1.0), (int)(alpha * 255.0f)).getRGB());
        }
        Render3DUtil.endBuilding(bufferBuilder);
        RenderSystem.enableCull();
        stack.method_22904(-x, -y, -z);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        stack.method_22909();
    }

    public static float getTickDelta() {
        return mc.getTimer().method_60637(true);
    }
}

