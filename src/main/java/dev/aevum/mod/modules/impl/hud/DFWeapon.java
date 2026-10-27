/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemLike
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.gui.fonts.FontRenderer;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.ItemLike;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class DFWeapon
extends Module {
    public static DFWeapon INSTANCE;
    public final SliderSetting xOffset = this.add(new SliderSetting("XOffset", -16.0, -400.0, 400.0, 1.0));
    public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", -52.0, -400.0, 400.0, 1.0));
    public final SliderSetting iconX = this.add(new SliderSetting("IconX", 46.0, 0.0, 120.0, 1.0));
    public final SliderSetting iconY = this.add(new SliderSetting("IconY", 17.0, 0.0, 80.0, 1.0));
    public final SliderSetting leftCountY = this.add(new SliderSetting("LeftCountY", 0.0, -20.0, 20.0, 1.0));
    public final SliderSetting rightCountY = this.add(new SliderSetting("RightCountY", 0.0, -20.0, 20.0, 1.0));
    private static final float WIDTH = 118.0f;
    private static final float HEIGHT = 34.0f;
    private static final float RADIUS = 2.5f;
    private static final Color BG_COLOR;
    private static final String DISPLAY_NAME = "EndCrystal";
    private static final Color NAME_COLOR;
    private static final Color HOTBAR_COLOR;
    private static final Color OTHER_COLOR;
    private static final boolean TEXT_SHADOW = true;
    private static final float NAME_X = 3.0f;
    private static final float NAME_BOTTOM_MARGIN = 3.0f;
    private static final float ICON_SCALE = 1.75f;
    private static final float COUNT_RIGHT_MARGIN = 6.0f;
    private static final float COUNT_BOTTOM_TO_BOX_TOP = 2.0f;
    private static final float NAME_FONT_SIZE = 8.0f;
    private static final float BIG_COUNT_FONT_SIZE = 18.0f;
    private static final float SMALL_COUNT_FONT_SIZE = 9.0f;
    private static final float COUNT_GAP = 1.0f;
    private static final ItemStack END_CRYSTAL_STACK;

    public DFWeapon() {
        super("DFWeapon", Module.Category.HUD);
        this.setChinese("\u4e09\u89d2\u6d32\u6b66\u5668");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        if (DFWeapon.mc.player == null || DFWeapon.mc.level == null) {
            return;
        }
        PoseStack matrices = drawContext.method_51448();
        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();
        float x = screenWidth - 118.0f + this.xOffset.getValueFloat();
        float y = screenHeight - 34.0f + this.yOffset.getValueFloat();
        Render2DUtil.drawRoundedRect(matrices, x, y, 118.0f, 34.0f, 2.5f, BG_COLOR);
        int hotbarCrystalCount = this.countHotbarCrystals();
        int totalCrystalCount = this.countTotalCrystals();
        this.renderCrystalIcon(drawContext, x, y);
        this.renderName(drawContext, x, y);
        this.renderCount(drawContext, x, y, hotbarCrystalCount, totalCrystalCount);
    }

    private void renderCrystalIcon(GuiGraphics drawContext, float x, float y) {
        PoseStack matrices = drawContext.method_51448();
        float scaledSize = 28.0f;
        float drawX = x + this.iconX.getValueFloat() - scaledSize / 2.0f;
        float drawY = y + this.iconY.getValueFloat() - scaledSize / 2.0f;
        matrices.method_22903();
        matrices.method_46416(drawX, drawY, 0.0f);
        matrices.method_22905(1.75f, 1.75f, 1.0f);
        drawContext.method_51427(END_CRYSTAL_STACK, 0, 0);
        matrices.method_22909();
    }

    private void renderName(GuiGraphics drawContext, float x, float y) {
        FontRenderer font = FontManager.get(8.0f);
        float textX = x + 3.0f;
        float textY = y + 34.0f - font.getFontHeight(DISPLAY_NAME) - 3.0f;
        this.drawFontText(drawContext, font, DISPLAY_NAME, textX, textY, NAME_COLOR, true);
    }

    private void renderCount(GuiGraphics drawContext, float x, float y, int hotbarCount, int totalCount) {
        FontRenderer bigFont = FontManager.get(18.0f);
        FontRenderer smallFont = FontManager.get(9.0f);
        String bigText = String.valueOf(hotbarCount);
        String smallText = "/" + totalCount;
        float bigWidth = bigFont.getWidth(bigText);
        float smallWidth = smallFont.getWidth(smallText);
        float bigHeight = bigFont.getFontHeight(bigText);
        float smallHeight = smallFont.getFontHeight(smallText);
        float totalWidth = bigWidth + 1.0f + smallWidth;
        float baselineBottom = y - 2.0f;
        float startX = x + 118.0f - 6.0f - totalWidth;
        float bigY = baselineBottom - bigHeight + this.leftCountY.getValueFloat();
        float smallY = baselineBottom - smallHeight + this.rightCountY.getValueFloat();
        this.drawFontText(drawContext, bigFont, bigText, startX, bigY, HOTBAR_COLOR, true);
        this.drawFontText(drawContext, smallFont, smallText, startX + bigWidth + 1.0f, smallY, OTHER_COLOR, true);
    }

    private void drawFontText(GuiGraphics drawContext, FontRenderer font, String text, float x, float y, Color color, boolean shadow) {
        PoseStack matrices = drawContext.method_51448();
        font.drawString(matrices, text, x, y, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f, shadow);
    }

    private int countHotbarCrystals() {
        if (DFWeapon.mc.player == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = DFWeapon.mc.player.getInventory().method_5438(i);
            if (stack.method_7909() != Items.END_CRYSTAL) continue;
            count += stack.getCount();
        }
        return count;
    }

    private int countTotalCrystals() {
        if (DFWeapon.mc.player == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < DFWeapon.mc.player.getInventory().items.size(); ++i) {
            ItemStack stack = (ItemStack)DFWeapon.mc.player.getInventory().items.get(i);
            if (stack.method_7909() != Items.END_CRYSTAL) continue;
            count += stack.getCount();
        }
        ItemStack offhand = DFWeapon.mc.player.getOffhandItem();
        if (offhand.method_7909() == Items.END_CRYSTAL) {
            count += offhand.getCount();
        }
        return count;
    }

    static {
        BG_COLOR = new Color(0x5F000000, true);
        NAME_COLOR = new Color(-1426063361, true);
        HOTBAR_COLOR = new Color(-1, true);
        OTHER_COLOR = new Color(-1090519041, true);
        END_CRYSTAL_STACK = new ItemStack((ItemLike)Items.END_CRYSTAL);
    }
}

