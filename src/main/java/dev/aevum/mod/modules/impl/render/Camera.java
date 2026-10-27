/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ResourceLocation
 *  net.minecraft.PoseStack
 *  net.minecraft.CameraType
 *  org.ladysnake.satin.api.managed.ManagedShaderEffect
 *  org.ladysnake.satin.api.managed.ShaderEffectManager
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.utils.math.AnimateUtil;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.math.FadeUtils;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.ResourceLocation;
import net.minecraft.PoseStack;
import net.minecraft.CameraType;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class Camera
extends Module {
    public static Camera INSTANCE;
    public final BooleanSetting cameraClipEnabled = this.add(new BooleanSetting("CameraClip", false));
    public final SliderSetting clipDistance = this.add(new SliderSetting("Clip Distance", 4.0, 1.0, 20.0));
    public final SliderSetting clipAnimateTime = this.add(new SliderSetting("Clip AnimationTime", 200, 0, 1000));
    private final EnumSetting<Easing> clipEase = this.add(new EnumSetting<Easing>("Clip Ease", Easing.CubicInOut));
    final FadeUtils clipAnimation = new FadeUtils(300L);
    private final BooleanSetting clipNoFront = this.add(new BooleanSetting("Clip NoFront", false));
    boolean clipFirst = false;
    public final BooleanSetting aspectRatioEnabled = this.add(new BooleanSetting("AspectRatio", false));
    public final SliderSetting ratio = this.add(new SliderSetting("Ratio", 1.78, 0.0, 5.0, 0.01));
    public final BooleanSetting fovEnabled = this.add(new BooleanSetting("Fov", false));
    public final SliderSetting fov = this.add(new SliderSetting("Fov Value", 90.0, 30.0, 170.0, 1.0));
    public final SliderSetting itemFov = this.add(new SliderSetting("ItemFov", 70.0, 30.0, 170.0, 1.0));
    public final BooleanSetting motionCameraEnabled = this.add(new BooleanSetting("MotionCamera", false));
    public final BooleanSetting motionNoFirstPerson = this.add(new BooleanSetting("Motion NoFirstPerson", true));
    public final SliderSetting motionFirstPersonSpeed = this.add(new SliderSetting("Motion FirstPersonSpeed", 0.6, 0.0, 1.0, 0.01));
    public final SliderSetting motionSpeed = this.add(new SliderSetting("Motion Speed", 0.3, 0.0, 1.0, 0.01));
    private double fakeX;
    private double fakeY;
    private double fakeZ;
    private double prevFakeX;
    private double prevFakeY;
    private double prevFakeZ;
    private static final ResourceLocation MOTION_BLUR_SHADER_ID;
    public final BooleanSetting motionBlurEnabled = this.add(new BooleanSetting("MotionBlur", false).setParent());
    public final SliderSetting blurAmount = this.add(new SliderSetting("Blur Amount", 50.0, 0.0, 100.0, 1.0, this.motionBlurEnabled::isOpen));
    public final BooleanSetting blurDynamicUpdate = this.add(new BooleanSetting("Blur DynamicUpdate", true, this.motionBlurEnabled::isOpen));
    private ManagedShaderEffect motionBlurShader;
    private float currentBlurValue = -1.0f;

    public Camera() {
        super("Camera", Module.Category.Render);
        this.setChinese("\u6444\u50cf\u673a");
        INSTANCE = this;
        this.blurAmount.injectTask(() -> {
            if (this.isMotionBlurOn() && !this.blurDynamicUpdate.getValue()) {
                this.updateBlurShaderUniform();
            }
        });
    }

    @Override
    public void onEnable() {
        if (!Camera.nullCheck() && this.motionCameraEnabled.getValue()) {
            this.fakeX = Camera.mc.player.getX();
            this.fakeY = Camera.mc.player.getY() + (double)Camera.mc.player.method_18381(Camera.mc.player.getPose());
            this.fakeZ = Camera.mc.player.getZ();
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
        }
        if (this.motionBlurEnabled.getValue()) {
            this.initMotionBlurShader();
        }
    }

    @Override
    public void onDisable() {
        this.disposeMotionBlurShader();
    }

    @EventListener
    public void onUpdate(ClientTickEvent event) {
        if (!event.isPre() && !Camera.nullCheck()) {
            if (this.isMotionCameraOn()) {
                this.prevFakeX = this.fakeX;
                this.prevFakeY = this.fakeY;
                this.prevFakeZ = this.fakeZ;
                double speed = Camera.mc.options.getCameraType().isFirstPerson() ? this.motionFirstPersonSpeed.getValue() : this.motionSpeed.getValue();
                this.fakeX = AnimateUtil.animate(this.fakeX, Camera.mc.player.getX(), speed);
                this.fakeY = AnimateUtil.animate(this.fakeY, Camera.mc.player.getY() + (double)Camera.mc.player.method_18381(Camera.mc.player.getPose()), speed);
                this.fakeZ = AnimateUtil.animate(this.fakeZ, Camera.mc.player.getZ(), speed);
            }
            if (this.motionBlurEnabled.getValue()) {
                if (this.motionBlurShader == null) {
                    this.initMotionBlurShader();
                }
            } else if (this.motionBlurShader != null) {
                this.disposeMotionBlurShader();
            }
        }
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (this.isCameraClipOn()) {
            if (Camera.mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT && this.clipNoFront.getValue()) {
                Camera.mc.options.method_31043(CameraType.FIRST_PERSON);
            }
            this.clipAnimation.setLength(this.clipAnimateTime.getValueInt());
            if (Camera.mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                if (!this.clipFirst) {
                    this.clipFirst = true;
                    this.clipAnimation.reset();
                }
            } else if (this.clipFirst) {
                this.clipFirst = false;
                this.clipAnimation.reset();
            }
        }
    }

    public void renderMotionBlur(float tickDelta) {
        if (!this.isMotionBlurOn() || this.motionBlurShader == null) {
            return;
        }
        float blurFactor = this.getBlurFactor();
        if (blurFactor <= 0.0f) {
            return;
        }
        if (this.blurDynamicUpdate.getValue() || this.currentBlurValue != blurFactor) {
            this.updateBlurShaderUniform();
        }
        this.motionBlurShader.render(tickDelta);
    }

    private void initMotionBlurShader() {
        try {
            this.motionBlurShader = ShaderEffectManager.getInstance().manage(MOTION_BLUR_SHADER_ID, shader -> shader.setUniformValue("BlendFactor", this.getBlurFactor()));
            this.updateBlurShaderUniform();
        }
        catch (Exception e) {
            e.printStackTrace();
            this.motionBlurShader = null;
        }
    }

    private void disposeMotionBlurShader() {
        this.motionBlurShader = null;
        this.currentBlurValue = -1.0f;
    }

    private void updateBlurShaderUniform() {
        if (this.motionBlurShader != null) {
            float factor = this.getBlurFactor();
            this.motionBlurShader.setUniformValue("BlendFactor", factor);
            this.currentBlurValue = factor;
        }
    }

    private float getBlurFactor() {
        float amount = (float)this.blurAmount.getValue();
        amount = Math.min(amount, 99.0f);
        return amount / 100.0f;
    }

    public boolean isCameraClipOn() {
        return this.isOn() && this.cameraClipEnabled.getValue();
    }

    public boolean isAspectRatioOn() {
        return this.isOn() && this.aspectRatioEnabled.getValue();
    }

    public boolean isFovOn() {
        return this.isOn() && this.fovEnabled.getValue();
    }

    public boolean isMotionCameraOn() {
        return this.isOn() && this.motionCameraEnabled.getValue() && (!this.motionNoFirstPerson.getValue() || !Camera.mc.options.getCameraType().isFirstPerson());
    }

    public boolean isMotionBlurOn() {
        return this.isOn() && this.motionBlurEnabled.getValue();
    }

    public double getClipDistance() {
        double quad = Camera.mc.options.getCameraType() == CameraType.FIRST_PERSON ? 1.0 - this.clipAnimation.ease(this.clipEase.getValue()) : this.clipAnimation.ease(this.clipEase.getValue());
        return this.clipDistance.getValue() * quad;
    }

    public double getMotionFakeX() {
        return MathUtil.interpolate(this.prevFakeX, this.fakeX, (double)mc.getTimer().method_60637(true));
    }

    public double getMotionFakeY() {
        return MathUtil.interpolate(this.prevFakeY, this.fakeY, (double)mc.getTimer().method_60637(true));
    }

    public double getMotionFakeZ() {
        return MathUtil.interpolate(this.prevFakeZ, this.fakeZ, (double)mc.getTimer().method_60637(true));
    }

    public void setBlurAmount(int amount) {
        this.blurAmount.setValue(Math.min(Math.max(amount, 0), 100));
        if (this.isMotionBlurOn()) {
            this.updateBlurShaderUniform();
        }
    }

    public int getBlurAmount() {
        return (int)this.blurAmount.getValue();
    }

    static {
        MOTION_BLUR_SHADER_ID = ResourceLocation.method_60655((String)"motionblur", (String)"shaders/post/motion_blur.json");
    }
}

