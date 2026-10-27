/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.GuiGraphics
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import java.awt.Color;
import net.minecraft.ItemStack;
import net.minecraft.GuiGraphics;

public class ArmorHud
extends Module
implements HudElement {
    public static ArmorHud INSTANCE;
    private int hudX = -1;
    private int hudY = -1;
    private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
    private static final int ITEM_SIZE = 16;
    private static final int ITEM_SPACING = 20;
    private static final int ARMOR_SLOTS = 4;
    public final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    public final BooleanSetting durability = this.add(new BooleanSetting("Durability", true));
    private static final Color MIN_COLOR;
    private static final Color MAX_COLOR;

    public ArmorHud() {
        super("Armor", Module.Category.HUD);
        this.setChinese("\u88c5\u5907\u663e\u793a");
        INSTANCE = this;
    }

    private void initDefaultPosition() {
        if (mc != null && mc.getWindow() != null) {
            this.hudX = mc.getWindow().getGuiScaledWidth() / 2 - this.getElementWidth() / 2;
            this.hudY = mc.getWindow().getGuiScaledHeight() - 55;
            this.alignMode = HudElement.AlignMode.LEFT;
        }
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
            this.initDefaultPosition();
        }
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
        if (this.hudY < 0) {
            this.initDefaultPosition();
        }
        return this.hudY;
    }

    @Override
    public void setElementY(int y) {
        this.hudY = y;
    }

    @Override
    public int getElementWidth() {
        return 80;
    }

    @Override
    public int getElementHeight() {
        return 16 + (this.durability.getValue() ? 6 : 0);
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
        if (ArmorHud.mc.player == null) {
            return;
        }
        int baseX = this.getElementX();
        int baseY = this.getElementY();
        int drawX = baseX;
        for (int i = 3; i >= 0; --i) {
            ItemStack stack = ArmorHud.mc.player.getInventory().method_7372(i);
            if (!stack.method_7960()) {
                context.method_51427(stack, drawX, baseY);
                context.method_51431(ArmorHud.mc.font, stack, drawX, baseY);
                if (this.durability.getValue() && stack.isDamageableItem()) {
                    int damage = EntityUtil.getDamagePercent(stack);
                    Color color = ColorUtil.fadeColor(MIN_COLOR, MAX_COLOR, (float)damage / 100.0f);
                    if (FontManager.isCustom()) {
                        FontManager.getSmall().drawString(context.method_51448(), damage + "%", (double)(drawX + 1), (double)((float)baseY - FontManager.getSmall().getFontHeight() / 2.0f), color.getRGB(), this.shadow.getValue());
                    } else {
                        TextUtil.drawStringScale(context, damage + "%", drawX + 2, (float)baseY - 2.25f, color.getRGB(), 0.5f, this.shadow.getValue());
                    }
                }
            }
            drawX += 20;
        }
    }

    static {
        MIN_COLOR = new Color(196, 0, 0);
        MAX_COLOR = new Color(0, 227, 0);
    }
}

