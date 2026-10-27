/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemLike
 *  net.minecraft.ResourceLocation
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 *  net.minecraft.AbstractClientPlayer
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.Aevum;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.ItemLike;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;
import net.minecraft.AbstractClientPlayer;

public class DFHealth
extends Module {
    public static DFHealth INSTANCE;
    public final SliderSetting yOffset = this.add(new SliderSetting("YOffset", -67.0, -300.0, 300.0, 1.0));
    public final SliderSetting maxDisplay = this.add(new SliderSetting("MaxDisplay", 3.0, 1.0, 10.0, 1.0));
    public final BooleanSetting hideVanilla = this.add(new BooleanSetting("HideVanilla", true));
    public final BooleanSetting showArmor = this.add(new BooleanSetting("Armor", true).setParent());
    public final BooleanSetting showDurability = this.add(new BooleanSetting("Durability", false, () -> this.showArmor.isOpen()));
    public final SliderSetting armorScale = this.add(new SliderSetting("ArmorScale", 0.6, 0.1, 1.0, 0.1, () -> this.showArmor.isOpen()));
    public final SliderSetting armorXOffset = this.add(new SliderSetting("ArmorX", 68.0, 0.0, 110.0, 1.0, () -> this.showArmor.isOpen()));
    public final SliderSetting armorYOffset = this.add(new SliderSetting("ArmorY", -12.0, -50.0, 50.0, 1.0, () -> this.showArmor.isOpen()));
    public final BooleanSetting showGapple = this.add(new BooleanSetting("Gapple", true));
    private static final int BASE_X = 6;
    private static final int BASE_BOTTOM_OFFSET = 42;
    private static final float AVATAR_SIZE = 25.0f;
    private static final float AVATAR_OFFSET_X = 1.0f;
    private static final float SPACING = 8.0f;
    private static final float INDEX_OFFSET_X = 1.0f;
    private static final float INDEX_OFFSET_Y = 2.0f;
    private static final float BAR_OFFSET_Y = 10.0f;
    private static final float BAR_WIDTH = 110.0f;
    private static final float BAR_HEIGHT = 3.0f;
    private static final int BAR_OUTLINE = 1;
    private static final float HP_TEXT_OFFSET_Y = -9.0f;
    private static final float NAME_TEXT_OFFSET_Y = 4.0f;
    private static final int ENTRY_HEIGHT = 31;
    private static final int ENTRY_GAP = 3;
    private static final Color HP_TEXT_COLOR;
    private static final Color NAME_TEXT_COLOR;
    private static final Color BAR_COLOR;
    private static final Color BAR_BACKGROUND_COLOR;
    private static final Color BAR_OUTLINE_COLOR;
    private static final int GAPPLE_X_OFFSET = 126;
    private static final int GAPPLE_Y_OFFSET = -13;
    private static final int GAPPLE_BOX_SIZE = 30;
    private static final float GAPPLE_RADIUS = 4.0f;
    private static final float GAPPLE_ICON_SCALE = 1.7f;
    private static final Color GAPPLE_BG_COLOR;
    private static final Color GAPPLE_TEXT_COLOR;
    private static final ItemStack ENCHANTED_GOLDEN_APPLE;

    public DFHealth() {
        super("DFHealth", Module.Category.HUD);
        this.setChinese("\u4e09\u89d2\u6d32\u8840\u91cf");
        INSTANCE = this;
    }

    public static boolean shouldHideVanilla() {
        return INSTANCE != null && INSTANCE.isOn() && DFHealth.INSTANCE.hideVanilla.getValue();
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        if (DFHealth.mc.player == null || DFHealth.mc.level == null) {
            return;
        }
        int selfBaseY = mc.getWindow().getGuiScaledHeight() - 42 + this.yOffset.getValueInt();
        ArrayList<AbstractClientPlayer> friends = new ArrayList<AbstractClientPlayer>();
        for (AbstractClientPlayer player : DFHealth.mc.level.method_18456()) {
            if (player == null || player == DFHealth.mc.player || !player.isAlive() || Aevum.FRIEND == null || !Aevum.FRIEND.isFriend((Player)player)) continue;
            friends.add(player);
        }
        friends.sort(Comparator.comparingDouble(p -> DFHealth.mc.player.method_5739((Entity)p)));
        int displayCount = Math.min(this.maxDisplay.getValueInt(), friends.size());
        for (int i = 0; i < displayCount; ++i) {
            AbstractClientPlayer friend = (AbstractClientPlayer)friends.get(i);
            int entryY = selfBaseY - (displayCount - i) * 34;
            this.renderEntry(drawContext, friend, entryY, i + 2, false);
        }
        this.renderEntry(drawContext, (AbstractClientPlayer)DFHealth.mc.player, selfBaseY, 1, true);
    }

    private void renderEntry(GuiGraphics drawContext, AbstractClientPlayer player, int baseY, int indexNumber, boolean isSelf) {
        PoseStack matrices = drawContext.method_51448();
        int avatarX = 7;
        int avatarY = baseY;
        int rectX = (int)((float)avatarX + 25.0f + 8.0f);
        int rectY = (int)((float)avatarY + 10.0f);
        this.drawPlayerHead(drawContext, player, avatarX, avatarY, 25.0f);
        this.drawScaledText(drawContext, String.valueOf(indexNumber), (float)avatarX + 1.0f, (float)avatarY + 2.0f, Color.WHITE, 1.0f, true);
        this.drawOutline(matrices, rectX - 1, rectY - 1, 112.0f, 5.0f, 1, BAR_OUTLINE_COLOR);
        Render2DUtil.drawRect(matrices, (float)rectX, (float)rectY, 110.0f, 3.0f, BAR_BACKGROUND_COLOR);
        float realHealth = player.getHealth() + player.getAbsorptionAmount();
        float ratio = Math.max(0.0f, Math.min(realHealth / 20.0f, 1.0f));
        if (ratio > 0.0f) {
            Render2DUtil.drawRect(matrices, (float)rectX, (float)rectY, (float)((int)(110.0f * ratio)), 3.0f, BAR_COLOR);
        }
        this.drawScaledText(drawContext, this.format(realHealth) + "/20", (float)rectX + 1.0f, (float)rectY + -9.0f, HP_TEXT_COLOR, 1.0f, true);
        this.drawScaledText(drawContext, player.method_5477().getString(), rectX, (float)rectY + 3.0f + 4.0f, NAME_TEXT_COLOR, 1.0f, true);
        if (this.showArmor.getValue()) {
            this.renderArmorAndDurability(drawContext, player, rectX + this.armorXOffset.getValueInt(), rectY + this.armorYOffset.getValueInt());
        }
        if (isSelf && this.showGapple.getValue()) {
            this.renderGapple(drawContext, rectX + 126, rectY + -13);
        }
    }

    private void renderGapple(GuiGraphics drawContext, int x, int y) {
        PoseStack matrices = drawContext.method_51448();
        Render2DUtil.drawRoundedRect(matrices, x, y, 30.0f, 30.0f, 4.0f, GAPPLE_BG_COLOR);
        int gappleCount = this.countEnchantedGoldenApples();
        float scaledIconSize = 27.2f;
        float iconX = (float)x + (30.0f - scaledIconSize) / 2.0f;
        float iconY = (float)y + (30.0f - scaledIconSize) / 2.0f;
        matrices.method_22903();
        matrices.method_46416(iconX, iconY, 0.0f);
        matrices.method_22905(1.7f, 1.7f, 1.0f);
        drawContext.method_51427(ENCHANTED_GOLDEN_APPLE, 0, 0);
        matrices.method_22909();
        if (gappleCount > 0) {
            String countText = String.valueOf(gappleCount);
            int textWidth = DFHealth.mc.font.method_1727(countText);
            float textX = (float)x + (float)(30 - textWidth) / 2.0f;
            float textY = y - 10;
            this.drawScaledText(drawContext, countText, textX, textY, GAPPLE_TEXT_COLOR, 1.0f, true);
        }
    }

    private int countEnchantedGoldenApples() {
        if (DFHealth.mc.player == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < DFHealth.mc.player.getInventory().items.size(); ++i) {
            ItemStack stack = (ItemStack)DFHealth.mc.player.getInventory().items.get(i);
            if (stack.method_7909() != Items.ENCHANTED_GOLDEN_APPLE) continue;
            count += stack.getCount();
        }
        ItemStack offhand = DFHealth.mc.player.getOffhandItem();
        if (offhand.method_7909() == Items.ENCHANTED_GOLDEN_APPLE) {
            count += offhand.getCount();
        }
        return count;
    }

    private void renderArmorAndDurability(GuiGraphics drawContext, AbstractClientPlayer player, int x, int y) {
        PoseStack matrices = drawContext.method_51448();
        float scale = this.armorScale.getValueFloat();
        matrices.method_22903();
        matrices.method_46416((float)x, (float)y, 0.0f);
        matrices.method_22905(scale, scale, 1.0f);
        int xOffset = 0;
        for (int i = 3; i >= 0; --i) {
            ItemStack armorStack = player.getInventory().method_7372(i);
            if (armorStack.method_7960()) continue;
            if (this.showDurability.getValue() && armorStack.isDamageableItem()) {
                int maxDamage = armorStack.getMaxDamage();
                int currentDamage = armorStack.getDamageValue();
                int durabilityPercent = (int)((float)(maxDamage - currentDamage) / (float)maxDamage * 100.0f);
                Color durColor = this.getDurabilityColor(durabilityPercent);
                String durText = durabilityPercent + "%";
                matrices.method_22903();
                float textPaddingX = 8.0f - (float)DFHealth.mc.font.method_1727(durText) / 2.0f;
                matrices.method_46416((float)xOffset + textPaddingX, -9.5f, 0.0f);
                drawContext.method_51433(DFHealth.mc.font, durText, 0, 0, durColor.getRGB(), true);
                matrices.method_22909();
            }
            drawContext.method_51427(armorStack, xOffset, 0);
            xOffset += 18;
        }
        matrices.method_22909();
    }

    private Color getDurabilityColor(int percent) {
        if (percent > 75) {
            return Color.GREEN;
        }
        if (percent > 45) {
            return Color.YELLOW;
        }
        if (percent > 20) {
            return Color.ORANGE;
        }
        return Color.RED;
    }

    private String format(float value) {
        return value == (float)((int)value) ? String.valueOf((int)value) : String.format("%.1f", Float.valueOf(value));
    }

    private void drawOutline(PoseStack matrices, float x, float y, float width, float height, int thickness, Color color) {
        Render2DUtil.drawRect(matrices, x, y, width, (float)thickness, color);
        Render2DUtil.drawRect(matrices, x, y + height - (float)thickness, width, (float)thickness, color);
        Render2DUtil.drawRect(matrices, x, y, (float)thickness, height, color);
        Render2DUtil.drawRect(matrices, x + width - (float)thickness, y, (float)thickness, height, color);
    }

    private void drawScaledText(GuiGraphics drawContext, String text, float x, float y, Color color, float scale, boolean shadow) {
        PoseStack matrices = drawContext.method_51448();
        matrices.method_22903();
        matrices.method_46416(x, y, 0.0f);
        matrices.method_22905(scale, scale, 1.0f);
        if (FontManager.isCustom()) {
            FontManager.get().drawString(matrices, text, 0.0f, 0.0f, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f, shadow);
        } else {
            drawContext.method_51433(DFHealth.mc.font, text, 0, 0, color.getRGB(), shadow);
        }
        matrices.method_22909();
    }

    private void drawPlayerHead(GuiGraphics drawContext, AbstractClientPlayer player, int x, int y, float size) {
        ResourceLocation skin = player.getSkin().comp_1626();
        PoseStack matrices = drawContext.method_51448();
        float scale = size / 8.0f;
        matrices.method_22903();
        matrices.method_46416((float)x, (float)y, 0.0f);
        matrices.method_22905(scale, scale, 1.0f);
        drawContext.method_25290(skin, 0, 0, 8.0f, 8.0f, 8, 8, 64, 64);
        drawContext.method_25290(skin, 0, 0, 40.0f, 8.0f, 8, 8, 64, 64);
        matrices.method_22909();
    }

    static {
        HP_TEXT_COLOR = new Color(255, 255, 255, 245);
        NAME_TEXT_COLOR = new Color(255, 255, 255, 190);
        BAR_COLOR = new Color(255, 255, 255, 255);
        BAR_BACKGROUND_COLOR = new Color(40, 40, 40, 136);
        BAR_OUTLINE_COLOR = new Color(0, 0, 0, 140);
        GAPPLE_BG_COLOR = new Color(0, 0, 0, 100);
        GAPPLE_TEXT_COLOR = new Color(255, 255, 255, 255);
        ENCHANTED_GOLDEN_APPLE = new ItemStack((ItemLike)Items.ENCHANTED_GOLDEN_APPLE);
    }
}

