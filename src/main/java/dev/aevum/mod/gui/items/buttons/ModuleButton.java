/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiGraphics
 *  net.minecraft.InputConstants
 *  net.minecraft.Axis
 */
package dev.aevum.mod.gui.items.buttons;

import dev.aevum.api.utils.i18n.Lang;
import dev.aevum.api.utils.math.Animation;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.gui.items.Item;
import dev.aevum.mod.gui.items.buttons.BindButton;
import dev.aevum.mod.gui.items.buttons.BooleanButton;
import dev.aevum.mod.gui.items.buttons.Button;
import dev.aevum.mod.gui.items.buttons.EnumButton;
import dev.aevum.mod.gui.items.buttons.PickerButton;
import dev.aevum.mod.gui.items.buttons.SliderButton;
import dev.aevum.mod.gui.items.buttons.StringButton;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClickGui;
import dev.aevum.mod.modules.impl.client.HoverSound;
import dev.aevum.mod.modules.settings.Setting;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.GuiGraphics;
import net.minecraft.InputConstants;
import net.minecraft.Axis;

public class ModuleButton
extends Button {
    private final Module module;
    private List<Item> items = new ArrayList<Item>();
    public boolean subOpen;
    public double itemHeight;
    public final Animation animation = new Animation();
    private final Animation hoverAnimation = new Animation();
    private boolean lastHovered = false;
    private final Animation toggleAnimation = new Animation();
    private final HashMap<BooleanSetting, Animation> parentOpenAnimations = new HashMap();

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.initSettings();
    }

    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<Item>();
        for (Setting setting : this.module.getSettings()) {
            Setting s;
            if (setting instanceof BooleanSetting) {
                s = (BooleanSetting)setting;
                newItems.add(new BooleanButton((BooleanSetting)s));
            }
            if (setting instanceof BindSetting) {
                s = (BindSetting)setting;
                newItems.add(new BindButton((BindSetting)s));
            }
            if (setting instanceof StringSetting) {
                s = (StringSetting)setting;
                newItems.add(new StringButton((StringSetting)s));
            }
            if (setting instanceof SliderSetting) {
                s = (SliderSetting)setting;
                newItems.add(new SliderButton((SliderSetting)s));
            }
            if (setting instanceof EnumSetting) {
                s = (EnumSetting)setting;
                newItems.add(new EnumButton((EnumSetting)s));
            }
            if (!(setting instanceof ColorSetting)) continue;
            s = (ColorSetting)setting;
            newItems.add(new PickerButton((ColorSetting)s));
        }
        this.items = newItems;
    }

    @Override
    public void update() {
        for (Item item : this.items) {
            item.update();
        }
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovering(mouseX, mouseY);
        if (hovered && !this.lastHovered) {
            HoverSound.hover();
        }
        this.lastHovered = hovered;
        boolean pressed = this.getState();
        double hoverProgress = this.hoverAnimation.get(hovered ? 1.0 : 0.0, 100L, Easing.CubicInOut);
        double toggleProgress = this.toggleAnimation.get(pressed ? 1.0 : 0.0, 160L, Easing.CubicInOut);
        double baseDelay = this.getColorDelay();
        Color defaultColor = ClickGui.getInstance().defaultColor.getValue();
        Color hoverColor = ClickGui.getInstance().hoverColor.getValue();
        Color idleFill = new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), defaultColor.getAlpha());
        int baseA = ClickGui.getInstance().alpha.getValueInt();
        int hoverA = ClickGui.getInstance().hoverAlpha.getValueInt();
        int accentA = Math.max(0, Math.min(255, (int)Math.round((double)baseA + (double)(hoverA - baseA) * hoverProgress)));
        Color hoverFill = new Color(hoverColor.getRed(), hoverColor.getGreen(), hoverColor.getBlue(), hoverA);
        Color unpressedFill = ColorUtil.fadeColor(idleFill, hoverFill, hoverProgress);
        float h = (float)this.height - 0.5f;
        float radius = Math.min(10.0f, Math.min((float)this.width, h) / 2.0f);
        if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            if (toggleProgress >= 0.999) {
                Render2DUtil.drawLutRect(context.method_51448(), this.x, this.y, this.width, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), accentA);
            } else {
                Render2DUtil.drawRect(context.method_51448(), this.x, this.y, (float)this.width, h, unpressedFill);
                int overlayA = (int)Math.round((double)accentA * toggleProgress);
                if (overlayA > 0) {
                    Render2DUtil.drawLutRect(context.method_51448(), this.x, this.y, this.width, h, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), overlayA);
                }
            }
        } else {
            Color accent = ClickGui.getInstance().getActiveColor(baseDelay);
            Color pressedFill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), accentA);
            Color baseFill = ColorUtil.fadeColor(unpressedFill, pressedFill, toggleProgress);
            Render2DUtil.drawRect(context.method_51448(), this.x, this.y, (float)this.width, h, baseFill);
        }
        float textY = this.getCenteredTextY(this.y, (float)this.height - 0.5f);
        if (hovered && InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)340)) {
            this.drawString(Lang.tr("Reset Default"), (double)(this.x + 2.3f), (double)textY, enableTextColor);
        } else {
            this.drawString(this.module.getDisplayName(), (double)(this.x + 2.3f), (double)textY, this.getState() ? enableTextColor : defaultTextColor);
        }
        if (ClickGui.getInstance().gear.booleanValue) {
            boolean expanded = this.subOpen || this.itemHeight > 0.0;
            switch (ClickGui.getInstance().expandIcon.getValue()) {
                case PlusMinus: {
                    this.drawString(this.subOpen ? "-" : "+", (double)(this.x + (float)this.width - 8.0f), (double)textY, ClickGui.getInstance().gear.getValue().getRGB());
                    break;
                }
                case Chevron: {
                    this.drawString(this.subOpen ? "v" : ">", (double)(this.x + (float)this.width - 8.0f), (double)textY, ClickGui.getInstance().gear.getValue().getRGB());
                    break;
                }
                case Gear: {
                    float expandProgress;
                    int gearColor = ClickGui.getInstance().gear.getValue().getRGB();
                    float gearW = FontManager.iconWidth("d");
                    float gearH = FontManager.iconHeight("d");
                    float centerX = this.x + (float)this.width - radius;
                    float centerY = this.y + h / 2.0f;
                    float gearX = centerX - gearW / 2.0f;
                    float gearY = centerY - gearH / 2.0f;
                    int totalItemHeight = this.getItemHeight();
                    float f = expandProgress = totalItemHeight <= 0 ? 0.0f : (float)Math.min(1.0, Math.max(0.0, this.itemHeight / (double)totalItemHeight));
                    if (expandProgress > 0.001f) {
                        float angle = (float)(System.currentTimeMillis() % 2000L) / 2000.0f * 360.0f * expandProgress;
                        context.method_51448().method_22903();
                        context.method_51448().method_46416(centerX, centerY, 0.0f);
                        context.method_51448().method_22907(Axis.ZP.rotationDegrees(angle));
                        FontManager.icon.drawString(context.method_51448(), "d", (double)(-gearW / 2.0f), (double)(-gearH / 2.0f), gearColor);
                        context.method_51448().method_22909();
                        break;
                    }
                    FontManager.icon.drawString(context.method_51448(), "d", (double)gearX, (double)gearY, gearColor);
                }
            }
        }
        if (this.subOpen || this.itemHeight > 0.0) {
            float expandProgress;
            double totalItemHeight = this.getVisibleItemHeight();
            double visibleItemHeight = Math.max(0.0, Math.min(this.itemHeight, totalItemHeight));
            float f = expandProgress = totalItemHeight <= 0.0 ? 0.0f : (float)(visibleItemHeight / totalItemHeight);
            if (this.subOpen && expandProgress < 0.9f) {
                for (Item item : this.items) {
                    if (!(item instanceof SliderButton)) continue;
                    ((SliderButton)item).resetFillAnimation();
                }
            }
            float slide = (1.0f - expandProgress) * 6.0f;
            if (ClickGui.getInstance().line.getValue() && visibleItemHeight > 0.01) {
                float yTop = this.y + (float)this.height - 0.5f;
                float yBottom = (float)((double)(this.y + (float)this.height) + visibleItemHeight - 0.5);
                float yBottomLine = (float)((double)(this.y + (float)this.height) + visibleItemHeight - (double)0.7f);
                double delayPerPixel = 0.25;
                float leftX = this.x + 0.6f;
                float rightX = this.x + (float)this.width - 0.6f;
                int alpha = Math.min(160, ClickGui.getInstance().topAlpha.getValueInt());
                if (ClickGui.getInstance().colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
                    float lineW = 1.0f;
                    Render2DUtil.drawLutRect(context.method_51448(), leftX, yTop, lineW, yBottom - yTop, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), alpha);
                    Render2DUtil.drawLutRect(context.method_51448(), rightX - lineW, yTop, lineW, yBottom - yTop, ClickGui.getInstance().getSpectrumLutId(), ClickGui.getInstance().getSpectrumLutHeight(), alpha);
                } else {
                    float segment = 2.0f;
                    for (float yy = yTop; yy < yBottom; yy += segment) {
                        float yy2 = Math.min(yy + segment, yBottom);
                        int c = ColorUtil.injectAlpha(ClickGui.getInstance().getColor((double)yy * delayPerPixel).getRGB(), alpha);
                        Render2DUtil.drawLine(context.method_51448(), leftX, yy2, leftX, yy, c);
                        Render2DUtil.drawLine(context.method_51448(), rightX, yy2, rightX, yy, c);
                    }
                }
                int bottomColor = ColorUtil.injectAlpha(ClickGui.getInstance().getColor((double)yBottom * delayPerPixel).getRGB(), alpha);
                Render2DUtil.drawLine(context.method_51448(), leftX, yBottom, rightX, yBottomLine, bottomColor);
            }
            float height = this.height + 2;
            List<Setting> settings = this.module.getSettings();
            int i = 0;
            while (i < this.items.size() && i < settings.size()) {
                Setting setting = settings.get(i);
                Item item = this.items.get(i);
                item.setHeight(this.height);
                double visibleH = item.getVisibleHeight();
                if (visibleH <= 0.01 && item.isHidden()) {
                    ++i;
                    continue;
                }
                item.setLocation(this.x + 1.0f, this.y + height + slide);
                item.setWidth(this.width - 9);
                if (setting instanceof BooleanSetting && ((BooleanSetting)setting).hasParent()) {
                    int j;
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                    height += (float)(visibleH + 2.0);
                    BooleanSetting parent = (BooleanSetting)setting;
                    double openProgress = this.getParentOpenProgress(parent);
                    for (j = i + 1; j < settings.size() && this.isChildOf(parent, settings.get(j)); ++j) {
                    }
                    double childrenFull = 0.0;
                    for (int k = i + 1; k < j && k < this.items.size(); ++k) {
                        if (!this.isVisibleWithParentOpen(parent, settings.get(k))) continue;
                        Item child = this.items.get(k);
                        child.setHeight(this.height);
                        double childVisibleH = child.getVisibleHeight();
                        if (childVisibleH <= 0.01 && child.isHidden()) continue;
                        childrenFull += childVisibleH + 2.0;
                    }
                    double childrenVisible = childrenFull * openProgress;
                    if (childrenVisible > 0.01) {
                        float childSlide = (1.0f - (float)openProgress) * 6.0f;
                        float childStartY = this.y + height + slide + childSlide;
                        float childX = this.x + 2.0f;
                        int childW = this.width - 11;
                        int scissorX1 = (int)childX - 1;
                        int scissorY1 = (int)childStartY - 1;
                        int scissorX2 = (int)(childX + (float)childW + 8.0f) + 1;
                        int scissorY2 = (int)Math.round((double)childStartY + childrenVisible) + 1;
                        context.method_44379(scissorX1, scissorY1, scissorX2, scissorY2);
                        float yOff = childStartY;
                        for (int k = i + 1; k < j && k < this.items.size(); ++k) {
                            if (!this.isVisibleWithParentOpen(parent, settings.get(k))) continue;
                            Item child = this.items.get(k);
                            child.setHeight(this.height);
                            double childVisibleH = child.getVisibleHeight();
                            if (childVisibleH <= 0.01 && child.isHidden()) continue;
                            child.setLocation(childX, yOff);
                            child.setWidth(childW);
                            if (childVisibleH < (double)child.getHeight() - 0.01) {
                                int cX1 = (int)child.getX() - 1;
                                int cY1 = (int)child.getY() - 1;
                                int cX2 = (int)(child.getX() + (float)child.getWidth() + 7.0f) + 1;
                                int cY2 = (int)((double)child.getY() + childVisibleH) + 1;
                                int iX1 = Math.max(scissorX1, cX1);
                                int iY1 = Math.max(scissorY1, cY1);
                                int iX2 = Math.min(scissorX2, cX2);
                                int iY2 = Math.min(scissorY2, cY2);
                                if (iX2 > iX1 && iY2 > iY1) {
                                    context.method_44379(iX1, iY1, iX2, iY2);
                                    child.drawScreen(context, mouseX, mouseY, partialTicks);
                                    context.disableScissor();
                                }
                            } else {
                                child.drawScreen(context, mouseX, mouseY, partialTicks);
                            }
                            yOff += (float)(childVisibleH + 2.0);
                        }
                        context.disableScissor();
                    }
                    height += (float)childrenVisible;
                    i = j;
                    continue;
                }
                if (visibleH < (double)item.getHeight() - 0.01) {
                    int scissorX1 = (int)item.getX() - 1;
                    int scissorY1 = (int)item.getY() - 1;
                    int scissorX2 = (int)(item.getX() + (float)item.getWidth() + 7.0f) + 1;
                    int scissorY2 = (int)((double)item.getY() + visibleH) + 1;
                    context.method_44379(scissorX1, scissorY1, scissorX2, scissorY2);
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                    context.disableScissor();
                } else {
                    item.drawScreen(context, mouseX, mouseY, partialTicks);
                }
                height += (float)(visibleH + 2.0);
                ++i;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY) && InputConstants.method_15987((long)mc.getWindow().getWindow(), (int)340)) {
            this.resetSettingsToDefault();
            ModuleButton.sound();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                boolean bl = this.subOpen = !this.subOpen;
                if (this.subOpen) {
                    for (Item item : this.items) {
                        if (!(item instanceof SliderButton)) continue;
                        ((SliderButton)item).resetFillAnimation();
                    }
                }
                ModuleButton.sound();
            }
            if (this.subOpen) {
                for (Item item : this.items) {
                    if (item.isHidden()) continue;
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onKeyPressed(int key) {
        super.onKeyPressed(key);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyPressed(key);
            }
        }
    }

    public int getButtonHeight() {
        return super.getHeight();
    }

    public double getVisibleItemHeight() {
        double height = 3.0;
        List<Setting> settings = this.module.getSettings();
        int i = 0;
        while (i < this.items.size() && i < settings.size()) {
            Setting setting = settings.get(i);
            Item item = this.items.get(i);
            item.setHeight(this.height);
            double visibleH = item.getVisibleHeight();
            if (visibleH <= 0.01 && item.isHidden()) {
                ++i;
                continue;
            }
            height += visibleH + 2.0;
            if (setting instanceof BooleanSetting && ((BooleanSetting)setting).hasParent()) {
                int j;
                BooleanSetting parent = (BooleanSetting)setting;
                double openProgress = this.getParentOpenProgress(parent);
                for (j = i + 1; j < settings.size() && this.isChildOf(parent, settings.get(j)); ++j) {
                }
                double childrenFull = 0.0;
                for (int k = i + 1; k < j && k < this.items.size(); ++k) {
                    if (!this.isVisibleWithParentOpen(parent, settings.get(k))) continue;
                    Item child = this.items.get(k);
                    child.setHeight(this.height);
                    double childVisibleH = child.getVisibleHeight();
                    if (childVisibleH <= 0.01 && child.isHidden()) continue;
                    childrenFull += childVisibleH + 2.0;
                }
                height += childrenFull * openProgress;
                i = j;
                continue;
            }
            ++i;
        }
        return height;
    }

    public int getItemHeight() {
        return (int)Math.round(this.getVisibleItemHeight());
    }

    private void resetSettingsToDefault() {
        for (Setting setting : this.module.getSettings()) {
            this.resetSettingToDefault(setting);
        }
    }

    public void resetChildSettingsToDefault(BooleanSetting parent) {
        boolean originalOpen = parent.isOpen();
        for (Setting setting : this.module.getSettings()) {
            BooleanSetting s;
            if (setting == parent) continue;
            parent.setOpen(true);
            boolean visibleWhenOpen = setting.isVisible();
            parent.setOpen(false);
            boolean visibleWhenClosed = setting.isVisible();
            parent.setOpen(originalOpen);
            if (!visibleWhenOpen || visibleWhenClosed) continue;
            if (setting instanceof BooleanSetting && (s = (BooleanSetting)setting).hasParent()) {
                this.resetChildSettingsToDefault(s);
            }
            this.resetSettingToDefault(setting);
        }
    }

    public void resetPageSettingsToDefault(EnumSetting<?> page) {
        Object current = page.getValue();
        Enum[] values = (Enum[])((Enum)current).getDeclaringClass().getEnumConstants();
        String originalName = ((Enum)current).name();
        page.setEnumValue(originalName);
        for (Setting setting : this.module.getSettings()) {
            BooleanSetting s;
            if (setting == page) continue;
            page.setEnumValue(originalName);
            boolean visibleInCurrent = setting.isVisible();
            if (!visibleInCurrent) continue;
            boolean visibleInAll = true;
            for (Enum e : values) {
                page.setEnumValue(e.name());
                if (setting.isVisible()) continue;
                visibleInAll = false;
                break;
            }
            page.setEnumValue(originalName);
            if (visibleInAll) continue;
            if (setting instanceof BooleanSetting && (s = (BooleanSetting)setting).hasParent()) {
                this.resetChildSettingsToDefault(s);
            }
            this.resetSettingToDefault(setting);
        }
        page.setEnumValue(originalName);
    }

    private double getParentOpenProgress(BooleanSetting parent) {
        Animation anim = this.parentOpenAnimations.get(parent);
        if (anim == null) {
            anim = new Animation();
            this.parentOpenAnimations.put(parent, anim);
        }
        return anim.get(parent.isOpen() ? 1.0 : 0.0, 200L, Easing.CubicInOut);
    }

    private boolean isChildOf(BooleanSetting parent, Setting setting) {
        boolean originalOpen = parent.isOpen();
        parent.setOpen(true);
        boolean visibleWhenOpen = setting.isVisible();
        parent.setOpen(false);
        boolean visibleWhenClosed = setting.isVisible();
        parent.setOpen(originalOpen);
        return visibleWhenOpen && !visibleWhenClosed;
    }

    private boolean isVisibleWithParentOpen(BooleanSetting parent, Setting setting) {
        boolean originalOpen = parent.isOpen();
        parent.setOpen(true);
        boolean visible = setting.isVisible();
        parent.setOpen(originalOpen);
        return visible;
    }

    private void resetSettingToDefault(Setting setting) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting s = (BooleanSetting)setting;
            s.setValue(s.getDefaultValue());
            if (s.hasParent()) {
                s.setOpen(false);
            }
            return;
        }
        if (setting instanceof SliderSetting) {
            SliderSetting s = (SliderSetting)setting;
            s.setValue(s.getDefaultValue());
            return;
        }
        if (setting instanceof StringSetting) {
            StringSetting s = (StringSetting)setting;
            s.setValue(s.getDefaultValue());
            return;
        }
        if (setting instanceof BindSetting) {
            BindSetting s = (BindSetting)setting;
            s.setValue(s.getDefaultValue());
            s.setHoldEnable(false);
            s.setPressed(false);
            s.holding = false;
            return;
        }
        if (setting instanceof EnumSetting) {
            EnumSetting s = (EnumSetting)setting;
            Object defaultValue = s.getDefaultValue();
            s.setEnumValue(((Enum)defaultValue).name());
            return;
        }
        if (setting instanceof ColorSetting) {
            ColorSetting s = (ColorSetting)setting;
            s.setValue(s.getDefaultValue());
            s.rainbow = s.getDefaultRainbow();
            if (s.injectBoolean) {
                s.booleanValue = s.getDefaultBooleanValue();
            }
        }
    }

    @Override
    public int getHeight() {
        if (this.subOpen) {
            int height = super.getHeight();
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                height += item.getHeight() + 1;
            }
            return height + 2;
        }
        return super.getHeight();
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }

    @Override
    public boolean getState() {
        return this.module.isOn();
    }
}

