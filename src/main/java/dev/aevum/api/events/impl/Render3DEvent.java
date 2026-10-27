/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.AABB
 *  net.minecraft.PoseStack
 */
package dev.aevum.api.events.impl;

import dev.aevum.api.utils.render.Render3DUtil;
import java.awt.Color;
import net.minecraft.AABB;
import net.minecraft.PoseStack;

public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();
    public PoseStack matrixStack;
    public float tickDelta;

    public static Render3DEvent get(PoseStack matrixStack, float tickDelta) {
        Render3DEvent.INSTANCE.matrixStack = matrixStack;
        Render3DEvent.INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }

    public void drawBox(AABB box, Color color) {
        Render3DUtil.drawBox(this.matrixStack, box, color);
    }

    public void drawFill(AABB box, Color color) {
        Render3DUtil.drawFill(this.matrixStack, box, color);
    }
}

