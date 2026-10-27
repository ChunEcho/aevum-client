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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.minecraft.GuiGraphics;

public class TimeHud
extends Module
implements HudElement {
    public static TimeHud INSTANCE;
    private int anchorX = 2;
    private int hudY = 98;
    private boolean isRightAligned = false;
    private boolean needsInitAnchor = false;
    private int loadedX = 2;
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

    public TimeHud() {
        super("Time", Module.Category.HUD);
        this.setChinese("\u65f6\u95f4\u663e\u793a");
        INSTANCE = this;
    }

    private String getText() {
        return "Time \u00a7f" + new SimpleDateFormat("h:mm a", Locale.ENGLISH).format(new Date());
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
        if (this.isRightAligned) {
            return this.anchorX - this.getElementWidth();
        }
        return this.anchorX;
    }

    @Override
    public void setElementX(int x) {
        if (mc == null || mc.getWindow() == null) {
            this.loadedX = x;
            this.anchorX = x;
            this.needsInitAnchor = true;
            return;
        }
        this.needsInitAnchor = false;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int hudCenterX = x + this.getElementWidth() / 2;
        if (hudCenterX > screenWidth / 2) {
            this.isRightAligned = true;
            this.anchorX = x + this.getElementWidth();
        } else {
            this.isRightAligned = false;
            this.anchorX = x;
        }
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
        if (this.needsInitAnchor) {
            this.setElementX(this.loadedX);
        }
        TextUtil.drawString(context, this.getText(), this.getElementX(), this.hudY, this.color.getValue().getRGB(), this.shadow.getValue());
    }
}

