/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.Vec3
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 *  net.minecraft.ModelLayers
 *  net.minecraft.EntityRendererProvider$Context
 *  net.minecraft.PlayerModel
 *  net.minecraft.ModelPart
 *  net.minecraft.ModelPart$Polygon
 *  net.minecraft.ModelPart$Cube
 *  net.minecraft.GameRenderer
 *  net.minecraft.Axis
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package dev.aevum.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.asm.accessors.ILivingEntity;
import dev.aevum.asm.accessors.IModelPart;
import dev.aevum.asm.accessors.IModelPartCuboid;
import dev.aevum.asm.accessors.IPlayerEntityModel;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.Vec3;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.Mth;
import net.minecraft.PoseStack;
import net.minecraft.ModelLayers;
import net.minecraft.EntityRendererProvider;
import net.minecraft.PlayerModel;
import net.minecraft.ModelPart;
import net.minecraft.GameRenderer;
import net.minecraft.Axis;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ModelPlayer
extends PlayerModel<Player> {
    public final Player player;
    private static final Vector4f pos1 = new Vector4f();
    private static final Vector4f pos2 = new Vector4f();
    private static final Vector4f pos3 = new Vector4f();
    private static final Vector4f pos4 = new Vector4f();

    public ModelPlayer(Player player) {
        super(new EntityRendererProvider.EntityRendererProvider$Context(Wrapper.mc.getEntityRenderDispatcher(), Wrapper.mc.getItemRenderer(), Wrapper.mc.getBlockRenderer(), Wrapper.mc.getEntityRenderDispatcher().getItemInHandRenderer(), Wrapper.mc.getResourceManager(), Wrapper.mc.getEntityModels(), Wrapper.mc.font).method_32167(ModelLayers.PLAYER), false);
        this.player = player;
        ((IPlayerEntityModel)((Object)this)).getLeftPants().visible = false;
        ((IPlayerEntityModel)((Object)this)).getRightPants().visible = false;
        ((IPlayerEntityModel)((Object)this)).getLeftSleeve().visible = false;
        ((IPlayerEntityModel)((Object)this)).getRightSleeve().visible = false;
        ((IPlayerEntityModel)((Object)this)).getJacket().visible = false;
        this.hat.visible = false;
        this.method_2838().method_41924(new Vector3f(-0.05f, -0.05f, -0.05f));
        this.crouching = player.isCrouching();
    }

    public void render(PoseStack matrices, ColorSetting fill, ColorSetting line) {
        this.render(matrices, fill, line, 1.0, 0.0, 1.0, 0.0, false, false);
    }

    public void render(PoseStack matrices, ColorSetting fill, ColorSetting line, double alpha, double yOffset, double scale, double yaw, boolean noLimb, boolean forceSneaking) {
        if (forceSneaking) {
            this.crouching = true;
        }
        double x = this.player.getX() - Wrapper.mc.getEntityRenderDispatcher().camera.getPosition().method_10216();
        double y = this.player.getY() - Wrapper.mc.getEntityRenderDispatcher().camera.getPosition().method_10214() + yOffset;
        double z = this.player.getZ() - Wrapper.mc.getEntityRenderDispatcher().camera.getPosition().method_10215();
        matrices.method_22903();
        matrices.method_46416((float)x, (float)y, (float)z);
        matrices.method_22907(Axis.YP.rotation(MathUtil.rad(180.0f - this.player.yBodyRot + (float)yaw)));
        this.attackTime = this.player.method_6055(1.0f);
        float j = ((ILivingEntity)this.player).getLeaningPitch();
        if (this.player.isFallFlying()) {
            float k = this.player.getXRot();
            float l = (float)this.player.getFallFlyingTicks() + this.player.yBodyRot + (float)yaw;
            float m = Mth.method_15363((float)(l * l / 100.0f), (float)0.0f, (float)1.0f);
            if (!this.player.isAutoSpinAttack()) {
                matrices.method_22907(Axis.XP.rotationDegrees(m * (-90.0f - k)));
            }
            Vec3 vec3d = this.player.method_5828(1.0f);
            Vec3 vec3d2 = this.player.getDeltaMovement();
            double d = vec3d2.method_37268();
            double e = vec3d.method_37268();
            if (d > 0.0 && e > 0.0) {
                double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrices.method_22907(Axis.YP.rotation((float)(Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0f) {
            float k = this.player.getXRot();
            float l = this.player.isInWater() ? -90.0f - k : -90.0f;
            float m = Mth.method_16439((float)j, (float)0.0f, (float)l);
            matrices.method_22907(Axis.XP.rotationDegrees(m));
            if (this.player.isVisuallySwimming()) {
                matrices.method_46416(0.0f, -1.0f, 0.3f);
            }
        }
        matrices.method_22905(-1.0f, -1.0f, 1.0f);
        matrices.method_46416(0.0f, -1.401f, 0.0f);
        matrices.method_22905((float)scale * 0.93f, (float)scale * 0.93f, (float)scale * 0.93f);
        this.method_17086((LivingEntity)this.player, noLimb ? 0.0f : this.player.walkAnimation.method_48569(), noLimb ? 0.0f : this.player.walkAnimation.method_48566(), Wrapper.mc.getTimer().method_60637(true));
        this.method_17087((LivingEntity)this.player, noLimb ? 0.0f : this.player.walkAnimation.method_48569(), noLimb ? 0.0f : this.player.walkAnimation.method_48566(), this.player.tickCount, this.player.yHeadRot - this.player.yBodyRot, this.player.getXRot());
        this.riding = this.player.isPassenger();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::method_34540);
        this.method_22946().forEach(modelPart -> ModelPlayer.render(matrices, modelPart, fill, line, alpha, false));
        this.method_22948().forEach(modelPart -> ModelPlayer.render(matrices, modelPart, fill, line, alpha, false));
        matrices.method_22909();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static void render(PoseStack matrices, ModelPart part, ColorSetting fill, ColorSetting line, double alpha, boolean texture) {
        if (!part.visible || ((IModelPart)part).getCuboids().isEmpty() && ((IModelPart)part).getChildren().isEmpty()) {
            return;
        }
        matrices.method_22903();
        part.method_22703(matrices);
        for (ModelPart.ModelPart$Cube cuboid : ((IModelPart)part).getCuboids()) {
            ModelPlayer.render(matrices, cuboid, fill, line, alpha, texture);
        }
        for (ModelPart child : ((IModelPart)part).getChildren().values()) {
            ModelPlayer.render(matrices, child, fill, line, alpha, texture);
        }
        matrices.method_22909();
    }

    public static void render(PoseStack matrices, ModelPart.ModelPart$Cube cuboid, ColorSetting fill, ColorSetting line, double alpha, boolean texture) {
        Matrix4f matrix = matrices.last().method_23761();
        for (ModelPart.ModelPart$Polygon quad : ((IModelPartCuboid)cuboid).getSides()) {
            BufferBuilder buffer;
            float elementCodec;
            float g;
            float r;
            float keyCodec;
            Color color;
            pos1.set(quad.vertices[0].pos.x / 16.0f, quad.vertices[0].pos.y / 16.0f, quad.vertices[0].pos.z / 16.0f, 1.0f);
            pos1.mul((Matrix4fc)matrix);
            pos2.set(quad.vertices[1].pos.x / 16.0f, quad.vertices[1].pos.y / 16.0f, quad.vertices[1].pos.z / 16.0f, 1.0f);
            pos2.mul((Matrix4fc)matrix);
            pos3.set(quad.vertices[2].pos.x / 16.0f, quad.vertices[2].pos.y / 16.0f, quad.vertices[2].pos.z / 16.0f, 1.0f);
            pos3.mul((Matrix4fc)matrix);
            pos4.set(quad.vertices[3].pos.x / 16.0f, quad.vertices[3].pos.y / 16.0f, quad.vertices[3].pos.z / 16.0f, 1.0f);
            pos4.mul((Matrix4fc)matrix);
            if (fill.booleanValue) {
                color = fill.getValue();
                keyCodec = (float)((double)((float)color.getAlpha() / 255.0f) * alpha);
                r = (float)color.getRed() / 255.0f;
                g = (float)color.getGreen() / 255.0f;
                elementCodec = (float)color.getBlue() / 255.0f;
                buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, texture ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR);
                buffer.method_22912(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).method_22913(quad.vertices[0].u, quad.vertices[0].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).method_22913(quad.vertices[1].u, quad.vertices[1].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).method_22913(quad.vertices[1].u, quad.vertices[1].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).method_22913(quad.vertices[2].u, quad.vertices[2].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).method_22913(quad.vertices[2].u, quad.vertices[2].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos4.x, ModelPlayer.pos4.y, ModelPlayer.pos4.z).method_22913(quad.vertices[3].u, quad.vertices[3].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).method_22913(quad.vertices[0].u, quad.vertices[0].v).method_22915(r, g, elementCodec, keyCodec);
                buffer.method_22912(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).method_22913(quad.vertices[0].u, quad.vertices[0].v).method_22915(r, g, elementCodec, keyCodec);
                Render3DUtil.endBuilding(buffer);
            }
            if (!line.booleanValue) continue;
            color = line.getValue();
            keyCodec = (float)((double)((float)color.getAlpha() / 255.0f) * alpha);
            r = (float)color.getRed() / 255.0f;
            g = (float)color.getGreen() / 255.0f;
            elementCodec = (float)color.getBlue() / 255.0f;
            buffer = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINES, texture ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR);
            buffer.method_22912(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).method_22913(quad.vertices[0].u, quad.vertices[0].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).method_22913(quad.vertices[1].u, quad.vertices[1].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos2.x, ModelPlayer.pos2.y, ModelPlayer.pos2.z).method_22913(quad.vertices[1].u, quad.vertices[1].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).method_22913(quad.vertices[2].u, quad.vertices[2].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos3.x, ModelPlayer.pos3.y, ModelPlayer.pos3.z).method_22913(quad.vertices[2].u, quad.vertices[2].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos4.x, ModelPlayer.pos4.y, ModelPlayer.pos4.z).method_22913(quad.vertices[3].u, quad.vertices[3].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos4.x, ModelPlayer.pos4.y, ModelPlayer.pos4.z).method_22913(quad.vertices[3].u, quad.vertices[3].v).method_22915(r, g, elementCodec, keyCodec);
            buffer.method_22912(ModelPlayer.pos1.x, ModelPlayer.pos1.y, ModelPlayer.pos1.z).method_22913(quad.vertices[0].u, quad.vertices[0].v).method_22915(r, g, elementCodec, keyCodec);
            Render3DUtil.endBuilding(buffer);
        }
    }
}

