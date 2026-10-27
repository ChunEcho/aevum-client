/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BakedModel
 *  net.minecraft.ChatFormatting
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Level
 *  net.minecraft.Vec3
 *  net.minecraft.Lighting
 *  net.minecraft.Font$DisplayMode
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.MultiBufferSource
 *  net.minecraft.MultiBufferSource$BufferSource
 *  net.minecraft.OverlayTexture
 *  net.minecraft.PlayerInfo
 *  net.minecraft.Holder
 *  net.minecraft.Axis
 *  net.minecraft.ItemDisplayContext
 *  net.minecraft.ItemEnchantments
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.Render3DEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.BakedModel;
import net.minecraft.ChatFormatting;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Level;
import net.minecraft.Vec3;
import net.minecraft.Lighting;
import net.minecraft.Font;
import net.minecraft.Camera;
import net.minecraft.PoseStack;
import net.minecraft.MultiBufferSource;
import net.minecraft.OverlayTexture;
import net.minecraft.PlayerInfo;
import net.minecraft.Holder;
import net.minecraft.Axis;
import net.minecraft.ItemDisplayContext;
import net.minecraft.ItemEnchantments;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class NameTags
extends Module {
    public static NameTags INSTANCE;
    final EnumSetting<Mode> modeConfig = this.add(new EnumSetting<Mode>("Mode", Mode.Alien));
    final ColorSetting colorConfig = this.add(new ColorSetting("Color", new Color(255, 255, 255)));
    final ColorSetting friendConfig = this.add(new ColorSetting("Friend", new Color(155, 155, 255)).injectBoolean(true));
    final ColorSetting ircConfig = this.add(new ColorSetting("IRC", new Color(255, 85, 255)).injectBoolean(true));
    final ColorSetting invisibleConfig = this.add(new ColorSetting("Invisible", new Color(200, 200, 200)).injectBoolean(true));
    final ColorSetting died = this.add(new ColorSetting("Died", new Color(180, 0, 0)).injectBoolean(true));
    final ColorSetting sneakingConfig = this.add(new ColorSetting("Sneaking", new Color(200, 200, 0)).injectBoolean(true));
    final ColorSetting rectConfig = this.add(new ColorSetting("Rectangle", new Color(0, 0, 0, 100)).injectBoolean(true));
    final ColorSetting trollHackBoxColor = this.add(new ColorSetting("TrollBoxColor", new Color(0, 0, 0, 130), () -> this.modeConfig.is(Mode.TrollHack)));
    final EnumSetting<MainHandSide> mainHandSideConfig = this.add(new EnumSetting<MainHandSide>("MainHand", MainHandSide.Right, () -> this.modeConfig.is(Mode.TrollHack)));
    final BooleanSetting armorConfig = this.add(new BooleanSetting("Armor", true).setParent());
    final BooleanSetting drawItemConfig = this.add(new BooleanSetting("DrawItem", true, this.armorConfig::isOpen));
    final BooleanSetting itemCountConfig = this.add(new BooleanSetting("ItemCount", true, this.armorConfig::isOpen));
    final SliderSetting offsetConfig = this.add(new SliderSetting("Offset", -20.0, -30.0, 10.0, 0.01, this.armorConfig::isOpen));
    final BooleanSetting enchantmentsConfig = this.add(new BooleanSetting("Enchantments", true));
    final BooleanSetting durabilityConfig = this.add(new BooleanSetting("Durability", true).setParent());
    final BooleanSetting forceBarConfig = this.add(new BooleanSetting("ForceBar", true, this.durabilityConfig::isOpen));
    final BooleanSetting itemNameConfig = this.add(new BooleanSetting("ItemName", false));
    final BooleanSetting entityIdConfig = this.add(new BooleanSetting("EntityId", false));
    final BooleanSetting gamemodeConfig = this.add(new BooleanSetting("Gamemode", false));
    final BooleanSetting pingConfig = this.add(new BooleanSetting("Ping", true));
    final BooleanSetting healthConfig = this.add(new BooleanSetting("Health", true));
    final BooleanSetting totemsConfig = this.add(new BooleanSetting("Totems", false));
    final SliderSetting scaleConfig = this.add(new SliderSetting("Scale", 1.0, 0.0, 3.0, 0.1));
    final BooleanSetting factorConfig = this.add(new BooleanSetting("Factor", true).setParent());
    final SliderSetting scalingConfig = this.add(new SliderSetting("Scaling", 1.0, 0.0, 3.0, 0.1, this.factorConfig::isOpen));
    final SliderSetting distanceConfig = this.add(new SliderSetting("Distance", 6.0, 0.0, 20.0, 0.1, this.factorConfig::isOpen));
    final SliderSetting heightConfig = this.add(new SliderSetting("Height", 0.0, -3.0, 3.0, 0.01));
    final DecimalFormat df = new DecimalFormat("0.0");
    private static int alienScan;

    public NameTags() {
        super("NameTags", "Renders info on player NameTags", Module.Category.Render);
        this.setChinese("\u540d\u5b57\u6807\u7b7e");
        INSTANCE = this;
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (NameTags.mc.gameRenderer != null && mc.getCameraEntity() != null) {
            if (this.modeConfig.is(Mode.Alien)) {
                this.renderAlien(event);
            } else if (this.modeConfig.is(Mode.TrollHack)) {
                this.renderTrollHack(event);
            }
        }
    }

    private void renderTrollHack(Render3DEvent event) {
        Camera camera = NameTags.mc.gameRenderer.method_19418();
        RenderSystem.enableBlend();
        GL11.glDepthFunc((int)519);
        PoseStack matrixStack = new PoseStack();
        for (Player class_16572 : Aevum.THREAD.getPlayers()) {
            if (!this.died.booleanValue && !class_16572.isAlive() || class_16572 == NameTags.mc.player && NameTags.mc.options.getCameraType().isFirstPerson() || !this.invisibleConfig.booleanValue && class_16572.isInvisible()) continue;
            Vec3 renderPosition = MathUtil.getRenderPosition((Entity)class_16572, event.tickDelta);
            double x = renderPosition.method_10216();
            double y = renderPosition.method_10214();
            double z = renderPosition.method_10215();
            this.renderTrollHackPlayer(class_16572, x, y, z, camera, matrixStack);
        }
        GL11.glDepthFunc((int)515);
        RenderSystem.disableBlend();
    }

    private void renderTrollHackPlayer(Player entity, double x, double y, double z, Camera camera, PoseStack matrices) {
        Vec3 pos = camera.getPosition();
        double eyeY = y + (double)entity.getBbHeight() + (double)(entity.isShiftKeyDown() ? 0.4f : 0.43f) + (double)this.heightConfig.getValueFloat();
        float scale = (float)((double)(-0.025f * this.scaleConfig.getValueFloat()) + (this.factorConfig.getValue() && pos.method_1028(x, eyeY, z) > (double)(this.distanceConfig.getValueFloat() * this.distanceConfig.getValueFloat()) ? (Math.sqrt(pos.method_1028(x, eyeY, z)) - (double)this.distanceConfig.getValueFloat()) * (double)-0.0025f * (double)this.scalingConfig.getValueFloat() : 0.0));
        matrices.method_22903();
        matrices.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.method_22907(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
        matrices.method_22904(x - pos.method_10216(), eyeY - pos.method_10214() + (double)((scale / -0.025f - 1.0f) / 4.0f), z - pos.method_10215());
        matrices.method_22907(Axis.YP.rotationDegrees(-camera.getYRot()));
        matrices.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.method_22905(scale, scale, -1.0f);
        this.renderTrollHackItems(matrices, entity);
        this.renderTrollHackNameAndHealth(matrices, entity);
        matrices.method_22909();
    }

    private void renderTrollHackItems(PoseStack matrices, Player player) {
        if (!this.armorConfig.getValue()) {
            return;
        }
        ArrayList armorList = new ArrayList();
        player.getInventory().armor.forEach(armorList::add);
        Collections.reverse(armorList);
        ItemStack[] displayItems = new ItemStack[6];
        if (this.mainHandSideConfig.is(MainHandSide.Right)) {
            displayItems[0] = player.getOffhandItem();
            displayItems[5] = player.getMainHandItem();
        } else {
            displayItems[0] = player.getMainHandItem();
            displayItems[5] = player.getOffhandItem();
        }
        for (int i = 0; i < 4; ++i) {
            displayItems[i + 1] = armorList.size() > i ? (ItemStack)armorList.get(i) : ItemStack.EMPTY;
        }
        float[] itemXs = new float[]{-44.0f, -26.0f, -10.0f, 6.0f, 22.0f, 40.0f};
        float itemY = -24.0f;
        int maxEnchantCount = 0;
        for (int i = 0; i < 6; ++i) {
            ItemStack stack = displayItems[i];
            if (stack == null || stack.method_7960()) continue;
            float currentX = itemXs[i];
            if (this.drawItemConfig.getValue()) {
                this.renderItemStack(matrices, stack, currentX, itemY);
            }
            this.renderItemOverlay(matrices, stack, currentX, itemY + 1.5f);
            if (stack.isDamageableItem() && this.durabilityConfig.getValue()) {
                int max = stack.getMaxDamage();
                int dmg = stack.getDamageValue();
                int percent = (int)((float)(max - dmg) / (float)max * 100.0f);
                int durabilityColor = ColorUtil.hslToColor((float)(max - dmg) / (float)max * 120.0f, 100.0f, 50.0f, 1.0f).getRGB();
                String percentStr = "" + percent;
                matrices.method_22903();
                matrices.method_46416(currentX + 8.0f, itemY + 18.0f, 0.0f);
                matrices.method_22905(0.6f, 0.6f, 1.0f);
                float strWidth = NameTags.mc.font.method_1727(percentStr);
                this.drawWithShadow(matrices, percentStr, -strWidth / 2.0f, 0.0f, durabilityColor);
                matrices.method_22909();
            }
            if (!this.enchantmentsConfig.getValue()) continue;
            int enchSize = stack.getEnchantments().method_57541();
            if (enchSize > maxEnchantCount) {
                maxEnchantCount = enchSize;
            }
            matrices.method_22903();
            matrices.method_46416(currentX + 8.0f, itemY, 0.0f);
            matrices.method_22905(0.5f, 0.5f, 1.0f);
            this.renderTrollHackEnchants(matrices, stack);
            matrices.method_22909();
        }
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.method_7960() && this.itemNameConfig.getValue()) {
            matrices.method_22903();
            matrices.method_46416(0.0f, itemY - 6.0f - (float)maxEnchantCount * 4.5f, 0.0f);
            matrices.method_22905(0.5f, 0.5f, 1.0f);
            String itemName = heldItem.getHoverName().getString();
            float nameWidth = NameTags.mc.font.method_1727(itemName);
            this.drawWithShadow(matrices, itemName, -nameWidth / 2.0f, 0.0f, -1);
            matrices.method_22909();
        }
    }

    private void renderTrollHackEnchants(PoseStack matrixStack, ItemStack itemStack) {
        if (itemStack.method_7909() == Items.ENCHANTED_GOLDEN_APPLE) {
            float strWidth = NameTags.mc.font.method_1727("God");
            this.drawWithShadow(matrixStack, "God", -strWidth / 2.0f, -9.0f, -3977663);
        } else if (itemStack.isEnchanted()) {
            ItemEnchantments enchants = EnchantmentHelper.method_57532((ItemStack)itemStack);
            float yOffset = -9.0f;
            for (Holder enchantment : enchants.method_57534()) {
                int lvl = enchants.method_57536(enchantment);
                StringBuilder enchantString = new StringBuilder();
                String translatedName = ((Enchantment)enchantment.comp_349()).toString().replace("Enchantment ", "");
                if (translatedName.contains("Vanish")) {
                    enchantString.append("\u00a7cVan");
                } else if (translatedName.contains("Bind")) {
                    enchantString.append("\u00a7cBind");
                } else {
                    int maxLen = lvl > 1 ? 2 : 3;
                    int n = maxLen;
                    if (translatedName.length() > maxLen) {
                        translatedName = translatedName.substring(0, maxLen);
                    }
                    enchantString.append(translatedName).append(lvl);
                }
                String encStr = enchantString.toString();
                float strWidth = NameTags.mc.font.method_1727(encStr);
                this.drawWithShadow(matrixStack, encStr, -strWidth / 2.0f, yOffset, -1);
                yOffset -= 9.0f;
            }
        }
    }

    private void renderTrollHackNameAndHealth(PoseStack matrices, Player player) {
        String mcName;
        String name = mcName = player.method_5477().getString();
        String ping = this.pingConfig.getValue() ? this.getRawPing(player) + "ms" : "";
        boolean drawHp = this.healthConfig.getValue();
        double health = player.getHealth() + player.getAbsorptionAmount();
        String hpStr = drawHp ? this.df.format(health) : "";
        float nameWidth = NameTags.mc.font.method_1727(name);
        float pingWidth = ping.isEmpty() ? 0.0f : (float)NameTags.mc.font.method_1727(ping);
        float hpWidth = hpStr.isEmpty() ? 0.0f : (float)NameTags.mc.font.method_1727(hpStr);
        float spaceWidth = NameTags.mc.font.method_1727(" ");
        float totalWidth = nameWidth;
        if (!ping.isEmpty()) {
            totalWidth += spaceWidth + pingWidth;
        }
        if (!hpStr.isEmpty()) {
            totalWidth += spaceWidth + hpWidth;
        }
        float startX = -totalWidth / 2.0f;
        if (this.rectConfig.booleanValue) {
            Render2DUtil.drawRect(matrices, startX - 2.0f, -1.0f, totalWidth + 4.0f, 10.0f, this.trollHackBoxColor.getValue());
        }
        float currentX = startX;
        int nameColor = this.getNametagColor(player);
        this.drawWithShadow(matrices, name, currentX, 0.0f, nameColor);
        currentX += nameWidth + (ping.isEmpty() && hpStr.isEmpty() ? 0.0f : spaceWidth);
        if (!ping.isEmpty()) {
            this.drawWithShadow(matrices, ping, currentX, 0.0f, ChatFormatting.DARK_GRAY.getColor());
            currentX += pingWidth + (hpStr.isEmpty() ? 0.0f : spaceWidth);
        }
        if (drawHp) {
            int hpColor = this.getHealthColor(health).getColor();
            this.drawWithShadow(matrices, hpStr, currentX, 0.0f, hpColor);
            float maxBase = 20.0f;
            float maxAbs = 18.0f;
            float baseHp = Math.min(player.getHealth(), maxBase);
            float absHp = Math.min(player.getAbsorptionAmount(), maxAbs);
            float boxWidth = totalWidth + 4.0f;
            float greenWidth = baseHp / maxBase * boxWidth;
            float yellowWidth = absHp / maxAbs * boxWidth;
            Render2DUtil.drawRect(matrices, startX - 2.0f, 9.5f, greenWidth, 1.5f, new Color(50, 255, 50).getRGB());
            if (absHp > 0.0f) {
                Render2DUtil.drawRect(matrices, startX - 2.0f, 8.0f, yellowWidth, 1.5f, new Color(255, 200, 0).getRGB());
            }
        }
    }

    private String getRawPing(Player entity) {
        if (mc.getConnection() == null) {
            return "-1";
        }
        PlayerInfo entry = mc.getConnection().method_2871(entity.method_5667());
        return entry == null ? "-1" : String.valueOf(entry.getLatency());
    }

    private ChatFormatting getHealthColor(double health) {
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

    private void renderAlien(Render3DEvent event) {
        Camera camera = NameTags.mc.gameRenderer.method_19418();
        RenderSystem.enableBlend();
        GL11.glDepthFunc((int)519);
        PoseStack matrixStack = new PoseStack();
        for (Player class_16572 : Aevum.THREAD.getPlayers()) {
            if (!this.died.booleanValue && !class_16572.isAlive() || class_16572 == NameTags.mc.player && NameTags.mc.options.getCameraType().isFirstPerson() || !this.invisibleConfig.booleanValue && class_16572.isInvisible()) continue;
            String info = this.getNametagInfo(class_16572);
            Vec3 renderPosition = MathUtil.getRenderPosition((Entity)class_16572, event.tickDelta);
            double x = renderPosition.method_10216();
            double y = renderPosition.method_10214();
            double z = renderPosition.method_10215();
            int width = NameTags.mc.font.method_1727(info);
            float hwidth = (float)width / 2.0f;
            this.renderInfo(info, hwidth, class_16572, x, y, z, camera, matrixStack);
        }
        GL11.glDepthFunc((int)515);
        RenderSystem.disableBlend();
    }

    private void renderInfo(String info, float width, Player entity, double x, double y, double z, Camera camera, PoseStack matrices) {
        Vec3 pos = camera.getPosition();
        double eyeY = y + (double)entity.getBbHeight() + (double)(entity.isShiftKeyDown() ? 0.4f : 0.43f) + (double)this.heightConfig.getValueFloat();
        float scale = (float)((double)(-0.025f * this.scaleConfig.getValueFloat()) + (this.factorConfig.getValue() && pos.method_1028(x, eyeY, z) > (double)(this.distanceConfig.getValueFloat() * this.distanceConfig.getValueFloat()) ? (Math.sqrt(pos.method_1028(x, eyeY, z)) - (double)this.distanceConfig.getValueFloat()) * (double)-0.0025f * (double)this.scalingConfig.getValueFloat() : 0.0));
        matrices.method_22903();
        matrices.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.method_22907(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
        matrices.method_22904(x - pos.method_10216(), eyeY - pos.method_10214() + (double)((scale / -0.025f - 1.0f) / 4.0f), z - pos.method_10215());
        matrices.method_22907(Axis.YP.rotationDegrees(-camera.getYRot()));
        matrices.method_22907(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.method_22905(scale, scale, -1.0f);
        if (this.rectConfig.booleanValue) {
            Render2DUtil.drawRect(matrices, -width - 2.0f, -1.0f, width * 2.0f + 3.0f, 10.0f, this.rectConfig.getValue());
        }
        this.drawWithShadow(matrices, info, -width, 0.0f, this.getNametagColor(entity));
        if (this.armorConfig.getValue()) {
            this.renderItems(matrices, entity);
        }
        matrices.method_22909();
    }

    private void drawWithShadow(PoseStack matrices, String info, float x, float y, int color) {
        MultiBufferSource.MultiBufferSource$BufferSource immediate = mc.renderBuffers().bufferSource();
        NameTags.mc.font.method_27521(info, x, y, color, true, matrices.last().method_23761(), (MultiBufferSource)immediate, Font.Font$DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        immediate.endBatch();
        NameTags.mc.font.method_27521(info, x, y, color, false, matrices.last().method_23761(), (MultiBufferSource)immediate, Font.Font$DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        immediate.endBatch();
    }

    private void renderItems(PoseStack matrixStack, Player player) {
        CopyOnWriteArrayList<ItemStack> displayItems = new CopyOnWriteArrayList<ItemStack>();
        if (!player.getOffhandItem().method_7960()) {
            displayItems.add(player.getOffhandItem());
        }
        player.getInventory().armor.forEach(armorStack -> {
            if (!armorStack.method_7960()) {
                displayItems.add((ItemStack)armorStack);
            }
        });
        if (!player.getMainHandItem().method_7960()) {
            displayItems.add(player.getMainHandItem());
        }
        Collections.reverse(displayItems);
        float x = 0.0f;
        int n11 = 0;
        for (ItemStack class_17992 : displayItems) {
            x -= 8.0f;
            if (class_17992.getEnchantments().method_57541() <= n11) continue;
            n11 = class_17992.getEnchantments().method_57541();
        }
        float y = this.offsetConfig.getValueFloat();
        for (ItemStack stackx : displayItems) {
            GL11.glDepthFunc((int)519);
            if (this.drawItemConfig.getValue()) {
                this.renderItemStack(matrixStack, stackx, x, y + 1.0f);
            }
            this.renderItemOverlay(matrixStack, stackx, x, y + 2.5f);
            matrixStack.method_22905(0.5f, 0.5f, 0.5f);
            if (this.durabilityConfig.getValue()) {
                this.renderDurability(matrixStack, stackx, x + 2.0f, y + 11.5f);
            }
            if (this.enchantmentsConfig.getValue()) {
                this.renderEnchants(matrixStack, stackx, x + 2.0f, y + 7.0f);
            }
            matrixStack.method_22905(2.0f, 2.0f, 2.0f);
            x += 16.0f;
            GL11.glDepthFunc((int)515);
        }
        ItemStack class_17993 = player.getMainHandItem();
        if (!class_17993.method_7960()) {
            matrixStack.method_22905(0.5f, 0.5f, 0.5f);
            if (this.itemNameConfig.getValue()) {
                this.renderItemName(matrixStack, class_17993, y - 4.5f + this.enchantOffset(n11));
            }
            matrixStack.method_22905(2.0f, 2.0f, 2.0f);
        }
    }

    private void renderItemStack(PoseStack matrixStack, ItemStack stack, float x, float y) {
        matrixStack.method_22903();
        matrixStack.method_46416(x, y, 0.0f);
        matrixStack.method_46416(8.0f, 8.0f, 0.0f);
        matrixStack.method_22905(16.0f, 16.0f, 1.0E-8f);
        matrixStack.method_34425(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        Lighting.setupForFlatItems();
        BakedModel model = mc.getItemRenderer().method_4019(stack, (Level)NameTags.mc.level, null, 0);
        MultiBufferSource.MultiBufferSource$BufferSource i = mc.renderBuffers().bufferSource();
        mc.getItemRenderer().method_23179(stack, ItemDisplayContext.GUI, false, matrixStack, (MultiBufferSource)i, 0xFF0000, OverlayTexture.NO_OVERLAY, model);
        i.endBatch();
        Lighting.setupFor3DItems();
        matrixStack.method_22909();
    }

    private void renderItemOverlay(PoseStack matrixStack, ItemStack stack, float x, float y) {
        matrixStack.method_22903();
        if (this.itemCountConfig.getValue() && stack.getCount() != 1) {
            String string = String.valueOf(stack.getCount());
            this.drawWithShadow(matrixStack, string, x + 17.0f - (float)NameTags.mc.font.method_1727(string), y + 9.0f, -1);
        }
        if (stack.isBarVisible() || stack.isDamageableItem() && this.forceBarConfig.getValue()) {
            int i = stack.getBarWidth();
            int j = stack.getBarColor();
            float k = x + 2.0f;
            float l = y + 13.0f;
            Render2DUtil.drawRect(matrixStack, k, l, 13.0f, 2.0f, -16777216);
            Render2DUtil.drawRect(matrixStack, k, l, (float)i, 1.0f, j | 0xFF000000);
        }
        matrixStack.method_22909();
    }

    private void renderDurability(PoseStack matrixStack, ItemStack itemStack, float x, float y) {
        if (itemStack.isDamageableItem()) {
            int n = itemStack.getMaxDamage();
            int n2 = itemStack.getDamageValue();
            int durability = (int)((float)(n - n2) / (float)n * 100.0f);
            this.drawWithShadow(matrixStack, durability + "%", x * 2.0f, y * 2.0f, ColorUtil.hslToColor((float)(n - n2) / (float)n * 120.0f, 100.0f, 50.0f, 1.0f).getRGB());
        }
    }

    private void renderEnchants(PoseStack matrixStack, ItemStack itemStack, float x, float y) {
        if (itemStack.method_7909() == Items.ENCHANTED_GOLDEN_APPLE) {
            this.drawWithShadow(matrixStack, "God", x * 2.0f, y * 2.0f, -3977663);
        } else if (itemStack.isEnchanted()) {
            ItemEnchantments enchants = EnchantmentHelper.method_57532((ItemStack)itemStack);
            float n2 = 0.0f;
            for (Holder enchantment : enchants.method_57534()) {
                int lvl = enchants.method_57536(enchantment);
                StringBuilder enchantString = new StringBuilder();
                String translatedName = ((Enchantment)enchantment.comp_349()).toString().replace("Enchantment ", "");
                if (translatedName.contains("Vanish")) {
                    enchantString.append("\u00a7cVan");
                } else if (translatedName.contains("Bind")) {
                    enchantString.append("\u00a7cBind");
                } else {
                    int maxLen = lvl > 1 ? 2 : 3;
                    int n = maxLen;
                    if (translatedName.length() > maxLen) {
                        translatedName = translatedName.substring(0, maxLen);
                    }
                    enchantString.append(translatedName);
                    enchantString.append(lvl);
                }
                this.drawWithShadow(matrixStack, enchantString.toString(), x * 2.0f, (y + n2) * 2.0f, -1);
                n2 -= 4.5f;
            }
        }
    }

    private float enchantOffset(int n) {
        if (this.enchantmentsConfig.getValue() && n > 2) {
            float value = -2.0f;
            return value - (float)(n - 3) * 4.5f;
        }
        return 0.0f;
    }

    private void renderItemName(PoseStack matrixStack, ItemStack itemStack, float y) {
        String itemName = itemStack.getHoverName().getString();
        float width = (float)NameTags.mc.font.method_1727(itemName) / 4.0f;
        this.drawWithShadow(matrixStack, itemName, (0.0f - width) * 2.0f, y * 2.0f, -1);
    }

    private String getNametagInfo(Player player) {
        int totems;
        StringBuilder info = new StringBuilder();
        if (this.gamemodeConfig.getValue()) {
            if (player.method_7337()) {
                info.append(ChatFormatting.GOLD);
                info.append("[C] ");
            } else if (player.isSpectator()) {
                info.append(ChatFormatting.GRAY);
                info.append("[I] ");
            } else {
                info.append(ChatFormatting.BOLD);
                info.append("[S] ");
            }
        }
        if (this.pingConfig.getValue()) {
            info.append(this.getEntityPing(player));
            info.append("ms ");
            info.append(ChatFormatting.RESET);
        }
        String mcName = player.method_5477().getString();
        info.append(mcName);
        info.append(" ");
        if (this.entityIdConfig.getValue()) {
            info.append("ID: ");
            info.append(player.method_5628());
            info.append(" ");
        }
        if (this.healthConfig.getValue()) {
            double health = player.getHealth() + player.getAbsorptionAmount();
            ChatFormatting hcolor = health > 18.0 ? ChatFormatting.GREEN : (health > 16.0 ? ChatFormatting.DARK_GREEN : (health > 12.0 ? ChatFormatting.YELLOW : (health > 8.0 ? ChatFormatting.GOLD : (health > 4.0 ? ChatFormatting.RED : ChatFormatting.DARK_RED))));
            String phealth = this.df.format(health);
            info.append(hcolor);
            info.append(phealth);
            info.append(" ");
        }
        if (this.totemsConfig.getValue() && (totems = Aevum.POP.getPop(player)) > 0) {
            ChatFormatting c = totems > 2 ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
            info.append(c);
            info.append(-totems);
            info.append(" ");
        }
        return info.toString().trim();
    }

    private String getEntityPing(Player entity) {
        if (mc.getConnection() == null) {
            return "\u00a77-1";
        }
        PlayerInfo playerListEntry = mc.getConnection().method_2871(entity.method_5667());
        if (playerListEntry == null) {
            return "\u00a77-1";
        }
        int ping = playerListEntry.getLatency();
        ChatFormatting color = ping >= 200 ? ChatFormatting.RED : (ping >= 100 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
        return color.toString() + ping;
    }

    private int getNametagColor(Player player) {
        String mcName = player.getGameProfile().getName();
        if (this.friendConfig.booleanValue && player.getDisplayName() != null && Aevum.FRIEND.isFriend(player)) {
            return this.friendConfig.getValue().getRGB();
        }
        if (this.invisibleConfig.booleanValue && player.isInvisible()) {
            return this.invisibleConfig.getValue().getRGB();
        }
        if (this.sneakingConfig.booleanValue && player.isShiftKeyDown()) {
            return this.sneakingConfig.getValue().getRGB();
        }
        return !player.isAlive() ? this.died.getValue().getRGB() : this.colorConfig.getValue().getRGB();
    }

    static {
        alienScan = 0;
    }

    public static enum Mode {
        Alien,
        TrollHack;

    }

    public static enum MainHandSide {
        Left,
        Right;

    }
}

