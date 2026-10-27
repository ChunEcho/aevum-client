/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.Minecraft
 *  net.minecraft.GuiGraphics
 *  net.minecraft.InputConstants
 *  net.minecraft.TextFieldHelper
 */
package dev.aevum.mod.gui.items.buttons;

import dev.aevum.api.utils.i18n.Lang;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.gui.items.buttons.Button;
import dev.aevum.mod.modules.impl.client.ClickGui;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.Minecraft;
import net.minecraft.GuiGraphics;
import net.minecraft.InputConstants;
import net.minecraft.TextFieldHelper;

public class StringButton
extends Button {
    private static final Timer idleTimer = new Timer();
    private static boolean idle;
    private final StringSetting setting;
    public boolean isListening;
    private String currentString = "";

    public StringButton(StringSetting setting) {
        super(setting.getName());
        this.setting = setting;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    public static String getIdleSign() {
        if (idleTimer.passed(500L)) {
            idle = !idle;
            idleTimer.reset();
        }
        if (idle) {
            return "_";
        }
        return "";
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        double baseDelay = this.getColorDelay();
        float w = (float)this.width + 7.0f;
        float h = (float)this.height - 0.5f;
        if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum && this.getState()) {
            int a = !hovered ? ClickGui.getInstance().alpha.getValueInt() : ClickGui.getInstance().hoverAlpha.getValueInt();
            Render2DUtil.drawLutRect(context.method_51448(), this.x, this.y, w, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), a);
        } else {
            Color color = ClickGui.getInstance().getColor(baseDelay);
            Render2DUtil.rect(context.method_51448(), this.x, this.y, this.x + w, this.y + (float)this.height - 0.5f, this.getState() ? (!hovered ? ColorUtil.injectAlpha(color, ClickGui.getInstance().alpha.getValueInt()).getRGB() : ColorUtil.injectAlpha(color, ClickGui.getInstance().hoverAlpha.getValueInt()).getRGB()) : (!hovered ? defaultColor : hoverColor));
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (this.isListening) {
            this.drawString(this.currentString + StringButton.getIdleSign(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        } else if (this.isHovering(mouseX, mouseY) && InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)340)) {
            this.drawString("Reset Default", (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(Lang.tr(this.setting.getName()) + ": " + String.valueOf(ChatFormatting.GRAY) + this.setting.getValue(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)340)) {
            this.isListening = false;
            this.currentString = "";
            this.setting.setValue(this.setting.getDefaultValue());
            StringButton.sound();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.isListening) {
            this.setString(this.currentString + typedChar);
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (this.isListening) {
            switch (key) {
                case 256: {
                    this.isListening = false;
                    break;
                }
                case 257: 
                case 335: {
                    this.enterString();
                    break;
                }
                case 86: {
                    if (!InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)341)) break;
                    this.setString(this.currentString + TextFieldHelper.method_27556((Minecraft)mc));
                    break;
                }
                case 259: {
                    this.setString(StringButton.removeLastChar(this.currentString));
                }
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    private void enterString() {
        if (this.currentString.isEmpty()) {
            this.setting.setValue(this.setting.getDefaultValue());
        } else {
            this.setting.setValue(this.currentString);
        }
        this.onMouseClick();
    }

    @Override
    public void toggle() {
        this.setString(this.setting.getValue());
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }

    public void setString(String newString) {
        this.currentString = newString;
    }
}

