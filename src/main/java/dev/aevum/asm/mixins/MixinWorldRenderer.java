/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Entity
 *  net.minecraft.LightningBolt
 *  net.minecraft.PostChain
 *  net.minecraft.Minecraft
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.MultiBufferSource$BufferSource
 *  net.minecraft.LevelRenderer
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.core.impl.ShaderManager;
import dev.aevum.mod.modules.impl.player.Freecam;
import dev.aevum.mod.modules.impl.render.Ambience;
import dev.aevum.mod.modules.impl.render.Chams;
import dev.aevum.mod.modules.impl.render.ShaderModule;
import dev.aevum.mod.modules.impl.render.skybox.Skybox;
import net.minecraft.Entity;
import net.minecraft.LightningBolt;
import net.minecraft.PostChain;
import net.minecraft.Minecraft;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.LevelRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public abstract class MixinWorldRenderer {
    @Unique
    boolean renderingChams = false;
    @Unique
    boolean renderingEntity = false;
    @Unique
    boolean renderingLightningShader = false;

    @Shadow
    abstract void method_22977(Entity var1, double var2, double var4, double var6, float var8, PoseStack var9, MultiBufferSource var10);

    @Inject(method={"renderEntity"}, at={@At(value="HEAD")})
    private void injectChamsForEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.throughWall.getValue()) {
            if (Chams.INSTANCE.chams(entity)) {
                if (this.renderingEntity) {
                    Wrapper.mc.renderBuffers().bufferSource().endBatch();
                    this.renderingEntity = false;
                }
                GL11.glEnable((int)32823);
                GL11.glPolygonOffset((float)1.0f, (float)-1000000.0f);
                this.renderingChams = true;
            } else {
                this.renderingEntity = true;
            }
        }
    }

    @Inject(method={"renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderSkyHead(Matrix4f matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo info) {
        if (Skybox.INSTANCE != null && Skybox.INSTANCE.isOn()) {
            runnable.run();
            Skybox.INSTANCE.renderSky(matrices, projectionMatrix, tickDelta);
            info.cancel();
        }
    }

    @Inject(method={"renderEntity"}, at={@At(value="RETURN")})
    private void injectChamsForEntityPost(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.throughWall.getValue() && this.renderingChams) {
            Wrapper.mc.renderBuffers().bufferSource().endBatch();
            GL11.glPolygonOffset((float)1.0f, (float)1000000.0f);
            GL11.glDisable((int)32823);
            this.renderingChams = false;
        }
    }

    @Inject(method={"renderEntity"}, at={@At(value="RETURN")})
    private void injectLightningShaderPost(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (this.renderingLightningShader) {
            return;
        }
        if (entity instanceof LightningBolt && ShaderModule.INSTANCE != null && Aevum.SHADER != null && ShaderModule.INSTANCE.shouldRenderLighting()) {
            this.renderingLightningShader = true;
            Aevum.SHADER.applyShader(() -> {
                MultiBufferSource.MultiBufferSource$BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
                this.method_22977(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, (MultiBufferSource)immediate);
                immediate.endBatch();
            }, ShaderModule.INSTANCE.mode.getValue());
            this.renderingLightningShader = false;
        }
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal=0), require=0)
    void replaceShaderHook(PostChain instance, float tickDelta) {
        ShaderManager.Shader shaders = ShaderModule.INSTANCE.mode.getValue();
        if (ShaderModule.INSTANCE.isOn() && Wrapper.mc.level != null) {
            Aevum.SHADER.setupShader(shaders, Aevum.SHADER.getShaderOutline(shaders));
        } else {
            instance.method_1258(tickDelta);
        }
    }

    @Redirect(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/WorldRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V"))
    private void skyShaderHook(LevelRenderer instance, Matrix4f viewMatrix, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable runnable) {
        if (Aevum.SHADER != null && Ambience.INSTANCE != null && Ambience.INSTANCE.shouldRenderSkyShader()) {
            Aevum.SHADER.applySky(() -> instance.method_3257(viewMatrix, projectionMatrix, tickDelta, camera, thickFog, runnable));
            RenderSystem.disableDepthTest();
            RenderSystem.clear((int)256, (boolean)Minecraft.ON_OSX);
            RenderSystem.enableDepthTest();
            return;
        }
        instance.method_3257(viewMatrix, projectionMatrix, tickDelta, camera, thickFog, runnable);
    }

    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index=3)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return Freecam.INSTANCE.isOn() || spectator;
    }
}

