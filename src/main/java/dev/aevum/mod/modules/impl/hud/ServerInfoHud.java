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
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import java.awt.Color;
import net.minecraft.GuiGraphics;

public class ServerInfoHud
extends Module
implements HudElement {
    public static ServerInfoHud INSTANCE;
    private int hudX = 2;
    private int hudY = 110;
    private final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.IP));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));

    public ServerInfoHud() {
        super("ServerInfo", Module.Category.HUD);
        this.setChinese("\u670d\u52a1\u5668\u4fe1\u606f");
        INSTANCE = this;
    }

    private String getText() {
        if (this.mode.getValue() == Mode.IP) {
            return "Server \u00a7f" + (mc.isLocalServer() ? "SinglePlayer" : (mc.getCurrentServer() != null ? ServerInfoHud.mc.getCurrentServer().ip : "Unknown"));
        }
        return "Brand \u00a7f" + (mc.isLocalServer() ? "Vanilla" : (mc.getConnection() != null ? mc.getConnection().serverBrand().replaceAll("\\(.*?\\)", "") : "Unknown"));
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
        TextUtil.drawString(context, this.getText(), this.hudX, this.hudY, this.color.getValue().getRGB(), this.shadow.getValue());
    }

    private static enum Mode {
        IP,
        Brand;

    }
}

