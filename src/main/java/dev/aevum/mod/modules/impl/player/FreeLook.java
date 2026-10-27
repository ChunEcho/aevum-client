/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.LookDirectionEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.mod.modules.Module;
import net.minecraft.PoseStack;

public class FreeLook
extends Module {
    public static FreeLook INSTANCE;
    private float fakeYaw;
    private float fakePitch;
    private float prevFakeYaw;
    private float prevFakePitch;

    public FreeLook() {
        super("FreeLook", Module.Category.Player);
        this.setChinese("\u81ea\u7531\u89c6\u89d2");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (FreeLook.nullCheck()) {
            this.disable();
            return;
        }
        this.fakePitch = FreeLook.mc.player.getXRot();
        this.fakeYaw = FreeLook.mc.player.getYRot();
        this.prevFakePitch = this.fakePitch;
        this.prevFakeYaw = this.fakeYaw;
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event) {
        this.fakeYaw += (float)event.getCursorDeltaX() * 0.15f;
        this.fakePitch += (float)event.getCursorDeltaY() * 0.15f;
        event.cancel();
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        this.prevFakeYaw = this.fakeYaw;
        this.prevFakePitch = this.fakePitch;
    }

    public float getFakeYaw() {
        return MathUtil.interpolate(this.prevFakeYaw, this.fakeYaw, mc.getTimer().method_60637(true));
    }

    public float getFakePitch() {
        return MathUtil.interpolate(this.prevFakePitch, this.fakePitch, mc.getTimer().method_60637(true));
    }
}

