/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.MobCategory
 *  net.minecraft.EndCrystal
 *  net.minecraft.ItemEntity
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.GuiGraphics
 *  net.minecraft.GameRenderer
 *  net.minecraft.MeshData
 *  org.joml.Matrix4f
 *  org.joml.Vector4d
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectInstance;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.MobCategory;
import net.minecraft.EndCrystal;
import net.minecraft.ItemEntity;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.GuiGraphics;
import net.minecraft.GameRenderer;
import net.minecraft.MeshData;
import org.joml.Matrix4f;
import org.joml.Vector4d;

public class TwoDESP
extends Module {
    public static TwoDESP INSTANCE;
    private final EnumSetting<TwoDESPMode> page = this.add(new EnumSetting<TwoDESPMode>("Settings", TwoDESPMode.Target));
    public final ColorSetting armorDuraColor = this.add(new ColorSetting("Armor Dura Color", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    public final ColorSetting hHealth = this.add(new ColorSetting("High Health Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESPMode.Color));
    public final ColorSetting mHealth = this.add(new ColorSetting("Mid Health Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESPMode.Color));
    public final ColorSetting lHealth = this.add(new ColorSetting("Low Health Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESPMode.Color));
    private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true, () -> this.page.getValue() == TwoDESPMode.Setting));
    private final BooleanSetting renderHealth = this.add(new BooleanSetting("renderHealth", true, () -> this.page.getValue() == TwoDESPMode.Setting));
    private final BooleanSetting renderArmor = this.add(new BooleanSetting("Armor Dura", true, () -> this.page.getValue() == TwoDESPMode.Setting));
    private final BooleanSetting renderEffects = this.add(new BooleanSetting("Effects", true, () -> this.page.getValue() == TwoDESPMode.Setting));
    private final SliderSetting durascale = this.add(new SliderSetting("DuraScale", 1.0, 0.0, 2.0, 0.1, () -> this.renderArmor.getValue()));
    private final BooleanSetting drawItem = this.add(new BooleanSetting("draw Item Name", true, () -> this.page.getValue() == TwoDESPMode.Setting));
    private final BooleanSetting drawItemC = this.add(new BooleanSetting("draw Item Count", true, () -> this.page.getValue() == TwoDESPMode.Setting && this.drawItem.getValue()));
    public final ColorSetting countColor = this.add(new ColorSetting("Item Count Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESPMode.Color && this.drawItemC.getValue()));
    public final ColorSetting textcolor = this.add(new ColorSetting("Item Name Color", new Color(189, 153, 255, 255), () -> this.page.getValue() == TwoDESPMode.Color && this.drawItem.getValue()));
    private final BooleanSetting font = this.add(new BooleanSetting("CustomFont", true, () -> this.page.getValue() == TwoDESPMode.Setting));
    private final BooleanSetting players = this.add(new BooleanSetting("Players", true, () -> this.page.getValue() == TwoDESPMode.Target));
    private final BooleanSetting friends = this.add(new BooleanSetting("Friends", true, () -> this.page.getValue() == TwoDESPMode.Target));
    private final BooleanSetting crystals = this.add(new BooleanSetting("Crystals", true, () -> this.page.getValue() == TwoDESPMode.Target));
    private final BooleanSetting creatures = this.add(new BooleanSetting("Creatures", false, () -> this.page.getValue() == TwoDESPMode.Target));
    private final BooleanSetting monsters = this.add(new BooleanSetting("Monsters", false, () -> this.page.getValue() == TwoDESPMode.Target));
    private final BooleanSetting ambients = this.add(new BooleanSetting("Ambients", false, () -> this.page.getValue() == TwoDESPMode.Target));
    private final BooleanSetting others = this.add(new BooleanSetting("Others", false, () -> this.page.getValue() == TwoDESPMode.Target));
    private final ColorSetting playersC = this.add(new ColorSetting("PlayersBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    private final ColorSetting friendsC = this.add(new ColorSetting("FriendsBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    private final ColorSetting crystalsC = this.add(new ColorSetting("CrystalsBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    private final ColorSetting creaturesC = this.add(new ColorSetting("CreaturesBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    private final ColorSetting monstersC = this.add(new ColorSetting("MonstersBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    private final ColorSetting ambientsC = this.add(new ColorSetting("AmbientsBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));
    private final ColorSetting othersC = this.add(new ColorSetting("OthersBox", new Color(12425727), () -> this.page.getValue() == TwoDESPMode.Color));

    public TwoDESP() {
        super("TwoDESP", Module.Category.Render);
        this.setChinese("2D\u900f\u89c6");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics context, float tickDelta) {
        if (TwoDESP.nullCheck()) {
            return;
        }
        Matrix4f matrix = context.method_51448().last().method_23761();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::method_34540);
        BufferBuilder bufferBuilder = null;
        boolean hasVertices = false;
        for (Entity ent : Aevum.THREAD.getEntities()) {
            if (!this.shouldRender(ent)) continue;
            if (bufferBuilder == null) {
                bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            }
            if (!this.drawBox(bufferBuilder, ent, matrix, context)) continue;
            hasVertices = true;
        }
        if (hasVertices && bufferBuilder != null) {
            BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        }
        RenderSystem.disableBlend();
        for (Entity ent : Aevum.THREAD.getEntities()) {
            if (!this.shouldRender(ent)) continue;
            this.drawText(ent, context);
        }
    }

    public boolean shouldRender(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (TwoDESP.mc.player == null) {
            return false;
        }
        if (entity instanceof Player) {
            if (entity == TwoDESP.mc.player && TwoDESP.mc.options.getCameraType().isFirstPerson()) {
                return false;
            }
            if (Aevum.FRIEND.isFriend((Player)entity)) {
                return this.friends.getValue();
            }
            return this.players.getValue();
        }
        if (entity instanceof EndCrystal) {
            return this.crystals.getValue();
        }
        return switch (entity.getType().getCategory()) {
            case MobCategory.CREATURE, MobCategory.WATER_CREATURE -> this.creatures.getValue();
            case MobCategory.MONSTER -> this.monsters.getValue();
            case MobCategory.AMBIENT, MobCategory.WATER_AMBIENT -> this.ambients.getValue();
            default -> this.others.getValue();
        };
    }

    public Color getEntityColor(Entity entity) {
        if (entity == null) {
            return new Color(31743);
        }
        if (entity instanceof Player) {
            Player PlayerEntity = (Player)entity;
            if (Aevum.FRIEND.isFriend(PlayerEntity)) {
                return this.friendsC.getValue();
            }
            return this.playersC.getValue();
        }
        if (entity instanceof EndCrystal) {
            return this.crystalsC.getValue();
        }
        return switch (entity.getType().getCategory()) {
            case MobCategory.CREATURE, MobCategory.WATER_CREATURE -> this.creaturesC.getValue();
            case MobCategory.MONSTER -> this.monstersC.getValue();
            case MobCategory.AMBIENT, MobCategory.WATER_AMBIENT -> this.ambientsC.getValue();
            default -> this.othersC.getValue();
        };
    }

    public boolean drawBox(BufferBuilder bufferBuilder, Entity ent, Matrix4f matrix, GuiGraphics context) {
        double x = MathUtil.interpolate(ent.xOld, ent.getX(), (double)mc.getTimer().method_60637(true));
        double y = MathUtil.interpolate(ent.yOld, ent.getY(), (double)mc.getTimer().method_60637(true));
        double z = MathUtil.interpolate(ent.zOld, ent.getZ(), (double)mc.getTimer().method_60637(true));
        AABB axisAlignedBB2 = ent.method_5829();
        AABB axisAlignedBB3 = new AABB(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        Vec3[] vectors = new Vec3[]{new Vec3(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.maxZ), new Vec3(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.maxZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ)};
        Color col = this.getEntityColor(ent);
        Vector4d position = null;
        for (Vec3 vector : vectors) {
            vector = TextUtil.worldSpaceToScreenSpace(new Vec3(vector.x, vector.y, vector.z));
            if (!(vector.z > 0.0) || !(vector.z < 1.0)) continue;
            if (position == null) {
                position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }
            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
        }
        if (position != null) {
            LivingEntity lent;
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;
            if (this.outline.getValue()) {
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 1.0), (float)posY, 1.5f, (float)(endPosY - posY + 0.5), Color.BLACK);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 1.0), (float)(posY - 0.5), (float)(endPosX - posX + 1.5), 1.5f, Color.BLACK);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(endPosX - 1.0), (float)posY, 1.5f, (float)(endPosY - posY + 0.5), Color.BLACK);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 1.0), (float)(endPosY - 1.0), (float)(endPosX - posX + 1.5), 1.5f, Color.BLACK);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, 0.5f, (float)(endPosY - posY), col);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)posX, (float)(endPosY - 0.5), (float)(endPosX - posX), 0.5f, col);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, (float)(endPosX - posX + 0.5), 0.5f, col);
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(endPosX - 0.5), (float)posY, 0.5f, (float)(endPosY - posY), col);
            }
            if (ent instanceof LivingEntity && (lent = (LivingEntity)ent).getHealth() != 0.0f && this.renderHealth.getValue()) {
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 4.0), (float)posY, 1.0f, (float)(endPosY - posY), Color.BLACK);
                Color color = this.getcolor(lent.getHealth());
                float healthHeight = (float)(endPosY - (endPosY + (posY - endPosY) * (double)lent.getHealth() / (double)lent.getMaxHealth()));
                this.drawRectToBuffer(bufferBuilder, matrix, (float)(posX - 4.0), (float)(endPosY - (double)healthHeight), 1.0f, healthHeight, color);
            }
            if (ent instanceof Player) {
                Player player = (Player)ent;
                if (this.renderArmor.getValue()) {
                    double height = (endPosY - posY) / 4.0;
                    ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
                    stacks.add((ItemStack)player.getInventory().armor.get(3));
                    stacks.add((ItemStack)player.getInventory().armor.get(2));
                    stacks.add((ItemStack)player.getInventory().armor.get(1));
                    stacks.add((ItemStack)player.getInventory().armor.get(0));
                    int i = -1;
                    for (ItemStack armor : stacks) {
                        ++i;
                        if (armor.method_7960()) continue;
                        float durability = armor.getMaxDamage() - armor.getDamageValue();
                        int percent = (int)(durability / (float)armor.getMaxDamage() * 100.0f);
                        double finalH = height * (double)(percent / 100) * (double)this.durascale.getValueFloat();
                        finalH = Math.min(height, finalH);
                        this.drawRectToBuffer(bufferBuilder, matrix, (float)(endPosX + 1.5), (float)((double)((float)posY) + height * (double)i + 1.2 * (double)(i + 1)), 1.5f, (float)finalH, this.armorDuraColor.getValue());
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void drawRectToBuffer(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float width, float height, Color color) {
        float a = (float)color.getAlpha() / 255.0f;
        float r = (float)color.getRed() / 255.0f;
        float g = (float)color.getGreen() / 255.0f;
        float b = (float)color.getBlue() / 255.0f;
        bufferBuilder.method_22918(matrix, x, y, 0.0f).method_22915(r, g, b, a);
        bufferBuilder.method_22918(matrix, x, y + height, 0.0f).method_22915(r, g, b, a);
        bufferBuilder.method_22918(matrix, x + width, y + height, 0.0f).method_22915(r, g, b, a);
        bufferBuilder.method_22918(matrix, x + width, y, 0.0f).method_22915(r, g, b, a);
    }

    public void drawText(Entity ent, GuiGraphics context) {
        double x = MathUtil.interpolate(ent.xOld, ent.getX(), (double)mc.getTimer().method_60637(true));
        double y = MathUtil.interpolate(ent.yOld, ent.getY(), (double)mc.getTimer().method_60637(true));
        double z = MathUtil.interpolate(ent.zOld, ent.getZ(), (double)mc.getTimer().method_60637(true));
        AABB axisAlignedBB2 = ent.method_5829();
        AABB axisAlignedBB3 = new AABB(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
        Vec3[] vectors = new Vec3[]{new Vec3(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.minZ), new Vec3(axisAlignedBB3.minX, axisAlignedBB3.minY, axisAlignedBB3.maxZ), new Vec3(axisAlignedBB3.minX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.minY, axisAlignedBB3.maxZ), new Vec3(axisAlignedBB3.maxX, axisAlignedBB3.maxY, axisAlignedBB3.maxZ)};
        Vector4d position = null;
        for (Vec3 vector : vectors) {
            vector = TextUtil.worldSpaceToScreenSpace(new Vec3(vector.x, vector.y, vector.z));
            if (!(vector.z > 0.0) || !(vector.z < 1.0)) continue;
            if (position == null) {
                position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }
            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
        }
        if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;
            if (ent instanceof ItemEntity) {
                ItemEntity entity = (ItemEntity)ent;
                if (this.drawItem.getValue()) {
                    float diff = (float)((endPosX - posX) / 2.0);
                    float textWidth = FontManager.ui.getWidth(entity.getDisplayName().getString());
                    float tagX = (float)(posX + (double)diff - (double)(textWidth / 2.0f));
                    int count = entity.getItem().getCount();
                    context.method_51433(TwoDESP.mc.font, entity.getDisplayName().getString(), (int)tagX, (int)(posY - 10.0), this.textcolor.getValue().getRGB(), false);
                    if (this.drawItemC.getValue()) {
                        context.method_51433(TwoDESP.mc.font, " x" + count, (int)(tagX + (float)TwoDESP.mc.font.method_1727(entity.getDisplayName().getString())), (int)posY - 10, this.countColor.getValue().getRGB(), false);
                    }
                }
            }
            if (ent instanceof Player) {
                Player player = (Player)ent;
                if (this.renderArmor.getValue()) {
                    double height = (endPosY - posY) / 4.0;
                    ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
                    stacks.add((ItemStack)player.getInventory().armor.get(3));
                    stacks.add((ItemStack)player.getInventory().armor.get(2));
                    stacks.add((ItemStack)player.getInventory().armor.get(1));
                    stacks.add((ItemStack)player.getInventory().armor.get(0));
                    int i = -1;
                    for (ItemStack armor : stacks) {
                        ++i;
                        if (armor.method_7960()) continue;
                        float durability = armor.getMaxDamage() - armor.getDamageValue();
                        int percent = (int)(durability / (float)armor.getMaxDamage() * 100.0f);
                        double finalH = height * ((double)percent / 100.0) * (double)this.durascale.getValueFloat();
                        finalH = Math.min(height, finalH);
                        context.method_51427(armor, (int)(endPosX + 4.0), (int)(posY + height * (double)i + 1.2 * (double)(i + 1) + finalH / 2.0));
                    }
                }
                if (this.renderEffects.getValue()) {
                    float currentY = (float)posY - 20.0f;
                    for (MobEffectInstance effect : player.getActiveEffects()) {
                        String timeText;
                        MobEffect statusEffect = (MobEffect)effect.method_5579().comp_349();
                        String effectName = statusEffect.getDisplayName().getString();
                        int duration = effect.method_5584();
                        if (duration >= 72000) {
                            int hours = duration / 72000;
                            int minutes = duration % 72000 / 1200;
                            timeText = String.format("%dh%dm", hours, minutes);
                        } else if (duration >= 1200) {
                            int minutes2 = duration / 1200;
                            int seconds = duration % 1200 / 20;
                            timeText = String.format("%dm%ds", minutes2, seconds);
                        } else {
                            int seconds2 = duration / 20;
                            timeText = String.format("%ds", seconds2);
                        }
                        String text = effectName + " " + timeText;
                        int color = statusEffect.getColor();
                        float textWidth2 = this.font.getValue() ? FontManager.ui.getWidth(text) : TextUtil.getWidth(text);
                        float effectX = (float)(posX + (endPosX - posX) / 2.0 - (double)textWidth2 / 2.0);
                        TextUtil.drawString(context, text, effectX, currentY, color, this.font.getValue(), false);
                        currentY -= 12.0f;
                    }
                }
            }
        }
    }

    public Color getcolor(float health) {
        if (health >= 20.0f) {
            return this.hHealth.getValue();
        }
        if (20.0f > health && health > 10.0f) {
            return this.mHealth.getValue();
        }
        return this.lHealth.getValue();
    }

    public static enum TwoDESPMode {
        Setting,
        Target,
        Color;

    }
}

