/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Entity
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 */
package dev.aevum.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.ColorUtil;
import java.awt.Color;
import net.minecraft.Entity;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;

public class JelloUtil
implements Wrapper {
    private static float prevCircleStep;
    private static float circleStep;

    public static void drawJello(PoseStack matrix, Entity target, Color color) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * mc.getTimer().method_60637(true);
        double prevSinAnim = JelloUtil.absSinAnimation(cs - (double)0.45f);
        double sinAnim = JelloUtil.absSinAnimation(cs);
        double x = target.xo + (target.getX() - target.xo) * (double)mc.getTimer().method_60637(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10216();
        double y = target.yo + (target.getY() - target.yo) * (double)mc.getTimer().method_60637(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10214() + prevSinAnim * (double)target.getBbHeight();
        double z = target.zo + (target.getZ() - target.zo) * (double)mc.getTimer().method_60637(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10215();
        double nextY = target.yo + (target.getY() - target.yo) * (double)mc.getTimer().method_60637(true) - JelloUtil.mc.getEntityRenderDispatcher().camera.getPosition().method_10214() + sinAnim * (double)target.getBbHeight();
        matrix.method_22903();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.method_60827(VertexFormat.VertexFormat$Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::method_34540);
        for (int i = 0; i <= 30; ++i) {
            float cos = (float)(x + Math.cos((double)i * 6.28 / 30.0) * (target.method_5829().maxX - target.method_5829().minX + (target.method_5829().maxZ - target.method_5829().minZ)) * 0.5);
            float sin = (float)(z + Math.sin((double)i * 6.28 / 30.0) * (target.method_5829().maxX - target.method_5829().minX + (target.method_5829().maxZ - target.method_5829().minZ)) * 0.5);
            bufferBuilder.method_22918(matrix.last().method_23761(), cos, (float)nextY, sin).method_39415(color.getRGB());
            bufferBuilder.method_22918(matrix.last().method_23761(), cos, (float)y, sin).method_39415(ColorUtil.injectAlpha(color, 0).getRGB());
        }
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        matrix.method_22909();
    }

    public static void updateJello() {
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }

    private static double absSinAnimation(double input) {
        return Math.abs(1.0 + Math.sin(input)) / 2.0;
    }
}

