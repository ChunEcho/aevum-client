/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec3
 *  net.minecraft.Mth
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.events.Event;
import net.minecraft.LivingEntity;
import net.minecraft.Vec3;
import net.minecraft.Mth;

public class FireworkShooterRotationEvent
extends Event {
    private static final FireworkShooterRotationEvent instance = new FireworkShooterRotationEvent();
    public LivingEntity shooter;
    public float pitch;
    public float yaw;

    private FireworkShooterRotationEvent() {
    }

    public static FireworkShooterRotationEvent get(LivingEntity shooter, float yaw, float pitch) {
        FireworkShooterRotationEvent.instance.shooter = shooter;
        FireworkShooterRotationEvent.instance.yaw = yaw;
        FireworkShooterRotationEvent.instance.pitch = pitch;
        instance.setCancelled(false);
        return instance;
    }

    public final Vec3 getRotationVector() {
        float f = this.pitch * ((float)Math.PI / 180);
        float g = -this.yaw * ((float)Math.PI / 180);
        float h = Mth.method_15362((float)g);
        float i = Mth.method_15374((float)g);
        float j = Mth.method_15362((float)f);
        float k = Mth.method_15374((float)f);
        return new Vec3((double)(i * j), (double)(-k), (double)(h * j));
    }
}

