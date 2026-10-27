/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SimpleSoundInstance
 *  net.minecraft.SoundInstance
 *  net.minecraft.GuiGraphics
 *  net.minecraft.SoundEvents
 *  net.minecraft.Holder
 */
package dev.aevum.mod.gui.items;

import dev.aevum.api.utils.i18n.Lang;
import dev.aevum.api.utils.math.Animation;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.Mod;
import dev.aevum.mod.modules.impl.client.ClickGui;
import dev.aevum.mod.modules.impl.client.HoverSound;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.SimpleSoundInstance;
import net.minecraft.SoundInstance;
import net.minecraft.GuiGraphics;
import net.minecraft.SoundEvents;
import net.minecraft.Holder;

public class Item
extends Mod {
    public static GuiGraphics context;
    protected float x;
    protected float y;
    protected int width;
    protected int height;
    private boolean hidden;
    private final Animation visibleHeightAnimation = new Animation();

    public Item(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return Lang.tr(super.getName());
    }

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static void sound() {
        if (HoverSound.playToggle()) {
            return;
        }
        if (ClickGui.getInstance().sound.getValue()) {
            mc.getSoundManager().method_4873((SoundInstance)SimpleSoundInstance.method_47978((Holder)SoundEvents.UI_BUTTON_CLICK, (float)ClickGui.getInstance().soundPitch.getValueFloat()));
        }
    }

    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
    }

    public void update() {
    }

    public void onKeyTyped(char typedChar, int keyCode) {
    }

    public void onKeyPressed(int key) {
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public double getVisibleHeight() {
        double target = this.hidden ? 0.0 : (double)this.getHeight();
        return this.visibleHeightAnimation.get(target, 200L, Easing.CubicInOut);
    }

    protected double getColorDelay() {
        return (double)this.y / 10.0;
    }

    protected void drawString(String text, double x, double y, Color color) {
        this.drawString(text, x, y, color.hashCode());
    }

    protected void drawString(String text, double x, double y, int color) {
        boolean shadow = FontManager.isShadowEnabled();
        if (FontManager.isCustomFontEnabled()) {
            FontManager.draw(context.method_51448(), text, (int)x, (int)y, color, shadow);
        } else {
            context.method_51433(Item.mc.font, text, (int)x, (int)y, color, shadow);
        }
    }

    protected int getFontHeight() {
        if (FontManager.isCustomFontEnabled()) {
            return (int)FontManager.ui.getFontHeight();
        }
        Objects.requireNonNull(Item.mc.font);
        return 9;
    }

    protected float getCenteredTextY(float baseY, float boxHeight) {
        return baseY + (boxHeight - (float)this.getFontHeight()) / 2.0f + (float)ClickGui.getInstance().textOffset.getValueInt();
    }

    protected int getWidth(String s) {
        if (FontManager.isCustomFontEnabled()) {
            return (int)FontManager.ui.getWidth(s);
        }
        return Item.mc.font.method_1727(s);
    }
}

