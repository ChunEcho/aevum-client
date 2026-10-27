/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.mod.modules;

import net.minecraft.GuiGraphics;

public interface HudElement {
    public String getElementName();

    public int getElementX();

    public void setElementX(int var1);

    public int getElementY();

    public void setElementY(int var1);

    public int getElementWidth();

    public int getElementHeight();

    default public int getDragBoundsHeight() {
        return this.getElementHeight();
    }

    public boolean isElementVisible();

    public void renderElement(GuiGraphics var1, float var2);

    default public boolean isDraggable() {
        return true;
    }

    default public AlignMode getAlignMode() {
        return AlignMode.LEFT;
    }

    default public void setAlignMode(AlignMode mode) {
    }

    default public int getRightAnchorX() {
        return this.getElementX() + this.getElementWidth();
    }

    default public void setRightAnchorX(int rightX) {
    }

    public static enum AlignMode {
        LEFT,
        RIGHT;

    }
}

