/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.core.impl.BlurManager;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.ItemStack;
import net.minecraft.GuiGraphics;

public class BackpackContents
extends Module
implements HudElement {
    public static BackpackContents INSTANCE;
    private int hudX = 100;
    private int hudY = 100;
    private final SliderSetting slotSize = this.add(new SliderSetting("SlotSize", 16, 12, 24));
    private final SliderSetting spacing = this.add(new SliderSetting("Spacing", 2, 0, 6));
    private final BooleanSetting showBackground = this.add(new BooleanSetting("ShowBackground", true));
    private final SliderSetting blurStrength = this.add(new SliderSetting("BlurStrength", 50, 0, 100));
    private final BooleanSetting showBorder = this.add(new BooleanSetting("ShowBorder", true));
    private final ColorSetting borderColor = this.add(new ColorSetting("BorderColor", new Color(255, 255, 255)));
    private final BooleanSetting showCount = this.add(new BooleanSetting("ShowCount", true));
    private final BooleanSetting showDurability = this.add(new BooleanSetting("ShowDurability", true));

    public BackpackContents() {
        super("BackpackContents", Module.Category.HUD);
        this.setChinese("\u80cc\u5305\u7269\u8d44\u663e\u793a");
        INSTANCE = this;
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
        return 9 * this.slotSize.getValueInt() + 8 * this.spacing.getValueInt();
    }

    @Override
    public int getElementHeight() {
        return 3 * this.slotSize.getValueInt() + 2 * this.spacing.getValueInt();
    }

    @Override
    public boolean isElementVisible() {
        return this.isOn();
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        this.renderElement(drawContext, tickDelta);
    }

    @Override
    public void renderElement(GuiGraphics drawContext, float tickDelta) {
        if (BackpackContents.mc.player == null) {
            return;
        }
        int ss = this.slotSize.getValueInt();
        int sp = this.spacing.getValueInt();
        int tw = this.getElementWidth();
        int th = this.getElementHeight();
        int sx = this.hudX;
        int sy = this.hudY;
        if (this.showBackground.getValue()) {
            float percentage = (float)this.blurStrength.getValueInt() / 100.0f;
            float actualRadius = percentage * 20.0f;
            if (actualRadius > 0.0f) {
                drawContext.flush();
                new BlurManager().applyBlur(actualRadius, sx, sy, tw, th);
            }
            drawContext.method_25294(sx, sy, sx + tw, sy + th, 0x40000000);
        }
        if (this.showBorder.getValue()) {
            int c = this.borderColor.getValue().getRGB();
            drawContext.method_25294(sx - 1, sy - 1, sx + tw + 1, sy, c);
            drawContext.method_25294(sx - 1, sy + th, sx + tw + 1, sy + th + 1, c);
            drawContext.method_25294(sx - 1, sy, sx, sy + th, c);
            drawContext.method_25294(sx + tw, sy, sx + tw + 1, sy + th, c);
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                ItemStack stack = BackpackContents.mc.player.getInventory().method_5438(row * 9 + col + 9);
                if (stack.method_7960()) continue;
                int x = sx + col * (ss + sp);
                int y = sy + row * (ss + sp);
                drawContext.method_51427(stack, x + 1, y + 1);
                if (!this.showCount.getValue() && !this.showDurability.getValue()) continue;
                ItemStack rs = stack.copy();
                if (!this.showCount.getValue()) {
                    rs.method_7939(1);
                }
                drawContext.method_51431(BackpackContents.mc.font, rs, x + 1, y + 1);
            }
        }
    }
}

