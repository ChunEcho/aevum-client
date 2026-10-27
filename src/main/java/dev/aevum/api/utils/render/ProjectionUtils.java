/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3
 *  net.minecraft.Minecraft
 *  net.minecraft.Camera
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package dev.aevum.api.utils.render;

import net.minecraft.Vec3;
import net.minecraft.Minecraft;
import net.minecraft.Camera;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class ProjectionUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static Vector3f project(Vec3 worldPos) {
        Camera camera = ProjectionUtils.mc.gameRenderer.method_19418();
        Vec3 camPos = camera.getPosition();
        Vector3f target = new Vector3f((float)(worldPos.x - camPos.x), (float)(worldPos.y - camPos.y), (float)(worldPos.z - camPos.z));
        Quaternionf rotation = new Quaternionf((Quaternionfc)camera.rotation());
        rotation.conjugate();
        target.rotate((Quaternionfc)rotation);
        double fov = ((Integer)ProjectionUtils.mc.options.fov().method_41753()).intValue();
        float fovRad = (float)Math.toRadians(fov);
        float aspectRatio = (float)mc.getWindow().getWidth() / (float)mc.getWindow().getHeight();
        float near = 0.05f;
        float far = 1000.0f;
        float tanHalfFov = (float)Math.tan(fovRad / 2.0f);
        float x = target.x / (tanHalfFov * aspectRatio);
        float y = target.y / tanHalfFov;
        float z = target.z;
        if (z >= 0.0f) {
            return null;
        }
        float halfWidth = (float)mc.getWindow().getGuiScaledWidth() / 2.0f;
        float halfHeight = (float)mc.getWindow().getGuiScaledHeight() / 2.0f;
        float screenX = halfWidth + x / -z * halfWidth;
        float screenY = halfHeight - y / -z * halfHeight;
        return new Vector3f(screenX, screenY, -z);
    }
}

