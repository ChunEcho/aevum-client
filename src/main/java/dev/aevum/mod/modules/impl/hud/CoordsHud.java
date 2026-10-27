/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Level
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
import net.minecraft.Level;
import net.minecraft.GuiGraphics;

public class CoordsHud
extends Module
implements HudElement {
    public static CoordsHud INSTANCE;
    private int hudX = 2;
    private int hudY = -1;
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

    public CoordsHud() {
        super("Coords", Module.Category.HUD);
        this.setChinese("\u5750\u6807\u663e\u793a");
        INSTANCE = this;
    }

    private String getText() {
        if (CoordsHud.mc.player == null || CoordsHud.mc.level == null) {
            return "XYZ \u00a7f0, 0, 0";
        }
        boolean inNether = CoordsHud.mc.level.dimension().equals(Level.NETHER);
        int pX = CoordsHud.mc.player.getBlockX();
        int pY = CoordsHud.mc.player.getBlockY();
        int pZ = CoordsHud.mc.player.getBlockZ();
        float factor = !inNether ? 0.125f : 8.0f;
        int aX = (int)(CoordsHud.mc.player.getX() * (double)factor);
        int aZ = (int)(CoordsHud.mc.player.getZ() * (double)factor);
        return "XYZ \u00a7f" + pX + ", " + pY + ", " + pZ + " \u00a77[\u00a7f" + aX + ", " + aZ + "\u00a77]";
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
        return this.hudX;
    }

    @Override
    public void setElementX(int x) {
        this.hudX = x;
    }

    @Override
    public int getElementY() {
        if (this.hudY < 0) {
            return mc.getWindow().getGuiScaledHeight() - (int)FontManager.getFontHeight() - 2;
        }
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

