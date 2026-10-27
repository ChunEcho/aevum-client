/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PoseStack
 *  net.minecraft.CameraType
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.math.FadeUtils;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.PoseStack;
import net.minecraft.CameraType;

public class CameraClip
extends Module {
    public static CameraClip INSTANCE;
    public final SliderSetting getDistance = this.add(new SliderSetting("Distance", 4.0, 1.0, 20.0));
    public final SliderSetting animateTime = this.add(new SliderSetting("AnimationTime", 200, 0, 1000));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut));
    final FadeUtils animation = new FadeUtils(300L);
    private final BooleanSetting noFront = this.add(new BooleanSetting("NoFront", false));
    boolean first = false;

    public CameraClip() {
        super("CameraClip", Module.Category.Render);
        this.setChinese("\u6444\u50cf\u673a\u7a7f\u5899");
        INSTANCE = this;
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (CameraClip.mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT && this.noFront.getValue()) {
            CameraClip.mc.options.method_31043(CameraType.FIRST_PERSON);
        }
        this.animation.setLength(this.animateTime.getValueInt());
        if (CameraClip.mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            if (!this.first) {
                this.first = true;
                this.animation.reset();
            }
        } else if (this.first) {
            this.first = false;
            this.animation.reset();
        }
    }

    public double getDistance() {
        double quad = CameraClip.mc.options.getCameraType() == CameraType.FIRST_PERSON ? 1.0 - this.animation.ease(this.ease.getValue()) : this.animation.ease(this.ease.getValue());
        return this.getDistance.getValue() * quad - 1.0 + 1.0;
    }
}

