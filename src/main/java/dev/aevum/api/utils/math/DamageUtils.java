/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.CombatRules
 *  net.minecraft.DamageSource
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.LivingEntity
 *  net.minecraft.AttributeModifier
 *  net.minecraft.AttributeInstance
 *  net.minecraft.Player
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.BlockGetter
 *  net.minecraft.GameType
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Position
 *  net.minecraft.AABB
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.BlockState
 *  net.minecraft.Heightmap$Types
 *  net.minecraft.EntityTypeTags
 *  net.minecraft.Mth
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.BlockHitResult
 *  net.minecraft.Attributes
 *  net.minecraft.ResourceKey
 *  net.minecraft.PlayerInfo
 *  net.minecraft.Holder
 *  net.minecraft.DamageTypeTags
 *  net.minecraft.ItemAttributeModifiers
 *  net.minecraft.ItemEnchantments
 *  net.minecraft.DataComponents
 *  net.minecraft.MaceItem
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Math
 */
package dev.aevum.api.utils.math;

import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.core.impl.PlayerManager;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.Criticals;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.CombatRules;
import net.minecraft.DamageSource;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.EquipmentSlot;
import net.minecraft.LivingEntity;
import net.minecraft.AttributeModifier;
import net.minecraft.AttributeInstance;
import net.minecraft.Player;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.BlockGetter;
import net.minecraft.GameType;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Position;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.BlockState;
import net.minecraft.Heightmap;
import net.minecraft.EntityTypeTags;
import net.minecraft.Mth;
import net.minecraft.ClipContext;
import net.minecraft.BlockHitResult;
import net.minecraft.Attributes;
import net.minecraft.ResourceKey;
import net.minecraft.PlayerInfo;
import net.minecraft.Holder;
import net.minecraft.DamageTypeTags;
import net.minecraft.ItemAttributeModifiers;
import net.minecraft.ItemEnchantments;
import net.minecraft.DataComponents;
import net.minecraft.MaceItem;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class DamageUtils {
    public static final RaycastFactory HIT_FACTORY = (context, blockPos) -> {
        BlockState blockState = Wrapper.mc.level.method_8320(blockPos);
        if (blockState.method_26204().getExplosionResistance() < 600.0f) {
            return null;
        }
        return blockState.method_26220((BlockGetter)Wrapper.mc.level, blockPos).method_1092(context.start(), context.end(), blockPos);
    };

    public static float calculateDamage(BlockPos pos, LivingEntity entity) {
        return DamageUtils.explosionDamage(entity, null, new Vec3((double)pos.method_10263() + 0.5, (double)(pos.method_10264() + 1), (double)pos.getZ() + 0.5), 12.0f);
    }

    public static float calculateDamage(Vec3 pos, LivingEntity entity) {
        return DamageUtils.explosionDamage(entity, null, pos, 12.0f);
    }

    public static float explosionDamage(LivingEntity target, Vec3 targetPos, AABB targetBox, Vec3 explosionPos, float power, RaycastFactory raycastFactory) {
        double modDistance = DamageUtils.getDistance(targetPos.x, targetPos.y, targetPos.z, explosionPos.x, explosionPos.y, explosionPos.z);
        if (modDistance > (double)power) {
            return 0.0f;
        }
        double exposure = DamageUtils.getExposure(explosionPos, targetBox, raycastFactory);
        double impact = (1.0 - modDistance / (double)power) * exposure;
        float damage = (int)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
        return DamageUtils.calculateReductionsExplosion(damage, target, Wrapper.mc.level.damageSources().method_48807(null));
    }

    public static float anchorDamage(LivingEntity target, LivingEntity predict, Vec3 anchor) {
        return DamageUtils.overridingExplosionDamage(target, predict, anchor, 10.0f, BlockPos.method_49638((Position)anchor), Blocks.AIR.defaultBlockState());
    }

    public static float overridingExplosionDamage(LivingEntity target, LivingEntity predict, Vec3 explosionPos, float power, BlockPos overridePos, BlockState overrideState) {
        return DamageUtils.explosionDamage(target, predict, explosionPos, power, DamageUtils.getOverridingHitFactory(overridePos, overrideState));
    }

    private static float explosionDamage(LivingEntity target, LivingEntity predict, Vec3 explosionPos, float power, RaycastFactory raycastFactory) {
        Player player;
        if (target == null) {
            return 0.0f;
        }
        if (target instanceof Player && DamageUtils.getGameMode(player = (Player)target) == GameType.CREATIVE) {
            return 0.0f;
        }
        return DamageUtils.explosionDamage(target, predict != null ? predict.position() : target.position(), predict != null ? predict.method_5829() : target.method_5829(), explosionPos, power, raycastFactory);
    }

    public static float explosionDamage(LivingEntity target, LivingEntity predict, Vec3 explosionPos, float power) {
        Player player;
        if (target == null) {
            return 0.0f;
        }
        if (target instanceof Player && DamageUtils.getGameMode(player = (Player)target) == GameType.CREATIVE) {
            return 0.0f;
        }
        return DamageUtils.explosionDamage(target, predict != null ? predict.position() : target.position(), predict != null ? predict.method_5829() : target.method_5829(), explosionPos, power, HIT_FACTORY);
    }

    public static RaycastFactory getOverridingHitFactory(BlockPos overridePos, BlockState overrideState) {
        return (context, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals((Object)overridePos)) {
                blockState = overrideState;
            } else {
                blockState = Wrapper.mc.level.method_8320(blockPos);
                if (blockState.method_26204().getExplosionResistance() < 600.0f) {
                    return null;
                }
            }
            return blockState.method_26220((BlockGetter)Wrapper.mc.level, blockPos).method_1092(context.start(), context.end(), blockPos);
        };
    }

    public static float getAttackDamage(LivingEntity attacker, LivingEntity target) {
        DamageSource damageSource;
        float itemDamage = (float)attacker.method_45325(Attributes.ATTACK_DAMAGE);
        if (attacker instanceof Player) {
            Player player = (Player)attacker;
            damageSource = Wrapper.mc.level.damageSources().method_48802(player);
        } else {
            damageSource = Wrapper.mc.level.damageSources().method_48812(attacker);
        }
        DamageSource damageSource2 = damageSource;
        MobEffectInstance effect = attacker.method_6112(MobEffects.DAMAGE_BOOST);
        if (effect != null) {
            itemDamage += 3.0f * (float)(effect.method_5578() + 1);
        }
        float damage = DamageUtils.modifyAttackDamage(attacker, target, attacker.getWeaponItem(), damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    public static float getAttackDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        DamageSource damageSource;
        AttributeInstance original = attacker.method_5996(Attributes.ATTACK_DAMAGE);
        AttributeInstance copy = new AttributeInstance(Attributes.ATTACK_DAMAGE, o -> {});
        copy.method_6192(original.method_6201());
        for (AttributeModifier modifier2 : original.method_6195()) {
            copy.method_26835(modifier2);
        }
        copy.method_6200(Item.BASE_ATTACK_DAMAGE_ID);
        ItemAttributeModifiers attributeModifiers = (ItemAttributeModifiers)weapon.method_57824(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.method_57482(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry == Attributes.ATTACK_DAMAGE) {
                    copy.method_55696(modifier);
                }
            });
        }
        float itemDamage = (float)copy.method_6194();
        if (attacker instanceof Player) {
            Player player = (Player)attacker;
            damageSource = Wrapper.mc.level.damageSources().method_48802(player);
        } else {
            damageSource = Wrapper.mc.level.damageSources().method_48812(attacker);
        }
        DamageSource damageSource2 = damageSource;
        float damage = DamageUtils.modifyAttackDamage(attacker, target, weapon, damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    private static float modifyAttackDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, DamageSource damageSource, float damage) {
        int smite;
        int impaling;
        int baneOfArthropods;
        Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
        DamageUtils.getEnchantments(weapon, (Object2IntMap<Holder<Enchantment>>)enchantments);
        float enchantDamage = 0.0f;
        int sharpness = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.SHARPNESS);
        if (sharpness > 0) {
            enchantDamage += 1.0f + 0.5f * (float)(sharpness - 1);
        }
        if ((baneOfArthropods = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) > 0 && target.getType().method_20210(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            enchantDamage += 2.5f * (float)baneOfArthropods;
        }
        if ((impaling = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.IMPALING)) > 0 && target.getType().method_20210(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
            enchantDamage += 2.5f * (float)impaling;
        }
        if ((smite = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.SMITE)) > 0 && target.getType().method_20210(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            enchantDamage += 2.5f * (float)smite;
        }
        if (attacker instanceof Player) {
            Player playerEntity = (Player)attacker;
            float charge = playerEntity.getAttackStrengthScale(0.5f);
            damage *= 0.2f + charge * charge * 0.8f;
            enchantDamage *= charge;
            Item item2 = weapon.method_7909();
            if (item2 instanceof MaceItem) {
                float f;
                MaceItem item = (MaceItem)item2;
                float bonusDamage = item.method_58403((Entity)target, damage, damageSource);
                if (f > 0.0f) {
                    int density = DamageUtils.getEnchantmentLevel(weapon, (ResourceKey<Enchantment>)Enchantments.DENSITY);
                    if (density > 0) {
                        bonusDamage += 0.5f * attacker.fallDistance;
                    }
                    damage += bonusDamage;
                }
            }
            if (!(!(charge > 0.9f) || !(attacker.fallDistance > 0.0f) && (attacker != Wrapper.mc.player || !Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground) || !Wrapper.mc.player.onGround() && Criticals.INSTANCE.onlyGround.getValue()) || attacker.onGround() && (attacker != Wrapper.mc.player || !Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground)) || attacker.onClimbable() || attacker.isInWater() || attacker.method_6059(MobEffects.BLINDNESS) || attacker.isPassenger())) {
                damage *= 1.5f;
            }
        }
        return damage + enchantDamage;
    }

    public static float fallDamage(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().flying) {
                return 0.0f;
            }
        }
        if (entity.method_6059(MobEffects.SLOW_FALLING) || entity.method_6059(MobEffects.LEVITATION)) {
            return 0.0f;
        }
        int surface = Wrapper.mc.level.getChunkAt(entity.method_24515()).method_12032(Heightmap.Heightmap$Types.MOTION_BLOCKING).method_12603(entity.getBlockX() & 0xF, entity.getBlockZ() & 0xF);
        if (entity.getBlockY() >= surface) {
            return DamageUtils.fallDamageReductions(entity, surface);
        }
        BlockHitResult raycastResult = Wrapper.mc.level.method_17742(new ClipContext(entity.position(), new Vec3(entity.getX(), (double)Wrapper.mc.level.method_31607(), entity.getZ()), ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.WATER, (Entity)entity));
        if (raycastResult.method_17783() == HitResult.HitResult$Type.MISS) {
            return 0.0f;
        }
        return DamageUtils.fallDamageReductions(entity, raycastResult.method_17777().method_10264());
    }

    private static float fallDamageReductions(LivingEntity entity, int surface) {
        int fallHeight = (int)(entity.getY() - (double)surface + (double)entity.fallDistance - 3.0);
        @Nullable MobEffectInstance jumpBoostInstance = entity.method_6112(MobEffects.JUMP);
        if (jumpBoostInstance != null) {
            fallHeight -= jumpBoostInstance.method_5578() + 1;
        }
        return DamageUtils.calculateReductions(fallHeight, entity, Wrapper.mc.level.damageSources().fall());
    }

    public static float calculateReductionsExplosion(float damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.scalesWithDifficulty()) {
            switch (Wrapper.mc.level.getDifficulty()) {
                case EASY: {
                    damage = Math.min(damage / 2.0f + 1.0f, damage);
                    break;
                }
                case HARD: {
                    damage *= 1.5f;
                }
            }
        }
        damage = CombatRules.method_5496((LivingEntity)entity, (float)damage, (DamageSource)damageSource, (float)DamageUtils.getArmor(entity), (float)((float)DamageUtils.getARMOR_TOUGHNESS(entity)));
        damage = DamageUtils.resistanceReduction(entity, damage);
        damage = CombatRules.method_5497((float)damage, (float)DamageUtils.getProtectionAmount(entity.method_5661()));
        return Math.max(damage, 0.0f);
    }

    public static float calculateReductions(float damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.scalesWithDifficulty()) {
            switch (Wrapper.mc.level.getDifficulty()) {
                case EASY: {
                    damage = Math.min(damage / 2.0f + 1.0f, damage);
                    break;
                }
                case HARD: {
                    damage *= 1.5f;
                }
            }
        }
        damage = CombatRules.method_5496((LivingEntity)entity, (float)damage, (DamageSource)damageSource, (float)DamageUtils.getArmor(entity), (float)((float)DamageUtils.getARMOR_TOUGHNESS(entity)));
        damage = DamageUtils.resistanceReduction(entity, damage);
        damage = DamageUtils.protectionReduction(entity, damage, damageSource);
        return Math.max(damage, 0.0f);
    }

    public static double getARMOR_TOUGHNESS(LivingEntity entity) {
        Player player;
        PlayerManager.EntityAttribute entityAttribute;
        if (entity instanceof Player && (entityAttribute = Aevum.PLAYER.map.get(player = (Player)entity)) != null) {
            return entityAttribute.toughness();
        }
        return entity.method_45325(Attributes.ARMOR_TOUGHNESS);
    }

    private static float getArmor(LivingEntity entity) {
        Player player;
        PlayerManager.EntityAttribute entityAttribute;
        if (entity instanceof Player && (entityAttribute = Aevum.PLAYER.map.get(player = (Player)entity)) != null) {
            return entityAttribute.armor();
        }
        return (float)Math.floor(entity.method_45325(Attributes.ARMOR));
    }

    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        if (source.method_48789(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        int damageProtection = 0;
        for (ItemStack stack : player.getArmorAndBodyArmorSlots()) {
            int featherFalling;
            int projectileProtection;
            int blastProtection;
            int fireProtection;
            Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
            DamageUtils.getEnchantments(stack, (Object2IntMap<Holder<Enchantment>>)enchantments);
            int protection = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.PROTECTION);
            if (protection > 0) {
                damageProtection += protection;
            }
            if ((fireProtection = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.FIRE_PROTECTION)) > 0 && source.method_48789(DamageTypeTags.IS_FIRE)) {
                damageProtection += 2 * fireProtection;
            }
            if ((blastProtection = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.BLAST_PROTECTION)) > 0 && source.method_48789(DamageTypeTags.IS_EXPLOSION)) {
                damageProtection += 2 * blastProtection;
            }
            if ((projectileProtection = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION)) > 0 && source.method_48789(DamageTypeTags.IS_PROJECTILE)) {
                damageProtection += 2 * projectileProtection;
            }
            if ((featherFalling = DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.FEATHER_FALLING)) <= 0 || !source.method_48789(DamageTypeTags.IS_FALL)) continue;
            damageProtection += 3 * featherFalling;
        }
        return CombatRules.method_5497((float)damage, (float)damageProtection);
    }

    public static int getProtectionAmount(Iterable<ItemStack> equipment) {
        MutableInt mutableInt = new MutableInt();
        equipment.forEach(i -> mutableInt.add(DamageUtils.getProtectionAmount(i)));
        return mutableInt.intValue();
    }

    public static int getProtectionAmount(ItemStack stack) {
        int modifierBlast = EnchantmentHelper.method_8225((Holder)((Holder)Wrapper.mc.level.method_30349().method_30530(Enchantments.BLAST_PROTECTION.method_58273()).method_40264(Enchantments.BLAST_PROTECTION).get()), (ItemStack)stack);
        int modifier = EnchantmentHelper.method_8225((Holder)((Holder)Wrapper.mc.level.method_30349().method_30530(Enchantments.PROTECTION.method_58273()).method_40264(Enchantments.PROTECTION).get()), (ItemStack)stack);
        return modifierBlast * 2 + modifier;
    }

    private static float resistanceReduction(LivingEntity player, float damage) {
        MobEffectInstance resistance = player.method_6112(MobEffects.DAMAGE_RESISTANCE);
        if (resistance != null) {
            int lvl = resistance.method_5578() + 1;
            damage *= 1.0f - (float)lvl * 0.2f;
        }
        return Math.max(damage, 0.0f);
    }

    private static float getExposure(Vec3 source, AABB box, RaycastFactory raycastFactory) {
        if (ClientSetting.INSTANCE.optimizedCalc.getValue()) {
            int miss = 0;
            int hit = 0;
            for (int k = 0; k <= 1; ++k) {
                for (int l = 0; l <= 1; ++l) {
                    for (int m = 0; m <= 1; ++m) {
                        double p;
                        double o;
                        double n = Mth.method_16436((double)k, (double)box.minX, (double)box.maxX);
                        Vec3 vec3d = new Vec3(n, o = Mth.method_16436((double)l, (double)box.minY, (double)box.maxY), p = Mth.method_16436((double)m, (double)box.minZ, (double)box.maxZ));
                        if (DamageUtils.raycast(vec3d, source, CombatUtil.terrainIgnore) == HitResult.HitResult$Type.MISS) {
                            ++miss;
                        }
                        ++hit;
                    }
                }
            }
            return (float)miss / (float)hit;
        }
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;
        double xStep = 1.0 / (xDiff * 2.0 + 1.0);
        double yStep = 1.0 / (yDiff * 2.0 + 1.0);
        double zStep = 1.0 / (zDiff * 2.0 + 1.0);
        if (xStep > 0.0 && yStep > 0.0 && zStep > 0.0) {
            int misses = 0;
            int hits = 0;
            double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) * 0.5;
            double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) * 0.5;
            xStep *= xDiff;
            yStep *= yDiff;
            zStep *= zDiff;
            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;
            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3 position = new Vec3(x, y, z);
                        if (DamageUtils.raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) {
                            ++misses;
                        }
                        ++hits;
                    }
                }
            }
            return (float)misses / (float)hits;
        }
        return 0.0f;
    }

    public static HitResult.HitResult$Type raycast(Vec3 start, Vec3 end, boolean ignoreTerrain) {
        return (HitResult.HitResult$Type)BlockGetter.method_17744((Vec3)start, (Vec3)end, null, (innerContext, blockPos) -> {
            BlockState blockState = Wrapper.mc.level.method_8320(blockPos);
            if (blockState.method_26204().getExplosionResistance() < 600.0f && ignoreTerrain) {
                return null;
            }
            BlockHitResult hitResult = blockState.method_26220((BlockGetter)Wrapper.mc.level, blockPos).method_1092(start, end, blockPos);
            return hitResult == null ? null : hitResult.method_17783();
        }, innerContext -> HitResult.HitResult$Type.MISS);
    }

    public static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory) {
        return (BlockHitResult)BlockGetter.method_17744((Vec3)context.start(), (Vec3)context.end(), (Object)context, (BiFunction)raycastFactory, ctx -> null);
    }

    public static int getEnchantmentLevel(ItemStack itemStack, ResourceKey<Enchantment> enchantment) {
        if (itemStack.method_7960()) {
            return 0;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        DamageUtils.getEnchantments(itemStack, (Object2IntMap<Holder<Enchantment>>)itemEnchantments);
        return DamageUtils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)itemEnchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<Holder<Enchantment>> itemEnchantments, ResourceKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(itemEnchantments)) {
            if (!((Holder)entry.getKey()).method_40225(enchantment)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(DamageUtils.squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    public static GameType getGameMode(Player player) {
        if (player == null) {
            return null;
        }
        PlayerInfo playerListEntry = Wrapper.mc.getConnection().method_2871(player.method_5667());
        if (playerListEntry == null) {
            return null;
        }
        return playerListEntry.getGameMode();
    }

    public static double squaredDistanceTo(Entity entity) {
        return DamageUtils.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double squaredDistanceTo(BlockPos blockPos) {
        return DamageUtils.squaredDistanceTo(blockPos.method_10263(), blockPos.method_10264(), blockPos.getZ());
    }

    public static double squaredDistanceTo(double x, double y, double z) {
        return DamageUtils.squaredDistance(Wrapper.mc.player.getX(), Wrapper.mc.player.getY(), Wrapper.mc.player.getZ(), x, y, z);
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double f = x1 - x2;
        double g = y1 - y2;
        double h = z1 - z2;
        return org.joml.Math.fma((double)f, (double)f, (double)org.joml.Math.fma((double)g, (double)g, (double)(h * h)));
    }

    public static void getEnchantments(ItemStack itemStack, Object2IntMap<Holder<Enchantment>> enchantments) {
        enchantments.clear();
        if (!itemStack.method_7960()) {
            Set itemEnchantments = itemStack.method_7909() == Items.ENCHANTED_BOOK ? ((ItemEnchantments)itemStack.method_57824(DataComponents.STORED_ENCHANTMENTS)).method_57539() : itemStack.getEnchantments().method_57539();
            for (Object2IntMap.Entry entry : itemEnchantments) {
                enchantments.put((Object)((Holder)entry.getKey()), entry.getIntValue());
            }
        }
    }

    @FunctionalInterface
    public static interface RaycastFactory
    extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {
    }

    public record ExposureRaycastContext(Vec3 start, Vec3 end) {
    }
}

