/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Minecraft
 */
package dev.aevum.api.utils.camera;

import net.minecraft.Minecraft;

public class CameraState {
    public float lookYaw;
    public float lookPitch;
    public float transitionInitialYaw;
    public float transitionInitialPitch;
    public boolean doLock;
    public boolean doTransition;

    public float originalYaw() {
        return Minecraft.getInstance().getCameraEntity().getYHeadRot();
    }

    public float originalPitch() {
        return Minecraft.getInstance().getCameraEntity().getXRot();
    }
}

