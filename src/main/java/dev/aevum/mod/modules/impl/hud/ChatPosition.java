/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.GuiGraphics;

public class ChatPosition
extends Module
implements HudElement {
    public static ChatPosition INSTANCE;
    public final SliderSetting x = this.add(new SliderSetting("X", 2.0, -500.0, 2000.0, 1.0));
    public final SliderSetting y = this.add(new SliderSetting("Y", 2.0, -500.0, 2000.0, 1.0));
    public final SliderSetting width = this.add(new SliderSetting("Width", 320.0, 40.0, 2000.0, 1.0));
    public final SliderSetting height = this.add(new SliderSetting("Height", 180.0, 20.0, 2000.0, 1.0));

    public ChatPosition() {
        super("ChatPosition", Module.Category.HUD);
        this.setChinese("\u804a\u5929\u4f4d\u7f6e");
        INSTANCE = this;
    }

    public static boolean shouldEdit() {
        return INSTANCE != null && INSTANCE.isOn();
    }

    public static int getXPos() {
        return ChatPosition.shouldEdit() ? ChatPosition.INSTANCE.x.getValueInt() : 2;
    }

    public static int getYPos() {
        return ChatPosition.shouldEdit() ? ChatPosition.INSTANCE.y.getValueInt() : 2;
    }

    public static int getWidthValue() {
        return ChatPosition.shouldEdit() ? ChatPosition.INSTANCE.width.getValueInt() : 320;
    }

    public static int getHeightValue() {
        return ChatPosition.shouldEdit() ? ChatPosition.INSTANCE.height.getValueInt() : 180;
    }

    @Override
    public String getElementName() {
        return this.getChinese() != null ? this.getChinese() : this.getName();
    }

    @Override
    public int getElementX() {
        return this.x.getValueInt();
    }

    @Override
    public void setElementX(int x) {
        this.x.setValue(x);
    }

    @Override
    public int getElementY() {
        return this.y.getValueInt();
    }

    @Override
    public void setElementY(int y) {
        this.y.setValue(y);
    }

    @Override
    public int getElementWidth() {
        return this.width.getValueInt();
    }

    @Override
    public int getElementHeight() {
        return this.height.getValueInt();
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public void renderElement(GuiGraphics context, float tickDelta) {
        int alpha;
        int x = this.getElementX();
        int y = this.getElementY();
        int w = this.getElementWidth();
        int h = this.getElementHeight();
        context.method_25294(x, y, x + w, y + h, 0x35000000);
        int lineX = x + 4;
        int lineY = y + h - 14;
        int lineHeight = 10;
        for (int i = 0; i < 6 && (alpha = 80 - i * 10) > 0; ++i) {
            int msgWidth = Math.max(40, w - 20 - i * 18);
            context.method_25294(lineX, lineY - i * lineHeight, lineX + msgWidth, lineY - i * lineHeight + 9, alpha << 24);
        }
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
    }
}

