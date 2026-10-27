/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.Aevum;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.GuiGraphics;

public class SpeedHud
extends Module
implements HudElement {
    public static SpeedHud INSTANCE;
    private int hudX = 2;
    private int hudY = 86;
    private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
    private final DecimalFormat df = new DecimalFormat("0.0");

    public SpeedHud() {
        super("SpeedHud", Module.Category.HUD);
        this.setChinese("\u901f\u5ea6\u663e\u793a");
        INSTANCE = this;
    }

    private String getText() {
        if (SpeedHud.mc.player == null) {
            return "Speed \u00a7f0.0km/h";
        }
        double x = SpeedHud.mc.player.getX() - SpeedHud.mc.player.xo;
        double z = SpeedHud.mc.player.getZ() - SpeedHud.mc.player.zo;
        double dist = Math.sqrt(x * x + z * z) / 1000.0;
        double div = 1.388888888888889E-5;
        float timer = Aevum.TIMER.get();
        return "Speed \u00a7f" + this.df.format(dist / div * (double)timer) + "km/h";
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
        if (this.alignMode == HudElement.AlignMode.RIGHT) {
            return this.hudX - this.getElementWidth();
        }
        return this.hudX;
    }

    @Override
    public void setElementX(int x) {
        this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? x + this.getElementWidth() : x;
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
    public HudElement.AlignMode getAlignMode() {
        return this.alignMode;
    }

    @Override
    public void setAlignMode(HudElement.AlignMode mode) {
        if (mode == this.alignMode) {
            return;
        }
        if (mode == HudElement.AlignMode.RIGHT && this.alignMode == HudElement.AlignMode.LEFT) {
            this.hudX += this.getElementWidth();
        } else if (mode == HudElement.AlignMode.LEFT && this.alignMode == HudElement.AlignMode.RIGHT) {
            this.hudX -= this.getElementWidth();
        }
        this.alignMode = mode;
    }

    @Override
    public int getRightAnchorX() {
        if (this.alignMode == HudElement.AlignMode.RIGHT) {
            return this.hudX;
        }
        return this.hudX + this.getElementWidth();
    }

    @Override
    public void setRightAnchorX(int rightX) {
        this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? rightX : rightX - this.getElementWidth();
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        int renderX = this.getElementX();
        TextUtil.drawString(context, this.getText(), renderX, this.hudY, this.color.getValue().getRGB(), this.shadow.getValue());
    }
}

