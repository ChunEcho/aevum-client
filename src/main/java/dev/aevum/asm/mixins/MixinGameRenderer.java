/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.ProjectileUtil
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.AABB
 *  net.minecraft.HitResult
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.Minecraft
 *  net.minecraft.Mth
 *  net.minecraft.BlockHitResult
 *  net.minecraft.EntityHitResult
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.FogType
 *  net.minecraft.GameRenderer
 *  net.minecraft.Axis
 *  net.minecraft.DeltaTracker
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.api.utils.world.InteractUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.player.Freecam;
import dev.aevum.mod.modules.impl.player.InteractTweaks;
import dev.aevum.mod.modules.impl.render.AspectRatio;
import dev.aevum.mod.modules.impl.render.Chams;
import dev.aevum.mod.modules.impl.render.Fov;
import dev.aevum.mod.modules.impl.render.HighLight;
import dev.aevum.mod.modules.impl.render.NoRender;
import dev.aevum.mod.modules.impl.render.Zoom;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.ProjectileUtil;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Position;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Minecraft;
import net.minecraft.Mth;
import net.minecraft.BlockHitResult;
import net.minecraft.EntityHitResult;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import net.minecraft.FogType;
import net.minecraft.GameRenderer;
import net.minecraft.Axis;
import net.minecraft.DeltaTracker;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameRenderer.class})
public class MixinGameRenderer {
    @Shadow
    @Final
    Minecraft minecraft;
    @Shadow
    private float fov;
    @Shadow
    private float oldFov;
    @Shadow
    private boolean panoramicMode;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float renderDistance;
    @Unique
    private Entity cameraEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalPitch;

    @Shadow
    private static HitResult method_56154(HitResult hitResult, Vec3 cameraPos, double interactionRange) {
        Vec3 vec3d = hitResult.getLocation();
        if (!vec3d.method_24802((Position)cameraPos, interactionRange)) {
            Vec3 vec3d2 = hitResult.getLocation();
            Direction direction = Direction.method_10142((double)(vec3d2.x - cameraPos.x), (double)(vec3d2.y - cameraPos.y), (double)(vec3d2.z - cameraPos.z));
            return BlockHitResult.method_17778((Vec3)vec3d2, (Direction)direction, (BlockPos)BlockPos.method_49638((Position)vec3d2));
        }
        return hitResult;
    }

    @Inject(method={"showFloatingItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.method_7909() == Items.TOTEM_OF_UNDYING && NoRender.INSTANCE.isOn() && NoRender.INSTANCE.totem.getValue()) {
            info.cancel();
        }
    }

    @Redirect(method={"renderWorld"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"), require=0)
    private float applyCameraTransformationsMathHelperLerpProxy(float delta, float first, float second) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.nausea.getValue()) {
            return 0.0f;
        }
        return Mth.method_16439((float)delta, (float)first, (float)second);
    }

    @Inject(method={"tiltViewWhenHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void tiltViewWhenHurtHook(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.hurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"shouldRenderBlockOutline"}, at={@At(value="HEAD")}, cancellable=true)
    public void hookOutline(CallbackInfoReturnable<Boolean> cir) {
        if (HighLight.INSTANCE.isOn()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode=180, ordinal=0)}, method={"renderWorld"})
    void render3dHook(DeltaTracker tickCounter, CallbackInfo ci) {
        if (Module.nullCheck()) {
            return;
        }
        Camera camera = Minecraft.getInstance().gameRenderer.method_19418();
        PoseStack matrixStack = new PoseStack();
        matrixStack.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        matrixStack.method_22907(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
        TextUtil.lastProjMat.set((Matrix4fc)RenderSystem.getProjectionMatrix());
        TextUtil.lastModMat.set((Matrix4fc)RenderSystem.getModelViewMatrix());
        TextUtil.lastWorldSpaceMatrix.set((Matrix4fc)matrixStack.last().method_23761());
        Aevum.FPS.record();
        Aevum.MODULE.render3D(matrixStack);
    }

    @Inject(at={@At(value="TAIL")}, method={"renderWorld"})
    void render3dTail(DeltaTracker tickCounter, CallbackInfo ci) {
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.hand.booleanValue) {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    @Inject(method={"getFov(Lnet/minecraft/client/render/Camera;FZ)D"}, at={@At(value="HEAD")}, cancellable=true)
    public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> ci) {
        if (!this.panoramicMode && (Fov.INSTANCE.isOn() || Zoom.on)) {
            FogType cameraSubmersionType;
            double d = 70.0;
            if (changingFov) {
                if (Fov.INSTANCE.isOn()) {
                    double fov = Fov.INSTANCE.fov.getValue();
                    if (Zoom.on) {
                        ci.setReturnValue((Object)Math.min(Math.max(fov - Zoom.INSTANCE.currentFov, 1.0), 177.0));
                    } else {
                        ci.setReturnValue((Object)fov);
                    }
                    return;
                }
                d = ((Integer)this.minecraft.options.fov().method_41753()).intValue();
                d *= (double)Mth.method_16439((float)tickDelta, (float)this.oldFov, (float)this.fov);
                if (Zoom.on) {
                    d = Math.min(Math.max(d - Zoom.INSTANCE.currentFov, 1.0), 177.0);
                }
            } else if (Fov.INSTANCE.isOn()) {
                ci.setReturnValue((Object)Fov.INSTANCE.itemFov.getValue());
                return;
            }
            if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isDeadOrDying()) {
                float f = Math.min((float)((LivingEntity)camera.getEntity()).deathTime + tickDelta, 20.0f);
                d /= (double)((1.0f - 500.0f / (f + 500.0f)) * 2.0f + 1.0f);
            }
            if ((cameraSubmersionType = camera.getFluidInCamera()) == FogType.LAVA || cameraSubmersionType == FogType.WATER) {
                d *= Mth.method_16436((double)((Double)this.minecraft.options.fovEffectScale().method_41753()), (double)1.0, (double)0.8571428656578064);
            }
            ci.setReturnValue((Object)d);
        }
    }

    @Inject(method={"getNightVisionStrength"}, at={@At(value="HEAD")}, cancellable=true)
    private static void getNightVisionStrengthHook(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        MobEffectInstance statusEffectInstance = entity.method_6112(MobEffects.NIGHT_VISION);
        if (statusEffectInstance == null) {
            cir.setReturnValue((Object)Float.valueOf(1.0f));
        }
    }

    @Inject(method={"renderWorld"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", shift=At.Shift.AFTER)})
    public void postRender3dHook(DeltaTracker tickCounter, CallbackInfo ci) {
        Aevum.SHADER.renderShaders();
    }

    @Inject(method={"getBasicProjectionMatrix"}, at={@At(value="TAIL")}, cancellable=true)
    public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (AspectRatio.INSTANCE.isOn()) {
            PoseStack matrixStack = new PoseStack();
            matrixStack.last().method_23761().identity();
            if (this.zoom != 1.0f) {
                matrixStack.method_46416(this.zoomX, -this.zoomY, 0.0f);
                matrixStack.method_22905(this.zoom, this.zoom, 1.0f);
            }
            matrixStack.last().method_23761().mul((Matrix4fc)new Matrix4f().setPerspective((float)(fov * 0.01745329238474369), AspectRatio.INSTANCE.ratio.getValueFloat(), 0.05f, this.renderDistance * 4.0f));
            cir.setReturnValue((Object)matrixStack.last().method_23761());
        }
    }

    @Inject(method={"findCrosshairTarget"}, at={@At(value="HEAD")}, cancellable=true)
    private void findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        if (Freecam.INSTANCE.isOn()) {
            cir.setReturnValue((Object)InteractUtil.getRtxTarget(Freecam.INSTANCE.getFakeYaw(), Freecam.INSTANCE.getFakePitch(), Freecam.INSTANCE.getFakeX(), Freecam.INSTANCE.getFakeY(), Freecam.INSTANCE.getFakeZ()));
            return;
        }
        double d = Math.max(blockInteractionRange, entityInteractionRange);
        double e = Mth.method_33723((double)d);
        Vec3 vec3d = camera.method_5836(tickDelta);
        InteractTweaks.INSTANCE.isActive = InteractTweaks.INSTANCE.ghostHand();
        HitResult hitResult = camera.method_5745(d, tickDelta, false);
        InteractTweaks.INSTANCE.isActive = false;
        double f = hitResult.getLocation().method_1025(vec3d);
        if (hitResult.method_17783() != HitResult.HitResult$Type.MISS) {
            e = f;
            d = Math.sqrt(f);
        }
        Vec3 vec3d2 = camera.method_5828(tickDelta);
        Vec3 vec3d3 = vec3d.method_1031(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        AABB box = camera.method_5829().method_18804(vec3d2.method_1021(d)).method_1009(1.0, 1.0, 1.0);
        if (!InteractTweaks.INSTANCE.noEntityTrace()) {
            EntityHitResult entityHitResult = ProjectileUtil.method_18075((Entity)camera, (Vec3)vec3d, (Vec3)vec3d3, (AABB)box, entity -> !entity.isSpectator() && entity.isPickable(), (double)e);
            cir.setReturnValue((Object)(entityHitResult != null && entityHitResult.getLocation().method_1025(vec3d) < f ? MixinGameRenderer.method_56154((HitResult)entityHitResult, vec3d, entityInteractionRange) : MixinGameRenderer.method_56154(hitResult, vec3d, blockInteractionRange)));
        } else {
            cir.setReturnValue((Object)MixinGameRenderer.method_56154(hitResult, vec3d, blockInteractionRange));
        }
    }
}

