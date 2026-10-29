package dev.chun.mod.modules.impl.render;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.AnimateUtil;
import dev.chun.api.utils.math.Easing;
import dev.chun.api.utils.math.FadeUtils;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

public class Camera extends Module {
   public static Camera INSTANCE;
   public final BooleanSetting cameraClipEnabled = this.add(new BooleanSetting("CameraClip", false));
   public final SliderSetting clipDistance = this.add(new SliderSetting("Clip Distance", 4.0, 1.0, 20.0));
   public final SliderSetting clipAnimateTime = this.add(new SliderSetting("Clip AnimationTime", 200, 0, 1000));
   private final EnumSetting<Easing> clipEase = this.add(new EnumSetting<>("Clip Ease", Easing.CubicInOut));
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
   private static final Identifier MOTION_BLUR_SHADER_ID = Identifier.of("motionblur", "shaders/post/motion_blur.json");
   public final BooleanSetting motionBlurEnabled = this.add(new BooleanSetting("MotionBlur", false).setParent());
   public final SliderSetting blurAmount = this.add(new SliderSetting("Blur Amount", 50.0, 0.0, 100.0, 1.0, this.motionBlurEnabled::isOpen));
   public final BooleanSetting blurDynamicUpdate = this.add(new BooleanSetting("Blur DynamicUpdate", true, this.motionBlurEnabled::isOpen));
   private ManagedShaderEffect motionBlurShader;
   private float currentBlurValue = -1.0F;

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
      if (!nullCheck() && this.motionCameraEnabled.getValue()) {
         this.fakeX = mc.player.getX();
         this.fakeY = mc.player.getY() + (double)mc.player.getEyeHeight(mc.player.getPose());
         this.fakeZ = mc.player.getZ();
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
      if (!event.isPre() && !nullCheck()) {
         if (this.isMotionCameraOn()) {
            this.prevFakeX = this.fakeX;
            this.prevFakeY = this.fakeY;
            this.prevFakeZ = this.fakeZ;
            double speed = mc.options.getPerspective().isFirstPerson() ? this.motionFirstPersonSpeed.getValue() : this.motionSpeed.getValue();
            this.fakeX = AnimateUtil.animate(this.fakeX, mc.player.getX(), speed);
            this.fakeY = AnimateUtil.animate(this.fakeY, mc.player.getY() + (double)mc.player.getEyeHeight(mc.player.getPose()), speed);
            this.fakeZ = AnimateUtil.animate(this.fakeZ, mc.player.getZ(), speed);
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
   public void onRender3D(MatrixStack matrixStack) {
      if (this.isCameraClipOn()) {
         if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && this.clipNoFront.getValue()) {
            mc.options.setPerspective(Perspective.FIRST_PERSON);
         }

         this.clipAnimation.setLength((long)this.clipAnimateTime.getValueInt());
         if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
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
      if (this.isMotionBlurOn() && this.motionBlurShader != null) {
         float blurFactor = this.getBlurFactor();
         if (!(blurFactor <= 0.0F)) {
            if (this.blurDynamicUpdate.getValue() || this.currentBlurValue != blurFactor) {
               this.updateBlurShaderUniform();
            }

            this.motionBlurShader.render(tickDelta);
         }
      }
   }

   private void initMotionBlurShader() {
      try {
         this.motionBlurShader = ShaderEffectManager.getInstance()
            .manage(MOTION_BLUR_SHADER_ID, shader -> shader.setUniformValue("BlendFactor", this.getBlurFactor()));
         this.updateBlurShaderUniform();
      } catch (Exception var2) {
         var2.printStackTrace();
         this.motionBlurShader = null;
      }
   }

   private void disposeMotionBlurShader() {
      this.motionBlurShader = null;
      this.currentBlurValue = -1.0F;
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
      amount = Math.min(amount, 99.0F);
      return amount / 100.0F;
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
      return this.isOn() && this.motionCameraEnabled.getValue() && (!this.motionNoFirstPerson.getValue() || !mc.options.getPerspective().isFirstPerson());
   }

   public boolean isMotionBlurOn() {
      return this.isOn() && this.motionBlurEnabled.getValue();
   }

   public double getClipDistance() {
      double quad = mc.options.getPerspective() == Perspective.FIRST_PERSON
         ? 1.0 - this.clipAnimation.ease(this.clipEase.getValue())
         : this.clipAnimation.ease(this.clipEase.getValue());
      return this.clipDistance.getValue() * quad;
   }

   public double getMotionFakeX() {
      return MathUtil.interpolate(this.prevFakeX, this.fakeX, (double)mc.getRenderTickCounter().getTickDelta(true));
   }

   public double getMotionFakeY() {
      return MathUtil.interpolate(this.prevFakeY, this.fakeY, (double)mc.getRenderTickCounter().getTickDelta(true));
   }

   public double getMotionFakeZ() {
      return MathUtil.interpolate(this.prevFakeZ, this.fakeZ, (double)mc.getRenderTickCounter().getTickDelta(true));
   }

   public void setBlurAmount(int amount) {
      this.blurAmount.setValue((double)Math.min(Math.max(amount, 0), 100));
      if (this.isMotionBlurOn()) {
         this.updateBlurShaderUniform();
      }
   }

   public int getBlurAmount() {
      return (int)this.blurAmount.getValue();
   }
}
