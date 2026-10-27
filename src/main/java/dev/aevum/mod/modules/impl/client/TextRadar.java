/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.GuiGraphics
 *  net.minecraft.AbstractClientPlayer
 */
package dev.aevum.mod.modules.impl.client;

import dev.aevum.Aevum;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.GuiGraphics;
import net.minecraft.AbstractClientPlayer;

public class TextRadar
extends Module {
    public static TextRadar INSTANCE;
    private final DecimalFormat df = new DecimalFormat("0.0");
    private final BooleanSetting font = this.add(new BooleanSetting("Font", true));
    private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
    private final SliderSetting x = this.add(new SliderSetting("X", 0, 0, 1500));
    private final SliderSetting y = this.add(new SliderSetting("Y", 100, 0, 1000));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255)));
    private final ColorSetting friend = this.add(new ColorSetting("Friend").injectBoolean(true));
    private final BooleanSetting doubleBlank = this.add(new BooleanSetting("Double", false));
    private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
    private final BooleanSetting pops = this.add(new BooleanSetting("Pops", true));
    public final BooleanSetting red = this.add(new BooleanSetting("Red", false));
    private final BooleanSetting getDistance = this.add(new BooleanSetting("Distance", true));
    private final BooleanSetting effects = this.add(new BooleanSetting("Effects", true));

    public TextRadar() {
        super("TextRadar", Module.Category.Client);
        this.setChinese("\u6587\u5b57\u96f7\u8fbe");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        int currentY = this.y.getValueInt();
        ArrayList<AbstractClientPlayer> players = new ArrayList<AbstractClientPlayer>(TextRadar.mc.level.method_18456());
        players.sort(Comparator.comparingDouble(player -> TextRadar.mc.player.method_5739((Entity)player)));
        for (Player class_16572 : players) {
            int n;
            boolean isFriend;
            int totemPopped;
            if (class_16572 == TextRadar.mc.player) continue;
            StringBuilder stringBuilder = new StringBuilder();
            String blank = this.doubleBlank.getValue() ? "  " : " ";
            String string = blank;
            if (this.health.getValue()) {
                stringBuilder.append(TextRadar.getHealthColor(class_16572));
                stringBuilder.append(this.df.format(class_16572.getHealth() + class_16572.getAbsorptionAmount()));
                stringBuilder.append(blank);
            }
            stringBuilder.append(ChatFormatting.RESET);
            stringBuilder.append(class_16572.method_5477().getString());
            if (this.getDistance.getValue()) {
                stringBuilder.append(blank);
                stringBuilder.append(ChatFormatting.WHITE);
                stringBuilder.append(this.df.format(TextRadar.mc.player.method_5739((Entity)class_16572)));
                stringBuilder.append("m");
            }
            if (this.effects.getValue()) {
                if (class_16572.method_6059(MobEffects.MOVEMENT_SLOWDOWN)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(ChatFormatting.GRAY);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(class_16572.method_6112(MobEffects.MOVEMENT_SLOWDOWN).method_5578() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(class_16572.method_6112(MobEffects.MOVEMENT_SLOWDOWN).method_5584() / 20 + 1);
                    stringBuilder.append("s");
                }
                if (class_16572.method_6059(MobEffects.MOVEMENT_SPEED)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(ChatFormatting.AQUA);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(class_16572.method_6112(MobEffects.MOVEMENT_SPEED).method_5578() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(class_16572.method_6112(MobEffects.MOVEMENT_SPEED).method_5584() / 20 + 1);
                    stringBuilder.append("s");
                }
                if (class_16572.method_6059(MobEffects.DAMAGE_BOOST)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(ChatFormatting.DARK_RED);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(class_16572.method_6112(MobEffects.DAMAGE_BOOST).method_5578() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(class_16572.method_6112(MobEffects.DAMAGE_BOOST).method_5584() / 20 + 1);
                    stringBuilder.append("s");
                }
                if (class_16572.method_6059(MobEffects.DAMAGE_RESISTANCE)) {
                    stringBuilder.append(blank);
                    stringBuilder.append(ChatFormatting.BLUE);
                    stringBuilder.append("Lv.");
                    stringBuilder.append(class_16572.method_6112(MobEffects.DAMAGE_RESISTANCE).method_5578() + 1);
                    stringBuilder.append(blank);
                    stringBuilder.append(class_16572.method_6112(MobEffects.DAMAGE_RESISTANCE).method_5584() / 20 + 1);
                    stringBuilder.append("s");
                }
            }
            if (this.pops.getValue() && (totemPopped = Aevum.POP.getPop(class_16572)) > 0) {
                stringBuilder.append(blank);
                stringBuilder.append(TextRadar.getPopColor(totemPopped));
                stringBuilder.append("-");
                stringBuilder.append(totemPopped);
            }
            if ((isFriend = Aevum.FRIEND.isFriend(class_16572)) && !this.friend.booleanValue) continue;
            int color = isFriend ? this.friend.getValue().getRGB() : this.color.getValue().getRGB();
            int n2 = color;
            if (this.font.getValue()) {
                FontManager.draw(drawContext.method_51448(), stringBuilder.toString(), this.x.getValueInt(), currentY, color, this.shadow.getValue());
            } else {
                drawContext.method_51433(TextRadar.mc.font, stringBuilder.toString(), this.x.getValueInt(), currentY, color, this.shadow.getValue());
            }
            if (this.font.getValue()) {
                n = (int)FontManager.ui.getFontHeight();
            } else {
                Objects.requireNonNull(TextRadar.mc.font);
                n = 9;
            }
            currentY += n;
        }
    }

    public static ChatFormatting getHealthColor(Player player) {
        double health = player.getHealth() + player.getAbsorptionAmount();
        if (health > 18.0) {
            return ChatFormatting.GREEN;
        }
        if (health > 16.0) {
            return ChatFormatting.DARK_GREEN;
        }
        if (health > 12.0) {
            return ChatFormatting.YELLOW;
        }
        if (health > 8.0) {
            return ChatFormatting.GOLD;
        }
        if (health > 4.0) {
            return ChatFormatting.RED;
        }
        return ChatFormatting.DARK_RED;
    }

    public static ChatFormatting getPopColor(int totems) {
        if (TextRadar.INSTANCE.red.getValue()) {
            return ChatFormatting.RED;
        }
        if (totems > 10) {
            return ChatFormatting.DARK_RED;
        }
        if (totems > 8) {
            return ChatFormatting.RED;
        }
        if (totems > 6) {
            return ChatFormatting.GOLD;
        }
        if (totems > 4) {
            return ChatFormatting.YELLOW;
        }
        if (totems > 2) {
            return ChatFormatting.DARK_GREEN;
        }
        return ChatFormatting.GREEN;
    }
}

