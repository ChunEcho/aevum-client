/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.GuiGraphics;

public class WelcomerHud
extends Module
implements HudElement {
    public static WelcomerHud INSTANCE;
    private int hudX = -1;
    private int hudY = 2;
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

    public WelcomerHud() {
        super("Welcomer", Module.Category.HUD);
        this.setChinese("\u6b22\u8fce\u8bed");
        INSTANCE = this;
    }

    private String getText() {
        if (WelcomerHud.mc.player == null) {
            return "Welcome :')";
        }
        return "Welcome, \u00a7f" + WelcomerHud.mc.player.method_5477().getString() + " \u00a7r:')";
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        this.renderElement(drawContext, tickDelta);
    }

    @Override
    public String getElementName() {
        return this.getChinese() != null ? this.getChinese() : this.getName();
    }

    @Override
    public int getElementX() {
        if (this.hudX < 0) {
            return mc.getWindow().getGuiScaledWidth() / 2 - this.getElementWidth() / 2;
        }
        return this.hudX;
    }

    @Override
    public void setElementX(int x) {
        this.hudX = x;
    }

    @Override
    public int getElementY() {
        return this.hudY;
    }

    @Override
    public void setElementY(int y) {
        this.hudY = y;
    }

    @Override
    public int getElementWidth() {
        return (int)FontManager.getWidth(this.getText()) + 4;
    }

    @Override
    public int getElementHeight() {
        return (int)FontManager.getFontHeight() + 2;
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        TextUtil.drawString(context, this.getText(), this.getElementX(), this.getElementY(), this.color.getValue().getRGB(), this.shadow.getValue());
    }
}

