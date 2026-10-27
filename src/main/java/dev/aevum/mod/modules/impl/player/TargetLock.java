/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 */
package dev.aevum.mod.modules.impl.player;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;

public class TargetLock
extends Module {
    public static TargetLock INSTANCE;
    private final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 3.0, 0.5, 10.0, 0.5));
    private final SliderSetting scale = this.add(new SliderSetting("Scale", 0.45, 0.2, 1.5, 0.05));
    private final SliderSetting heightPos = this.add(new SliderSetting("HeightPos", 0.63, 0.0, 1.0, 0.01));
    private final SliderSetting gap = this.add(new SliderSetting("Gap", 0.28, 0.0, 0.45, 0.01));
    private final SliderSetting rotationSpeed = this.add(new SliderSetting("RotationSpeed", 0.0, -180.0, 180.0, 1.0));
    private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 1.0, 0.1, 3.0, 0.1));
    private final BooleanSetting players = this.add(new BooleanSetting("Players", true));
    private final BooleanSetting friends = this.add(new BooleanSetting("Friends", false));
    private final BooleanSetting self = this.add(new BooleanSetting("Self", false));

    public TargetLock() {
        super("TargetLock", Module.Category.Player);
        this.setChinese("\u76ee\u6807\u9501\u5b9a");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics DrawContext, float n) {
        if (!Module.nullCheck()) {
            PoseStack getMatrices = DrawContext.method_51448();
            for (Entity NarratorManager : Aevum.THREAD.getEntities()) {
                if (!(NarratorManager instanceof Player) || NarratorManager == TargetLock.mc.player && !this.self.getValue() || !this.friends.getValue() && Aevum.FRIEND.isFriend(((Player)NarratorManager).method_5477().getString()) || !this.players.getValue()) continue;
                this.drawTargetLock(getMatrices, NarratorManager, n);
            }
        }
    }

    private void drawTargetLock(PoseStack MatrixStack, Entity VertexConsumer, float n) {
        double interpolate = MathUtil.interpolate(VertexConsumer.xOld, VertexConsumer.getX(), (double)n);
        double interpolate2 = MathUtil.interpolate(VertexConsumer.yOld, VertexConsumer.getY(), (double)n);
        double interpolate3 = MathUtil.interpolate(VertexConsumer.zOld, VertexConsumer.getZ(), (double)n);
        AABB getBoundingBox = VertexConsumer.method_5829();
        AABB VertexMultiConsumer$Double = new AABB(getBoundingBox.minX - VertexConsumer.getX() + interpolate, getBoundingBox.minY - VertexConsumer.getY() + interpolate2, getBoundingBox.minZ - VertexConsumer.getZ() + interpolate3, getBoundingBox.maxX - VertexConsumer.getX() + interpolate, getBoundingBox.maxY - VertexConsumer.getY() + interpolate2, getBoundingBox.maxZ - VertexConsumer.getZ() + interpolate3);
        Vec3[] array = new Vec3[]{new Vec3(VertexMultiConsumer$Double.minX, VertexMultiConsumer$Double.minY, VertexMultiConsumer$Double.minZ), new Vec3(VertexMultiConsumer$Double.minX, VertexMultiConsumer$Double.maxY, VertexMultiConsumer$Double.minZ), new Vec3(VertexMultiConsumer$Double.maxX, VertexMultiConsumer$Double.minY, VertexMultiConsumer$Double.minZ), new Vec3(VertexMultiConsumer$Double.maxX, VertexMultiConsumer$Double.maxY, VertexMultiConsumer$Double.minZ), new Vec3(VertexMultiConsumer$Double.minX, VertexMultiConsumer$Double.minY, VertexMultiConsumer$Double.maxZ), new Vec3(VertexMultiConsumer$Double.minX, VertexMultiConsumer$Double.maxY, VertexMultiConsumer$Double.maxZ), new Vec3(VertexMultiConsumer$Double.maxX, VertexMultiConsumer$Double.minY, VertexMultiConsumer$Double.maxZ), new Vec3(VertexMultiConsumer$Double.maxX, VertexMultiConsumer$Double.maxY, VertexMultiConsumer$Double.maxZ)};
        double min = Double.POSITIVE_INFINITY;
        double min2 = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double max2 = Double.NEGATIVE_INFINITY;
        boolean b = false;
        Vec3[] array2 = array;
        int length = array2.length;
        for (int i = 0; i < length; ++i) {
            Vec3 worldSpaceToScreenSpace = TextUtil.worldSpaceToScreenSpace(array2[i]);
            if (!(worldSpaceToScreenSpace.z > 0.0) || !(worldSpaceToScreenSpace.z < 1.0)) continue;
            b = true;
            min = Math.min(worldSpaceToScreenSpace.x, min);
            min2 = Math.min(worldSpaceToScreenSpace.y, min2);
            max = Math.max(worldSpaceToScreenSpace.x, max);
            max2 = Math.max(worldSpaceToScreenSpace.y, max2);
        }
        if (b && max - min > 0.0 && max2 - min2 > 0.0) {
            float n2 = (float)(max2 - min2);
            float n3 = (float)(min + max) / 2.0f;
            float n4 = (float)max2 - n2 * this.heightPos.getValueFloat();
            float n5 = n2 * 0.5f * this.scale.getValueFloat();
            float n6 = 0.5f - Math.max(0.0f, Math.min(0.5f, this.gap.getValueFloat())) * 0.5f;
            long currentTimeMillis = System.currentTimeMillis();
            double n7 = (double)(currentTimeMillis % 360000L) / 1000.0 * Math.toRadians(this.rotationSpeed.getValue());
            float n8 = (float)Math.cos(n7);
            float n9 = (float)Math.sin(n7);
            float n10 = (float)(currentTimeMillis % 10000L) * this.rainbowSpeed.getValueFloat() / 2000.0f;
            float valueFloat = this.lineWidth.getValueFloat();
            float[][] array3 = new float[][]{{-n5, n5}, {n5, n5}, {n5, -n5}, {-n5, -n5}};
            float[][] array4 = new float[4][2];
            for (int j = 0; j < 4; ++j) {
                array4[j][0] = n3 + array3[j][0] * n8 - array3[j][1] * n9;
                array4[j][1] = n4 + array3[j][0] * n9 + array3[j][1] * n8;
            }
            for (int k = 0; k < 4; ++k) {
                float[] array5 = array4[k];
                float[] array6 = array4[(k + 1) % 4];
                this.drawSegment(MatrixStack, array5, array6, 0.0f, n6, valueFloat, n10, k);
                this.drawSegment(MatrixStack, array5, array6, 1.0f - n6, 1.0f, valueFloat, n10, k);
            }
        }
    }

    private void drawSegment(PoseStack MatrixStack, float[] array, float[] array2, float n, float n2, float n3, float n4, int n5) {
        float n6 = array[0] + (array2[0] - array[0]) * n;
        float n7 = array[1] + (array2[1] - array[1]) * n;
        float n8 = array[0] + (array2[0] - array[0]) * n2;
        float n9 = array[1] + (array2[1] - array[1]) * n2;
        int n10 = Color.getHSBColor((n4 + (float)n5 / 4.0f + (n + n2) / 8.0f) % 1.0f, 1.0f, 1.0f).getRGB() & 0xFFFFFF;
        this.drawThickLine(MatrixStack, n6, n7, n8, n9, n3 * 3.0f, n10 | 0x4C000000);
        this.drawThickLine(MatrixStack, n6, n7, n8, n9, n3, n10 | 0xFF000000);
    }

    private void drawThickLine(PoseStack MatrixStack, float n, float n2, float n3, float n4, float n5, int n6) {
        float n7 = n3 - n;
        float n8 = n4 - n2;
        float n9 = (float)Math.sqrt(n7 * n7 + n8 * n8);
        if (n9 > 0.0f) {
            float n10 = -n8 / n9 * n5 / 2.0f;
            float n11 = n7 / n9 * n5 / 2.0f;
            Matrix4f getPositionMatrix = MatrixStack.last().method_23761();
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::method_34540);
            BufferBuilder getNormalMatrix = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            getNormalMatrix.method_22918(getPositionMatrix, n + n10, n2 + n11, 0.0f).method_39415(n6);
            getNormalMatrix.method_22918(getPositionMatrix, n3 + n10, n4 + n11, 0.0f).method_39415(n6);
            getNormalMatrix.method_22918(getPositionMatrix, n3 - n10, n4 - n11, 0.0f).method_39415(n6);
            getNormalMatrix.method_22918(getPositionMatrix, n - n10, n2 - n11, 0.0f).method_39415(n6);
            BufferUploader.method_43433((MeshData)getNormalMatrix.method_60800());
            RenderSystem.disableBlend();
        }
    }
}

