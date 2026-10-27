/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.api.events.impl;

import net.minecraft.GuiGraphics;

public class PreRender2DEvent {
    private static final PreRender2DEvent INSTANCE = new PreRender2DEvent();
    public GuiGraphics drawContext;

    public static PreRender2DEvent get(GuiGraphics drawContext) {
        PreRender2DEvent.INSTANCE.drawContext = drawContext;
        return INSTANCE;
    }
}

