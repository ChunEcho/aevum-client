/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.api.events.impl;

import net.minecraft.GuiGraphics;

public class Render2DEvent {
    private static final Render2DEvent INSTANCE = new Render2DEvent();
    public GuiGraphics drawContext;
    public float tickDelta;

    public static Render2DEvent get(GuiGraphics drawContext, float tickDelta) {
        Render2DEvent.INSTANCE.drawContext = drawContext;
        Render2DEvent.INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}

