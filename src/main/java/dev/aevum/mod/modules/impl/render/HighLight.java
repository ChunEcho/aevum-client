/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BlockGetter
 *  net.minecraft.AABB
 *  net.minecraft.HitResult
 *  net.minecraft.HitResult$Type
 *  net.minecraft.VoxelShape
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.BlockGetter;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.VoxelShape;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;

public class HighLight
extends Module {
    public static HighLight INSTANCE;
    private final BooleanSetting depth = this.add(new BooleanSetting("Depth", true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 0, 0, 50)).injectBoolean(true));
    private final ColorSetting boxColor = this.add(new ColorSetting("Box", new Color(255, 0, 0, 100)).injectBoolean(true));

    public HighLight() {
        super("HighLight", Module.Category.Render);
        INSTANCE = this;
        this.setChinese("\u65b9\u5757\u9ad8\u4eae");
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        HitResult ItemStackParticleEffect;
        if (HighLight.mc.hitResult.method_17783() == HitResult.HitResult$Type.BLOCK && (ItemStackParticleEffect = HighLight.mc.hitResult) instanceof BlockHitResult) {
            BlockHitResult hitResult = (BlockHitResult)ItemStackParticleEffect;
            if (this.fill.booleanValue || this.boxColor.booleanValue) {
                BufferBuilder bufferBuilder;
                float b;
                float g;
                float r;
                float a;
                Color color;
                VoxelShape shape = HighLight.mc.level.method_8320(hitResult.method_17777()).method_26218((BlockGetter)HighLight.mc.level, hitResult.method_17777());
                if (shape == null) {
                    return;
                }
                if (shape.method_1110()) {
                    return;
                }
                AABB box = shape.method_1107().method_996(hitResult.method_17777()).method_1014(0.001);
                box = box.method_997(HighLight.mc.gameRenderer.method_19418().getPosition().method_22882());
                RenderSystem.enableBlend();
                if (!this.depth.getValue()) {
                    RenderSystem.disableDepthTest();
                } else {
                    RenderSystem.enableDepthTest();
                }
                Matrix4f matrix = matrixStack.last().method_23761();
                if (this.fill.booleanValue) {
                    color = this.fill.getValue();
                    a = (float)color.getAlpha() / 255.0f;
                    r = (float)color.getRed() / 255.0f;
                    g = (float)color.getGreen() / 255.0f;
                    b = (float)color.getBlue() / 255.0f;
                    RenderSystem.setShader(GameRenderer::method_34540);
                    bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
                }
                if (this.depth.getValue()) {
                    RenderSystem.disableDepthTest();
                }
                if (this.boxColor.booleanValue) {
                    color = this.boxColor.getValue();
                    a = (float)color.getAlpha() / 255.0f;
                    r = (float)color.getRed() / 255.0f;
                    g = (float)color.getGreen() / 255.0f;
                    b = (float)color.getBlue() / 255.0f;
                    RenderSystem.setShader(GameRenderer::method_34540);
                    bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).method_22915(r, g, b, a);
                    bufferBuilder.method_22918(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).method_22915(r, g, b, a);
                    BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
                }
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
            }
        }
    }
}

