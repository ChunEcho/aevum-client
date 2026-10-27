/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Arrow
 *  net.minecraft.ThrownExperienceBottle
 *  net.minecraft.ThrownEnderpearl
 *  net.minecraft.BowItem
 *  net.minecraft.CrossbowItem
 *  net.minecraft.EggItem
 *  net.minecraft.EnderpearlItem
 *  net.minecraft.ExperienceBottleItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.LingeringPotionItem
 *  net.minecraft.SnowballItem
 *  net.minecraft.SplashPotionItem
 *  net.minecraft.TridentItem
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.Mth
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 *  net.minecraft.Holder
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.player.AutoPearl;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import java.awt.Color;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Arrow;
import net.minecraft.ThrownExperienceBottle;
import net.minecraft.ThrownEnderpearl;
import net.minecraft.BowItem;
import net.minecraft.CrossbowItem;
import net.minecraft.EggItem;
import net.minecraft.EnderpearlItem;
import net.minecraft.ExperienceBottleItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.LingeringPotionItem;
import net.minecraft.SnowballItem;
import net.minecraft.SplashPotionItem;
import net.minecraft.TridentItem;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Mth;
import net.minecraft.ClipContext;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;
import net.minecraft.Holder;

public class Trajectories
extends Module {
    static PoseStack matrixStack;
    private final ColorSetting hand = this.add(new ColorSetting("Hand", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting pearl = this.add(new ColorSetting("Pearl", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting arrow = this.add(new ColorSetting("Arrow", new Color(255, 255, 255, 255)).injectBoolean(true));
    private final ColorSetting xp = this.add(new ColorSetting("XP", new Color(255, 255, 255, 255)).injectBoolean(true));

    public Trajectories() {
        super("Trajectories", Module.Category.Render);
        this.setChinese("\u629b\u7269\u7ebf\u9884\u6d4b");
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (!Trajectories.nullCheck()) {
            Trajectories.matrixStack = matrixStack;
            if (this.pearl.booleanValue || this.arrow.booleanValue || this.xp.booleanValue) {
                RenderSystem.disableDepthTest();
                for (Entity en : Aevum.THREAD.getEntities()) {
                    if (en instanceof ThrownEnderpearl && this.pearl.booleanValue) {
                        this.calcTrajectory(en, this.pearl.getValue());
                    }
                    if (en instanceof Arrow && this.arrow.booleanValue) {
                        this.calcTrajectory(en, this.arrow.getValue());
                    }
                    if (!(en instanceof ThrownExperienceBottle) || !this.xp.booleanValue) continue;
                    this.calcTrajectory(en, this.xp.getValue());
                }
                RenderSystem.enableDepthTest();
            }
            if (this.hand.booleanValue) {
                InteractionHand hand;
                if (!Trajectories.mc.options.getCameraType().isFirstPerson()) {
                    return;
                }
                ItemStack mainHand = Trajectories.mc.player.getMainHandItem();
                ItemStack offHand = Trajectories.mc.player.getOffhandItem();
                if (!(mainHand.method_7909() instanceof BowItem || mainHand.method_7909() instanceof CrossbowItem || this.isThrowable(mainHand.method_7909()) || AutoPearl.INSTANCE.isOn())) {
                    if (!(offHand.method_7909() instanceof BowItem || offHand.method_7909() instanceof CrossbowItem || this.isThrowable(offHand.method_7909()))) {
                        return;
                    }
                    hand = InteractionHand.OFF_HAND;
                } else {
                    hand = InteractionHand.MAIN_HAND;
                }
                RenderSystem.disableDepthTest();
                boolean prev_bob = (Boolean)Trajectories.mc.options.bobView().method_41753();
                Trajectories.mc.options.bobView().method_41748((Object)false);
                double x = MathUtil.interpolate(Trajectories.mc.player.xo, Trajectories.mc.player.getX(), (double)mc.getTimer().method_60637(true));
                double y = MathUtil.interpolate(Trajectories.mc.player.yo, Trajectories.mc.player.getY(), (double)mc.getTimer().method_60637(true));
                double z = MathUtil.interpolate(Trajectories.mc.player.zo, Trajectories.mc.player.getZ(), (double)mc.getTimer().method_60637(true));
                if (offHand.method_7909() instanceof CrossbowItem && EnchantmentHelper.method_8225((Holder)((Holder)Trajectories.mc.level.method_30349().method_46762(Enchantments.MULTISHOT.method_58273()).method_46746(Enchantments.MULTISHOT).get()), (ItemStack)offHand) != 0 || mainHand.method_7909() instanceof CrossbowItem && EnchantmentHelper.method_8225((Holder)((Holder)Trajectories.mc.level.method_30349().method_46762(Enchantments.MULTISHOT.method_58273()).method_46746(Enchantments.MULTISHOT).get()), (ItemStack)mainHand) != 0) {
                    this.calcTrajectory(hand == InteractionHand.OFF_HAND ? offHand.method_7909() : mainHand.method_7909(), Trajectories.mc.player.getYRot() - 10.0f, x, y, z);
                    this.calcTrajectory(hand == InteractionHand.OFF_HAND ? offHand.method_7909() : mainHand.method_7909(), Trajectories.mc.player.getYRot(), x, y, z);
                    this.calcTrajectory(hand == InteractionHand.OFF_HAND ? offHand.method_7909() : mainHand.method_7909(), Trajectories.mc.player.getYRot() + 10.0f, x, y, z);
                } else {
                    this.calcTrajectory(hand == InteractionHand.OFF_HAND ? offHand.method_7909() : mainHand.method_7909(), Trajectories.mc.player.getYRot(), x, y, z);
                }
                Trajectories.mc.options.bobView().method_41748((Object)prev_bob);
                RenderSystem.enableDepthTest();
            }
        }
    }

    private void calcTrajectory(Entity e, Color color) {
        double motionX = e.getDeltaMovement().x;
        double motionY = e.getDeltaMovement().y;
        double motionZ = e.getDeltaMovement().z;
        if (motionX != 0.0 || motionY != 0.0 || motionZ != 0.0) {
            double x = e.getX();
            double y = e.getY();
            double z = e.getZ();
            for (int i = 0; i < 300; ++i) {
                Vec3 lastPos = new Vec3(x, y, z);
                if (Trajectories.mc.level.method_8320(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).method_26204() == Blocks.WATER) {
                    motionX *= 0.8;
                    motionY *= 0.8;
                    motionZ *= 0.8;
                } else {
                    motionX *= 0.99;
                    motionY *= 0.99;
                    motionZ *= 0.99;
                }
                motionY = e instanceof Arrow ? (motionY = motionY - (double)0.05f) : (motionY = motionY - (double)0.03f);
                Vec3 pos = new Vec3(x, y, z);
                if (Trajectories.mc.level.method_17742(new ClipContext(lastPos, pos, ClipContext.ClipContext$Block.OUTLINE, ClipContext.ClipContext$Fluid.NONE, (Entity)Trajectories.mc.player)) != null && (Trajectories.mc.level.method_17742(new ClipContext(lastPos, pos, ClipContext.ClipContext$Block.OUTLINE, ClipContext.ClipContext$Fluid.NONE, (Entity)Trajectories.mc.player)).method_17783() == HitResult.HitResult$Type.ENTITY || Trajectories.mc.level.method_17742(new ClipContext(lastPos, pos, ClipContext.ClipContext$Block.OUTLINE, ClipContext.ClipContext$Fluid.NONE, (Entity)Trajectories.mc.player)).method_17783() == HitResult.HitResult$Type.BLOCK) || y <= -65.0) break;
                int alpha = (int)MathUtil.clamp(255.0f * ((float)(i + 1) / 10.0f), 0.0f, 255.0f);
                Render3DUtil.drawLine(lastPos, pos, ColorUtil.injectAlpha(color, alpha));
            }
        }
    }

    private void calcTrajectory(Item item, float yaw, double x, double y, double z) {
        y = y + (double)Trajectories.mc.player.method_18381(Trajectories.mc.player.getPose()) - 0.1000000014901161;
        if (item == Trajectories.mc.player.getMainHandItem().method_7909()) {
            x -= (double)(Mth.method_15362((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
            z -= (double)(Mth.method_15374((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
        } else {
            x += (double)(Mth.method_15362((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
            z += (double)(Mth.method_15374((float)(yaw / 180.0f * (float)Math.PI)) * 0.16f);
        }
        float maxDist = this.getDistance(item);
        double motionX = -Mth.method_15374((float)(yaw / 180.0f * (float)Math.PI)) * Mth.method_15362((float)(Trajectories.mc.player.getXRot() / 180.0f * (float)Math.PI)) * maxDist;
        double motionY = -Mth.method_15374((float)((Trajectories.mc.player.getXRot() - (float)this.getThrowPitch(item)) / 180.0f * 3.141593f)) * maxDist;
        double motionZ = Mth.method_15362((float)(yaw / 180.0f * (float)Math.PI)) * Mth.method_15362((float)(Trajectories.mc.player.getXRot() / 180.0f * (float)Math.PI)) * maxDist;
        float power = (float)Trajectories.mc.player.getTicksUsingItem() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        float distance = Mth.method_15355((float)((float)(motionX * motionX + motionY * motionY + motionZ * motionZ)));
        motionX /= (double)distance;
        motionY /= (double)distance;
        motionZ /= (double)distance;
        float pow = (item instanceof BowItem ? power * 2.0f : (item instanceof CrossbowItem ? 2.2f : 1.0f)) * this.getThrowVelocity(item);
        motionX *= (double)pow;
        motionY *= (double)pow;
        motionZ *= (double)pow;
        motionX += Trajectories.mc.player.getDeltaMovement().method_10216();
        motionY += Trajectories.mc.player.getDeltaMovement().method_10214();
        motionZ += Trajectories.mc.player.getDeltaMovement().method_10215();
        for (int i = 0; i < 300; ++i) {
            BlockHitResult bhr;
            Vec3 lastPos = new Vec3(x, y, z);
            if (Trajectories.mc.level.method_8320(new BlockPos((int)(x += motionX), (int)(y += motionY), (int)(z += motionZ))).method_26204() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }
            motionY = item instanceof BowItem ? (motionY = motionY - (double)0.05f) : (Trajectories.mc.player.getMainHandItem().method_7909() instanceof CrossbowItem ? (motionY = motionY - (double)0.05f) : (motionY = motionY - (double)0.03f));
            Vec3 pos = new Vec3(x, y, z);
            for (Entity ent : Aevum.THREAD.getEntities()) {
                if (ent instanceof Arrow || ent.equals((Object)Trajectories.mc.player) || !ent.method_5829().method_994(new AABB(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3))) continue;
                Render3DUtil.drawBox(matrixStack, ent.method_5829(), this.hand.getValue());
                break;
            }
            if ((bhr = Trajectories.mc.level.method_17742(new ClipContext(lastPos, pos, ClipContext.ClipContext$Block.OUTLINE, ClipContext.ClipContext$Fluid.NONE, (Entity)Trajectories.mc.player))) != null && bhr.method_17783() == HitResult.HitResult$Type.BLOCK) {
                Render3DUtil.drawBox(matrixStack, new AABB(bhr.method_17777()), this.hand.getValue());
                break;
            }
            if (y <= -65.0) break;
            if (motionX == 0.0 && motionY == 0.0 && motionZ == 0.0) continue;
            Render3DUtil.drawLine(lastPos, pos, this.hand.getValue());
        }
    }

    private boolean isThrowable(Item item) {
        return item instanceof EnderpearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return 0.5f;
        }
        if (item instanceof ExperienceBottleItem) {
            return 0.59f;
        }
        return item instanceof TridentItem ? 2.0f : 1.5f;
    }

    private int getThrowPitch(Item item) {
        return !(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem) && !(item instanceof ExperienceBottleItem) ? 0 : 20;
    }
}

