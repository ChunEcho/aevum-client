/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Player
 *  net.minecraft.AbstractContainerMenu
 *  net.minecraft.ChestMenu
 *  net.minecraft.ClickType
 *  net.minecraft.ShulkerBoxMenu
 *  net.minecraft.BlockItem
 *  net.minecraft.ElytraItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.BlockGetter
 *  net.minecraft.Blocks
 *  net.minecraft.EnderChestBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.CompoundTag
 *  net.minecraft.ListTag
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.ResourceLocation
 *  net.minecraft.GuiGraphics
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 *  net.minecraft.HolderLookup$Provider
 *  net.minecraft.BuiltInRegistries
 *  net.minecraft.CustomData
 *  net.minecraft.DataComponentMap
 *  net.minecraft.DataComponents
 *  org.joml.Vector3f
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.Render2DEvent;
import dev.aevum.api.events.impl.Render3DEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.render.ProjectionUtils;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.asm.accessors.IContainerComponent;
import dev.aevum.core.Manager;
import dev.aevum.core.impl.BreakManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.impl.player.AutoArmor;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.InteractionHand;
import net.minecraft.Player;
import net.minecraft.AbstractContainerMenu;
import net.minecraft.ChestMenu;
import net.minecraft.ClickType;
import net.minecraft.ShulkerBoxMenu;
import net.minecraft.BlockItem;
import net.minecraft.ElytraItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.BlockGetter;
import net.minecraft.Blocks;
import net.minecraft.EnderChestBlock;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.CompoundTag;
import net.minecraft.ListTag;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundPlayerActionPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.Mth;
import net.minecraft.PoseStack;
import net.minecraft.HolderLookup;
import net.minecraft.BuiltInRegistries;
import net.minecraft.CustomData;
import net.minecraft.DataComponentMap;
import net.minecraft.DataComponents;
import org.joml.Vector3f;

public class AutoRegear
extends Module {
    public static AutoRegear INSTANCE;
    public final StringSetting kitName = this.add(new StringSetting("KitName", "default"));
    public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    private final SliderSetting rotateSpeed = this.add(new SliderSetting("RotateSpeed", 0.7, 0.1, 1.0, 0.01, () -> this.rotate.getValue()));
    private final SliderSetting rotatePriority = this.add(new SliderSetting("RotatePriority", 100.0, 1.0, 100.0, 1.0, () -> this.rotate.getValue()));
    public final EnumSetting<PlaceType> placeType = this.add(new EnumSetting<PlaceType>("PlaceType", PlaceType.Random));
    private final SliderSetting placeTryCount = this.add(new SliderSetting("TryCount", 5, 1, 20, () -> this.placeType.getValue() == PlaceType.Burrow || this.placeType.getValue() == PlaceType.Tower));
    private final SliderSetting smartX = this.add(new SliderSetting("SmartXZ", 3.0, 0.0, 10.0, 0.1, () -> this.placeType.getValue() == PlaceType.Burrow));
    private final SliderSetting smartUp = this.add(new SliderSetting("SmartUp", 3.0, 0.0, 10.0, 0.1, () -> this.placeType.getValue() == PlaceType.Burrow));
    private final SliderSetting smartDown = this.add(new SliderSetting("SmartDown", 3.0, 0.0, 10.0, 0.1, () -> this.placeType.getValue() == PlaceType.Burrow));
    private final SliderSetting smartDistance = this.add(new SliderSetting("SmartDistance", 2.0, 0.0, 10.0, 0.1, () -> this.placeType.getValue() == PlaceType.Burrow));
    private final SliderSetting regearDelay = this.add(new SliderSetting("RegearDelay", 50.0, 0.0, 1000.0, 1.0));
    private final SliderSetting perAction = this.add(new SliderSetting("PerAction", 1.0, 1.0, 10.0, 1.0));
    private final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 200.0, 0.0, 500.0, 10.0));
    private final SliderSetting enderChestDelay = this.add(new SliderSetting("EnderChestDelay", 150.0, 0.0, 300.0, 10.0));
    private final SliderSetting openDelay = this.add(new SliderSetting("OpenDelay", 200.0, 0.0, 500.0, 10.0));
    private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true));
    private final BooleanSetting autoContinue = this.add(new BooleanSetting("AutoContinue", true));
    private final BooleanSetting checkKitType = this.add(new BooleanSetting("SmartSearch", true));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting disableTime = this.add(new SliderSetting("DisableTime", 500, 0, 1000));
    private final BooleanSetting place = this.add(new BooleanSetting("Place", true));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting open = this.add(new BooleanSetting("Open", true));
    private final SliderSetting range = this.add(new SliderSetting("MaxRange", 4.0, 0.0, 6.0, 0.1));
    private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
    private final BooleanSetting take = this.add(new BooleanSetting("Take", true));
    private final BooleanSetting regearArmor = this.add(new BooleanSetting("RegearArmor", true));
    private final BooleanSetting returnExtras = this.add(new BooleanSetting("ReturnExtras", true));
    private final BindSetting placeKey = this.add(new BindSetting("PlaceKey", -1));
    public final BooleanSetting silentDisplay = this.add(new BooleanSetting("SilentDisplay", true));
    private final SliderSetting height = this.add(new SliderSetting("Height", 1.5, 0.0, 3.0, 0.1, () -> this.silentDisplay.getValue()));
    private final SliderSetting scale = this.add(new SliderSetting("Scale", 1.0, 0.5, 2.0, 0.1, () -> this.silentDisplay.getValue()));
    private final SliderSetting armorCoin = this.add(new SliderSetting("ArmorCoin", 1000.0, 1.0, 5000.0, 1.0, () -> this.checkKitType.getValue()));
    private final SliderSetting weaponCoin = this.add(new SliderSetting("WeaponCoin", 1000.0, 1.0, 5000.0, 1.0, () -> this.checkKitType.getValue()));
    private final SliderSetting regearItemCoin = this.add(new SliderSetting("RegearItemCoin", 1.0, 1.0, 5000.0, 1.0, () -> this.checkKitType.getValue()));
    private final BooleanSetting xCarryRegear = this.add(new BooleanSetting("XCarryRegear", false));
    private final Timer timer = new Timer();
    public final Timer timeoutTimer = new Timer();
    private final Timer openTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private final Timer xCarryMoveTimer = new Timer();
    public BlockPos placePos = null;
    private BlockPos openPos;
    private boolean opend = false;
    private boolean on = false;
    private boolean finishedRegear = false;
    private boolean retrievingFromEnderChest = false;
    private boolean armorPhaseComplete = false;
    private boolean xCarryPhaseComplete = false;
    private int currentTryCount = 0;
    private final Map<Integer, Item> targetKit = new HashMap<Integer, Item>();
    private Vector3f currentScreenPos = null;
    private final List<Pulse> pulses = new ArrayList<Pulse>();
    private final List<Runnable> armorActionQueue = new ArrayList<Runnable>();
    private int armorActionIndex = 0;
    public boolean silentScreenActive = false;

    public AutoRegear() {
        super("AutoRegear", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u8865\u7ed9");
        INSTANCE = this;
    }

    private boolean isElytraFlyMode() {
        return ElytraFly.INSTANCE != null && ElytraFly.INSTANCE.isOn() && AutoArmor.INSTANCE != null && AutoArmor.INSTANCE.isOn() && AutoArmor.INSTANCE.autoElytra.getValue();
    }

    private Map<Integer, Item> getEffectiveKit() {
        HashMap<Integer, Item> effective = new HashMap<Integer, Item>(this.targetKit);
        if (!this.isElytraFlyMode()) {
            return effective;
        }
        Item chestItem = effective.getOrDefault(38, Items.AIR);
        if (chestItem instanceof ElytraItem || chestItem == Items.ELYTRA) {
            return effective;
        }
        int elytraSlot = -1;
        for (Map.Entry<Integer, Item> entry : effective.entrySet()) {
            if (entry.getKey() >= 36 || entry.getValue() != Items.ELYTRA) continue;
            elytraSlot = entry.getKey();
            break;
        }
        if (elytraSlot != -1) {
            effective.put(38, Items.ELYTRA);
            effective.put(elytraSlot, chestItem);
        } else if (chestItem != Items.AIR) {
            effective.put(38, Items.ELYTRA);
            for (int i = 0; i < 36; ++i) {
                if (effective.containsKey(i)) continue;
                effective.put(i, chestItem);
                break;
            }
        }
        return effective;
    }

    private boolean isArmorItem(Item item) {
        return item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS || item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS || item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS || item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS || item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS || item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS || item == Items.TURTLE_HELMET || item == Items.ELYTRA;
    }

    private boolean isWeaponItem(Item item) {
        return item == Items.NETHERITE_SWORD || item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD || item == Items.STONE_SWORD || item == Items.WOODEN_SWORD || item == Items.NETHERITE_PICKAXE || item == Items.DIAMOND_PICKAXE || item == Items.IRON_PICKAXE || item == Items.GOLDEN_PICKAXE || item == Items.STONE_PICKAXE || item == Items.WOODEN_PICKAXE;
    }

    private int getItemCoin(Item item) {
        if (this.isArmorItem(item)) {
            return this.armorCoin.getValueInt();
        }
        if (this.isWeaponItem(item)) {
            return this.weaponCoin.getValueInt();
        }
        return this.regearItemCoin.getValueInt();
    }

    private boolean itemsMatch(Item item1, Item item2) {
        if (item1 == item2) {
            return true;
        }
        boolean p1 = item1 == Items.PISTON || item1 == Items.STICKY_PISTON;
        boolean p2 = item2 == Items.PISTON || item2 == Items.STICKY_PISTON;
        return p1 && p2;
    }

    private boolean strictMatch(Item item1, Item item2) {
        return item1 == item2;
    }

    public boolean hasActiveShulkerScreen() {
        if (AutoRegear.mc.player == null) {
            return false;
        }
        return AutoRegear.mc.player.containerMenu instanceof ShulkerBoxMenu;
    }

    public boolean isWorking() {
        return this.isOn() && (this.hasActiveShulkerScreen() || this.silentScreenActive || this.retrievingFromEnderChest || this.opend || this.on || !this.timer.passed(2000L) || !this.openTimer.passed(2000L));
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.rotate.getValue()) {
            BlockPos targetPos = null;
            if (this.openPos != null) {
                targetPos = this.openPos;
            } else if (this.placePos != null) {
                targetPos = this.placePos;
            }
            if (targetPos != null && this.isWorking()) {
                event.setTarget(targetPos.method_46558(), this.rotateSpeed.getValueFloat(), this.rotatePriority.getValueFloat());
            }
        }
    }

    private void silentCloseScreen() {
        if (AutoRegear.mc.player != null) {
            AutoRegear.mc.player.method_7346();
        }
        this.silentScreenActive = false;
    }

    private BlockPos findNearbyShulkerBlock() {
        for (BlockPos pos : BlockUtil.getSphere((float)this.range.getValue())) {
            if (!(AutoRegear.mc.level.method_8320(pos).method_26204() instanceof ShulkerBoxBlock) || !AutoRegear.mc.level.isEmptyBlock(pos.method_10084()) && !BlockUtil.canReplace(pos.method_10084())) continue;
            return pos;
        }
        return null;
    }

    private BlockPos findNearbyEnderChestBlock() {
        for (BlockPos pos : BlockUtil.getSphere((float)this.range.getValue())) {
            if (AutoRegear.mc.level.method_8320(pos).method_26204() != Blocks.ENDER_CHEST || !AutoRegear.mc.level.isEmptyBlock(pos.method_10084()) && !BlockUtil.canReplace(pos.method_10084())) continue;
            return pos;
        }
        return null;
    }

    @Override
    public void onEnable() {
        this.opend = false;
        this.openPos = null;
        this.timeoutTimer.reset();
        this.timer.reset();
        this.openTimer.reset();
        this.actionTimer.reset();
        this.xCarryMoveTimer.reset();
        this.placePos = null;
        this.finishedRegear = false;
        this.retrievingFromEnderChest = false;
        this.armorPhaseComplete = false;
        this.xCarryPhaseComplete = false;
        this.armorActionQueue.clear();
        this.armorActionIndex = 0;
        this.pulses.clear();
        this.currentScreenPos = null;
        this.silentScreenActive = false;
        this.currentTryCount = 0;
        if (!this.loadKit(this.kitName.getValue()) && this.take.getValue()) {
            this.disable();
            return;
        }
        if (this.xCarryRegear.getValue() && this.hasShulkerInCraftSlots()) {
            return;
        }
        if (!AutoRegear.nullCheck()) {
            BlockPos nearbyShulker = this.findNearbyShulkerBlock();
            if (nearbyShulker != null) {
                this.placePos = nearbyShulker;
            } else if (this.place.getValue()) {
                this.doPlace();
            }
        }
    }

    @Override
    public void onDisable() {
        if (this.mine.getValue()) {
            BlockPos targetPos = this.openPos != null ? this.openPos : this.placePos;
            BlockPos class_23382 = targetPos;
            if (targetPos != null && AutoRegear.mc.level.method_8320(targetPos).method_26204() instanceof ShulkerBoxBlock) {
                if (PacketMine.INSTANCE != null && !PacketMine.INSTANCE.isOn()) {
                    PacketMine.INSTANCE.enable();
                }
                if (PacketMine.INSTANCE != null) {
                    PacketMine.INSTANCE.mine(targetPos);
                }
            }
        }
        this.opend = false;
        this.openPos = null;
        this.retrievingFromEnderChest = false;
        this.armorPhaseComplete = false;
        this.xCarryPhaseComplete = false;
        this.armorActionQueue.clear();
        this.armorActionIndex = 0;
        this.pulses.clear();
        this.currentScreenPos = null;
        this.currentTryCount = 0;
        if (this.silentScreenActive && AutoRegear.mc.player != null) {
            AutoRegear.mc.player.method_7346();
        }
        this.silentScreenActive = false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.placeKey.isPressed() && AutoRegear.mc.screen == null && !this.silentScreenActive) {
            if (!this.on) {
                this.opend = false;
                this.openPos = null;
                this.timeoutTimer.reset();
                this.timer.reset();
                this.openTimer.reset();
                this.actionTimer.reset();
                this.xCarryMoveTimer.reset();
                this.retrievingFromEnderChest = false;
                this.armorPhaseComplete = false;
                this.xCarryPhaseComplete = false;
                this.armorActionQueue.clear();
                this.armorActionIndex = 0;
                this.currentTryCount = 0;
                this.doPlace();
            }
            this.on = true;
        } else {
            this.on = false;
        }
        if (this.xCarryRegear.getValue() && !this.xCarryPhaseComplete) {
            if (this.tickXCarryMove()) {
                return;
            }
            this.xCarryPhaseComplete = true;
            if (!AutoRegear.nullCheck()) {
                BlockPos nearbyShulker = this.findNearbyShulkerBlock();
                if (nearbyShulker != null) {
                    this.placePos = nearbyShulker;
                } else if (this.place.getValue()) {
                    this.doPlace();
                }
            }
        }
        if (this.retrievingFromEnderChest) {
            AbstractContainerMenu class_17032 = AutoRegear.mc.player.containerMenu;
            if (class_17032 instanceof ChestMenu) {
                ChestMenu container = (ChestMenu)class_17032;
                if (this.findBestShulker() != -1) {
                    AutoRegear.mc.player.method_7346();
                    this.retrievingFromEnderChest = false;
                    this.openPos = null;
                    this.timer.reset();
                    if (!this.autoContinue.getValue()) {
                        this.disable();
                    }
                    return;
                }
                if (this.timer.passed(this.enderChestDelay.getValueInt())) {
                    int bestSlot = -1;
                    if (this.checkKitType.getValue()) {
                        Map<Integer, Item> effectiveKit = this.getEffectiveKit();
                        List<Item> missingItems = this.getMissingItems(effectiveKit);
                        int maxScore = -1;
                        for (int i = 0; i < container.getContainer().method_5439(); ++i) {
                            int score;
                            ItemStack stack = container.method_7611(i).getItem();
                            if (!this.isShulkerBox(stack) || (score = this.getShulkerScore(stack, missingItems)) <= maxScore) continue;
                            maxScore = score;
                            bestSlot = i;
                        }
                    } else {
                        for (int i = 0; i < container.getContainer().method_5439(); ++i) {
                            if (!this.isShulkerBox(container.method_7611(i).getItem())) continue;
                            bestSlot = i;
                            break;
                        }
                    }
                    if (bestSlot != -1) {
                        boolean hasSpace = false;
                        for (int i = 0; i < 36; ++i) {
                            if (!AutoRegear.mc.player.getInventory().method_5438(i).method_7960()) continue;
                            hasSpace = true;
                            break;
                        }
                        if (!hasSpace) {
                            AutoRegear.mc.player.method_7346();
                            this.openPos = null;
                            this.disable();
                            return;
                        }
                        AutoRegear.mc.gameMode.method_2906(container.containerId, bestSlot, 0, ClickType.QUICK_MOVE, (Player)AutoRegear.mc.player);
                        this.timer.reset();
                    } else {
                        AutoRegear.mc.player.method_7346();
                        this.openPos = null;
                        this.disable();
                    }
                }
                return;
            }
            this.retrievingFromEnderChest = false;
            this.openPos = null;
        }
        if (this.hasActiveShulkerScreen()) {
            ShulkerBoxMenu shulker = (ShulkerBoxMenu)AutoRegear.mc.player.containerMenu;
            this.opend = true;
            this.openPos = this.openPos == null ? this.placePos : this.openPos;
            BlockPos class_23382 = this.openPos;
            if (this.silentDisplay.getValue()) {
                this.silentScreenActive = true;
            }
            if (this.take.getValue()) {
                Map<Integer, Item> effectiveKit = this.getEffectiveKit();
                if (!this.actionTimer.passed((long)this.regearDelay.getValue())) {
                    return;
                }
                if (this.regearArmor.getValue() && !this.armorPhaseComplete) {
                    this.processArmorPhase(shulker, effectiveKit);
                    return;
                }
                boolean totalActed = false;
                for (int actionsThisCycle = 0; actionsThisCycle < this.perAction.getValueInt(); ++actionsThisCycle) {
                    boolean actedThisStep = false;
                    if (this.returnExtras.getValue() && !actedThisStep) {
                        actedThisStep = this.returnExtraItems(shulker, effectiveKit);
                    }
                    if (!actedThisStep) {
                        actedThisStep = this.sortInventory(shulker, effectiveKit);
                    }
                    if (!actedThisStep) {
                        actedThisStep = this.takeNeededItems(shulker, effectiveKit);
                    }
                    if (!actedThisStep) break;
                    totalActed = true;
                }
                if (totalActed) {
                    this.timeoutTimer.reset();
                    this.actionTimer.reset();
                } else if (this.autoDisable.getValue() && this.timeoutTimer.passed(this.disableTime.getValueInt())) {
                    this.finishedRegear = true;
                    if (this.silentScreenActive) {
                        this.silentCloseScreen();
                    } else {
                        AutoRegear.mc.player.method_7346();
                    }
                    this.disable();
                }
            }
        } else if (this.opend) {
            this.opend = false;
            this.silentScreenActive = false;
            this.armorPhaseComplete = false;
            this.armorActionQueue.clear();
            this.armorActionIndex = 0;
            if (this.mine.getValue() && this.openPos != null && AutoRegear.mc.level.method_8320(this.openPos).method_26204() instanceof ShulkerBoxBlock && PacketMine.INSTANCE != null) {
                PacketMine.INSTANCE.mine(this.openPos);
            }
            if (this.autoDisable.getValue()) {
                this.disable();
            }
        } else {
            boolean foundExisting = false;
            if (this.open.getValue() && !this.retrievingFromEnderChest && this.openTimer.passed(this.openDelay.getValueInt()) && (foundExisting = this.processOpening())) {
                this.openTimer.reset();
            }
            if (!foundExisting && this.place.getValue()) {
                this.doPlace();
            }
        }
    }

    private boolean hasShulkerInCraftSlots() {
        if (AutoRegear.mc.player == null) {
            return false;
        }
        for (int craftSlot = 1; craftSlot <= 4; ++craftSlot) {
            ItemStack stack = AutoRegear.mc.player.inventoryMenu.method_7611(craftSlot).getItem();
            if (!this.isShulkerBox(stack)) continue;
            return true;
        }
        return false;
    }

    private boolean tickXCarryMove() {
        if (AutoRegear.mc.player == null) {
            return false;
        }
        if (!this.xCarryMoveTimer.passed((long)this.regearDelay.getValue())) {
            return true;
        }
        for (int craftSlot = 1; craftSlot <= 4; ++craftSlot) {
            ItemStack craftStack = AutoRegear.mc.player.inventoryMenu.method_7611(craftSlot).getItem();
            if (!this.isShulkerBox(craftStack)) continue;
            int emptySlot = this.findEmptyInventorySlot();
            if (emptySlot == -1) {
                this.xCarryPhaseComplete = true;
                return false;
            }
            int syncId = AutoRegear.mc.player.inventoryMenu.containerId;
            AutoRegear.mc.gameMode.method_2906(syncId, craftSlot, 0, ClickType.PICKUP, (Player)AutoRegear.mc.player);
            AutoRegear.mc.gameMode.method_2906(syncId, emptySlot, 0, ClickType.PICKUP, (Player)AutoRegear.mc.player);
            if (!AutoRegear.mc.player.inventoryMenu.getCarried().method_7960()) {
                AutoRegear.mc.gameMode.method_2906(syncId, craftSlot, 0, ClickType.PICKUP, (Player)AutoRegear.mc.player);
            }
            this.xCarryMoveTimer.reset();
            return true;
        }
        return false;
    }

    private int findEmptyInventorySlot() {
        if (AutoRegear.mc.player == null) {
            return -1;
        }
        for (int slot = 9; slot <= 44; ++slot) {
            if (!AutoRegear.mc.player.inventoryMenu.method_7611(slot).getItem().method_7960()) continue;
            return slot;
        }
        return -1;
    }

    private void processArmorPhase(ShulkerBoxMenu shulker, Map<Integer, Item> kit) {
        if (this.armorActionQueue.isEmpty() && this.armorActionIndex == 0) {
            this.buildArmorActionQueue(shulker, kit);
            if (this.armorActionQueue.isEmpty()) {
                this.armorPhaseComplete = true;
                return;
            }
        }
        if (this.armorActionIndex >= this.armorActionQueue.size()) {
            this.armorPhaseComplete = true;
            this.armorActionQueue.clear();
            this.armorActionIndex = 0;
            this.timeoutTimer.reset();
            return;
        }
        for (int actionsThisCycle = 0; actionsThisCycle < this.perAction.getValueInt() && this.armorActionIndex < this.armorActionQueue.size(); ++actionsThisCycle) {
            this.armorActionQueue.get(this.armorActionIndex).run();
            ++this.armorActionIndex;
        }
        this.actionTimer.reset();
        this.timeoutTimer.reset();
    }

    private boolean isPositionBeingMined(BlockPos pos) {
        if (PacketMine.INSTANCE != null && PacketMine.INSTANCE.isOn() && pos.equals((Object)PacketMine.getBreakPos())) {
            return true;
        }
        if (Aevum.BREAK != null) {
            for (BreakManager.BreakData data : Aevum.BREAK.breakMap.values()) {
                if (data == null || !pos.equals((Object)data.pos)) continue;
                return true;
            }
            for (BreakManager.BreakData data : Aevum.BREAK.doubleMap.values()) {
                if (data == null || !pos.equals((Object)data.pos)) continue;
                return true;
            }
        }
        return false;
    }

    private void buildArmorActionQueue(ShulkerBoxMenu shulker, Map<Integer, Item> kit) {
        this.armorActionQueue.clear();
        for (int i = 36; i < 40; ++i) {
            ItemStack current;
            Item desired;
            if (!kit.containsKey(i) || (desired = kit.get(i)) == Items.AIR || (current = AutoRegear.mc.player.getInventory().method_5438(i)).method_7909() == desired || i == 38 && desired == Items.ELYTRA && current.method_7909() == Items.ELYTRA && ElytraItem.isFlyEnabled((ItemStack)current)) continue;
            int slotId = -1;
            for (int j = 0; j < 27; ++j) {
                if (shulker.method_7611(j).getItem().method_7909() != desired) continue;
                slotId = j;
                break;
            }
            if (slotId == -1) continue;
            int finalSlotId = slotId;
            int armorSlot = i;
            int hotbar = AutoRegear.mc.player.getInventory().selected;
            this.armorActionQueue.add(() -> this.windowClick(shulker.containerId, finalSlotId, hotbar, ClickType.SWAP));
            this.armorActionQueue.add(() -> {
                if (armorSlot == 40) {
                    mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                } else {
                    mc.getConnection().method_52787((Packet)new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                }
            });
            this.armorActionQueue.add(() -> this.windowClick(shulker.containerId, finalSlotId, hotbar, ClickType.SWAP));
        }
    }

    private List<Item> getMissingItems(Map<Integer, Item> kit) {
        ArrayList<Item> missing = new ArrayList<Item>();
        for (Map.Entry<Integer, Item> entry : kit.entrySet()) {
            int slot = entry.getKey();
            Item desired = entry.getValue();
            if (desired == Items.AIR || slot < 0 || slot >= AutoRegear.mc.player.getInventory().method_5439()) continue;
            ItemStack currentStack = AutoRegear.mc.player.getInventory().method_5438(slot);
            boolean isMissing = false;
            if (!this.itemsMatch(currentStack.method_7909(), desired)) {
                isMissing = true;
            } else if (currentStack.isStackable() && currentStack.getCount() < currentStack.getMaxStackSize()) {
                isMissing = true;
            }
            if (!isMissing) continue;
            boolean alreadyListed = false;
            for (Item m : missing) {
                if (!this.itemsMatch(m, desired)) continue;
                alreadyListed = true;
                break;
            }
            if (alreadyListed) continue;
            missing.add(desired);
        }
        return missing;
    }

    private int getShulkerScore(ItemStack shulkerStack, List<Item> missingItems) {
        int score = 0;
        List<ItemStack> contents = this.getShulkerContents(shulkerStack);
        for (ItemStack itemInBox : contents) {
            if (itemInBox.method_7960()) continue;
            Item item = itemInBox.method_7909();
            boolean isNeeded = false;
            for (Item missing : missingItems) {
                if (!this.itemsMatch(missing, item)) continue;
                isNeeded = true;
                break;
            }
            if (!isNeeded) continue;
            score += this.getItemCoin(item);
        }
        return score;
    }

    public int findBestShulker() {
        if (!this.checkKitType.getValue()) {
            for (int i = 0; i < 36; ++i) {
                ItemStack stack = AutoRegear.mc.player.getInventory().method_5438(i);
                if (!this.isShulkerBox(stack)) continue;
                return i < 9 ? i + 36 : i;
            }
            return -1;
        }
        Map<Integer, Item> effectiveKit = this.getEffectiveKit();
        List<Item> missingItems = this.getMissingItems(effectiveKit);
        int bestSlot = -1;
        int maxScore = -1;
        for (int i = 0; i < 36; ++i) {
            int score;
            ItemStack stack = AutoRegear.mc.player.getInventory().method_5438(i);
            if (!this.isShulkerBox(stack) || (score = this.getShulkerScore(stack, missingItems)) <= maxScore) continue;
            maxScore = score;
            bestSlot = i < 9 ? i + 36 : i;
        }
        return bestSlot;
    }

    public int findEnderChestItem() {
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = AutoRegear.mc.player.getInventory().method_5438(i);
            if (stack.method_7909() != Items.ENDER_CHEST) continue;
            return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    private boolean isShulkerBox(ItemStack stack) {
        BlockItem blockItem;
        Item class_17922;
        return !stack.method_7960() && (class_17922 = stack.method_7909()) instanceof BlockItem && (blockItem = (BlockItem)class_17922).getBlock() instanceof ShulkerBoxBlock;
    }

    private List<ItemStack> getShulkerContents(ItemStack itemStack) {
        CustomData nbt2;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        DataComponentMap components = itemStack.method_57353();
        if (components.method_57832(DataComponents.CONTAINER)) {
            IContainerComponent container = (IContainerComponent)components.method_57829(DataComponents.CONTAINER);
            items.addAll((Collection<ItemStack>)container.getStacks());
        } else if (components.method_57832(DataComponents.BLOCK_ENTITY_DATA) && (nbt2 = (CustomData)components.method_57829(DataComponents.BLOCK_ENTITY_DATA)).method_57450("Items")) {
            ListTag nbt3 = (ListTag)nbt2.method_57463().method_10580("Items");
            for (int ix = 0; ix < nbt3.size(); ++ix) {
                items.add(ItemStack.method_57359((HolderLookup.HolderLookup$Provider)AutoRegear.mc.player.registryAccess(), (CompoundTag)nbt3.method_10602(ix)));
            }
        }
        return items;
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        this.currentScreenPos = null;
        if (!this.silentDisplay.getValue() || !this.opend || AutoRegear.mc.player == null) {
            return;
        }
        if (!this.hasActiveShulkerScreen()) {
            return;
        }
        BlockPos targetPos = this.openPos != null ? this.openPos : this.placePos;
        BlockPos class_23382 = targetPos;
        if (targetPos == null) {
            return;
        }
        Vec3 worldCenter = targetPos.method_46558().method_1031(0.0, this.height.getValue(), 0.0);
        this.currentScreenPos = ProjectionUtils.project(worldCenter);
    }

    @EventListener
    public void onRender2D(Render2DEvent event) {
        if (this.currentScreenPos == null || !this.silentDisplay.getValue() || !this.opend) {
            return;
        }
        if (!this.hasActiveShulkerScreen()) {
            return;
        }
        ShulkerBoxMenu handler = (ShulkerBoxMenu)AutoRegear.mc.player.containerMenu;
        GuiGraphics context = event.drawContext;
        PoseStack matrices = context.method_51448();
        float screenX = this.currentScreenPos.x;
        float screenY = this.currentScreenPos.y;
        if (screenX < -200.0f || screenX > (float)(mc.getWindow().getGuiScaledWidth() + 200) || screenY < -200.0f || screenY > (float)(mc.getWindow().getGuiScaledHeight() + 200)) {
            return;
        }
        int gridWidth = 162;
        int gridHeight = 54;
        int padding = 6;
        float totalWidth = gridWidth + padding * 2;
        float totalHeight = gridHeight + padding * 2;
        float currentScale = (float)this.scale.getValue();
        matrices.method_22903();
        matrices.method_46416(screenX, screenY, 0.0f);
        matrices.method_22905(currentScale, currentScale, 1.0f);
        int startX = (int)(-totalWidth / 2.0f);
        int startY = (int)(-totalHeight / 2.0f);
        int endX = (int)((float)startX + totalWidth);
        int endY = (int)((float)startY + totalHeight);
        int bgColor = new Color(20, 20, 20, 160).getRGB();
        int borderColor = new Color(100, 100, 100, 200).getRGB();
        context.method_25294(startX, startY, endX, endY, bgColor);
        context.method_49601(startX, startY, (int)totalWidth, (int)totalHeight, borderColor);
        for (int i = 0; i < 27; ++i) {
            ItemStack stack = handler.method_7611(i).getItem();
            int row = i / 9;
            int col = i % 9;
            int slotX = startX + padding + col * 18;
            int slotY = startY + padding + row * 18;
            if (!stack.method_7960()) {
                context.method_51427(stack, slotX, slotY);
                context.method_51431(AutoRegear.mc.font, stack, slotX, slotY);
            }
            this.drawPulse2D(matrices, slotX + 9, slotY + 9, i);
        }
        matrices.method_22909();
        this.pulses.removeIf(p -> p.timer.getMs() > p.maxTime);
    }

    private void drawPulse2D(PoseStack ms, float centerX, float centerY, int slotId) {
        for (Pulse p : this.pulses) {
            if (p.slotId != slotId) continue;
            float progress = Math.min(1.0f, (float)p.timer.getMs() / (float)p.maxTime);
            float radius = progress * 10.0f;
            int maxAlpha = 80;
            int alpha = (int)((1.0f - progress) * (float)maxAlpha);
            if (alpha <= 0) continue;
            Color pulseColor = new Color(255, 255, 255, alpha);
            Render2DUtil.drawCircle(ms, centerX, centerY, radius, pulseColor, 12);
        }
    }

    private void windowClick(int syncId, int slotId, int button, ClickType type) {
        AutoRegear.mc.gameMode.method_2906(syncId, slotId, button, type, (Player)AutoRegear.mc.player);
        if (slotId >= 0 && slotId < 27) {
            this.pulses.add(new Pulse(slotId));
        }
    }

    private boolean loadKit(String name) {
        this.targetKit.clear();
        try {
            String line;
            File file = Manager.getFile(name + ".kit");
            if (!file.exists()) {
                return false;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(file), StandardCharsets.UTF_8));
            while ((line = reader.readLine()) != null) {
                int slot;
                String[] parts = line.split(":");
                if (parts.length < 3 || (slot = Integer.parseInt(parts[0])) == 40) continue;
                String idStr = parts[1] + ":" + parts[2];
                this.targetKit.put(slot, (Item)BuiltInRegistries.ITEM.method_10223(ResourceLocation.method_60654((String)idStr)));
            }
            reader.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean returnExtraItems(ShulkerBoxMenu shulker, Map<Integer, Item> kit) {
        for (int playerSlot = 0; playerSlot < 36; ++playerSlot) {
            int emptyShulkerSlot;
            BlockItem blockItem;
            Item desiredItem;
            Item currentItem;
            int windowSlot = playerSlot < 9 ? 54 + playerSlot : 27 + (playerSlot - 9);
            ItemStack currentStack = shulker.method_7611(windowSlot).getItem();
            if (currentStack.method_7960() || this.itemsMatch(currentItem = currentStack.method_7909(), desiredItem = kit.getOrDefault(playerSlot, Items.AIR)) || this.isItemNeededElsewhere(currentItem, playerSlot, kit) || currentItem instanceof BlockItem && (blockItem = (BlockItem)currentItem).getBlock() instanceof ShulkerBoxBlock || (emptyShulkerSlot = this.findEmptyShulkerSlot(shulker)) == -1) continue;
            this.windowClick(shulker.containerId, windowSlot, 0, ClickType.QUICK_MOVE);
            return true;
        }
        return false;
    }

    private boolean sortInventory(ShulkerBoxMenu shulker, Map<Integer, Item> kit) {
        for (int playerSlot = 0; playerSlot < 36; ++playerSlot) {
            int sourcePlayerSlot;
            int windowSlot;
            ItemStack currentStack;
            Item desiredItem = kit.getOrDefault(playerSlot, Items.AIR);
            if (desiredItem == Items.AIR || this.itemsMatch((currentStack = shulker.method_7611(windowSlot = playerSlot < 9 ? 54 + playerSlot : 27 + (playerSlot - 9)).getItem()).method_7909(), desiredItem) || (sourcePlayerSlot = this.findItemInInventory(shulker, desiredItem, playerSlot, kit)) == -1) continue;
            int sourceWindowSlot = sourcePlayerSlot < 9 ? 54 + sourcePlayerSlot : 27 + (sourcePlayerSlot - 9);
            this.windowClick(shulker.containerId, windowSlot, 0, ClickType.PICKUP);
            this.windowClick(shulker.containerId, sourceWindowSlot, 0, ClickType.PICKUP);
            this.windowClick(shulker.containerId, windowSlot, 0, ClickType.PICKUP);
            return true;
        }
        return false;
    }

    private boolean takeNeededItems(ShulkerBoxMenu shulker, Map<Integer, Item> kit) {
        for (Map.Entry<Integer, Item> entry : kit.entrySet()) {
            int shulkerSlot;
            Item desired;
            int playerSlot = entry.getKey();
            if (playerSlot >= 36 || (desired = entry.getValue()) == Items.AIR) continue;
            int windowSlot = playerSlot < 9 ? 54 + playerSlot : 27 + (playerSlot - 9);
            ItemStack currentStack = shulker.method_7611(windowSlot).getItem();
            if (!currentStack.method_7960()) {
                if (!this.itemsMatch(currentStack.method_7909(), desired) || currentStack.getCount() >= currentStack.getMaxStackSize() || (shulkerSlot = this.findStrictMatchingItemInShulker(shulker, currentStack)) == -1) continue;
                this.windowClick(shulker.containerId, shulkerSlot, 0, ClickType.PICKUP);
                this.windowClick(shulker.containerId, windowSlot, 0, ClickType.PICKUP);
                if (!shulker.getCarried().method_7960()) {
                    this.windowClick(shulker.containerId, shulkerSlot, 0, ClickType.PICKUP);
                }
                return true;
            }
            shulkerSlot = this.findBestItemInShulker(shulker, desired);
            if (shulkerSlot == -1) continue;
            this.windowClick(shulker.containerId, shulkerSlot, 0, ClickType.PICKUP);
            this.windowClick(shulker.containerId, windowSlot, 0, ClickType.PICKUP);
            if (!shulker.getCarried().method_7960()) {
                this.windowClick(shulker.containerId, shulkerSlot, 0, ClickType.PICKUP);
            }
            return true;
        }
        return false;
    }

    private boolean isItemNeededElsewhere(Item item, int currentSlot, Map<Integer, Item> kit) {
        for (Map.Entry<Integer, Item> entry : kit.entrySet()) {
            ShulkerBoxMenu shulker;
            ItemStack stackInSlot;
            Item needed;
            int slotIndex = entry.getKey();
            if (slotIndex >= 36 || slotIndex == currentSlot || !this.itemsMatch(needed = entry.getValue(), item)) continue;
            int windowSlot = slotIndex < 9 ? 54 + slotIndex : 27 + (slotIndex - 9);
            int n = windowSlot;
            AbstractContainerMenu class_17032 = AutoRegear.mc.player.containerMenu;
            if (!(class_17032 instanceof ShulkerBoxMenu) || this.itemsMatch((stackInSlot = (shulker = (ShulkerBoxMenu)class_17032).method_7611(windowSlot).getItem()).method_7909(), needed)) continue;
            return true;
        }
        return false;
    }

    private int findItemInInventory(ShulkerBoxMenu shulker, Item item, int ignoreSlot, Map<Integer, Item> kit) {
        for (int i = 0; i < 36; ++i) {
            Item neededAtSource;
            int windowSlot;
            ItemStack stack;
            if (i == ignoreSlot || !this.itemsMatch((stack = shulker.method_7611(windowSlot = i < 9 ? 54 + i : 27 + (i - 9)).getItem()).method_7909(), item) || this.itemsMatch(neededAtSource = kit.getOrDefault(i, Items.AIR), item)) continue;
            return i;
        }
        return -1;
    }

    private int findBestItemInShulker(ShulkerBoxMenu shulker, Item desired) {
        int bestSlot = -1;
        int bestCount = -1;
        for (int i = 0; i < 27; ++i) {
            ItemStack stack = shulker.method_7611(i).getItem();
            if (!this.itemsMatch(stack.method_7909(), desired) || stack.getCount() <= bestCount) continue;
            bestCount = stack.getCount();
            bestSlot = i;
        }
        return bestSlot;
    }

    private int findStrictMatchingItemInShulker(ShulkerBoxMenu shulker, ItemStack targetStack) {
        for (int i = 0; i < 27; ++i) {
            ItemStack shulkerStack = shulker.method_7611(i).getItem();
            if (!this.strictMatch(shulkerStack.method_7909(), targetStack.method_7909()) || !ItemStack.method_31577((ItemStack)shulkerStack, (ItemStack)targetStack)) continue;
            return i;
        }
        return -1;
    }

    private int findEmptyShulkerSlot(ShulkerBoxMenu shulker) {
        for (int i = 0; i < 27; ++i) {
            if (!shulker.method_7611(i).getItem().method_7960()) continue;
            return i;
        }
        return -1;
    }

    private boolean canMove(BlockPos pos) {
        return AutoRegear.mc.level.isEmptyBlock(pos) && AutoRegear.mc.level.isEmptyBlock(pos.method_10084());
    }

    private boolean processOpening() {
        if (this.placePos != null) {
            boolean isShulker = AutoRegear.mc.level.method_8320(this.placePos).method_26204() instanceof ShulkerBoxBlock;
            boolean isEChest = AutoRegear.mc.level.method_8320(this.placePos).method_26204() == Blocks.ENDER_CHEST;
            boolean bl = isEChest;
            if ((isShulker || isEChest && this.enderChest.getValue() && this.retrievingFromEnderChest) && (!(AutoRegear.mc.level.method_8320(this.placePos).method_26204() instanceof EnderChestBlock) || this.retrievingFromEnderChest) && (AutoRegear.mc.level.isEmptyBlock(this.placePos.method_10084()) || BlockUtil.canReplace(this.placePos.method_10084()))) {
                this.openPos = this.placePos;
                BlockUtil.clickBlock(this.placePos, BlockUtil.getClickSide(this.placePos), false);
                return true;
            }
        }
        for (BlockPos pos : BlockUtil.getSphere((float)this.range.getValue())) {
            if (!(AutoRegear.mc.level.method_8320(pos).method_26204() instanceof ShulkerBoxBlock) || !AutoRegear.mc.level.isEmptyBlock(pos.method_10084()) && !BlockUtil.canReplace(pos.method_10084())) continue;
            this.openPos = pos;
            this.placePos = pos;
            BlockUtil.clickBlock(pos, BlockUtil.getClickSide(pos), false);
            return true;
        }
        return false;
    }

    private BlockPos findRandomPlacePos() {
        BlockPos bestPos = null;
        double bestScore = -1000.0;
        for (BlockPos pos : BlockUtil.getSphere((float)this.range.getValue())) {
            double dist;
            if (!AutoRegear.mc.level.method_8320(pos).method_45474() || !AutoRegear.mc.level.method_8320(pos.method_10074()).method_26212((BlockGetter)AutoRegear.mc.level, pos.method_10074()) || !AutoRegear.mc.level.isEmptyBlock(pos.method_10084()) && !BlockUtil.canReplace(pos.method_10084()) || !AutoRegear.mc.level.getEntities(null, new AABB(pos)).isEmpty() || (dist = (double)Mth.method_15355((float)((float)AutoRegear.mc.player.method_5707(pos.method_46558())))) < 1.0 || this.isPositionBeingMined(pos)) continue;
            int surroundCount = 0;
            if (!AutoRegear.mc.level.method_8320(pos.method_10095()).method_45474()) {
                ++surroundCount;
            }
            if (!AutoRegear.mc.level.method_8320(pos.method_10072()).method_45474()) {
                ++surroundCount;
            }
            if (!AutoRegear.mc.level.method_8320(pos.east()).method_45474()) {
                ++surroundCount;
            }
            if (!AutoRegear.mc.level.method_8320(pos.method_10067()).method_45474()) {
                ++surroundCount;
            }
            double score = (double)surroundCount * 0.5 - dist;
            if (bestPos != null && !(score > bestScore)) continue;
            bestScore = score;
            bestPos = pos;
        }
        return bestPos;
    }

    private void executePlacement(int targetSlot, int oldSlot, BlockPos targetPos) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(targetSlot, oldSlot);
            BlockUtil.clickBlock(targetPos.method_10074(), Direction.UP, false);
            InventoryUtil.inventorySwap(targetSlot, oldSlot);
        } else {
            InventoryUtil.switchToSlot(targetSlot);
            BlockUtil.clickBlock(targetPos.method_10074(), Direction.UP, false);
            InventoryUtil.switchToSlot(oldSlot);
        }
    }

    private void doPlace() {
        if (this.findNearbyShulkerBlock() != null) {
            return;
        }
        if (this.placePos != null && AutoRegear.mc.level.method_8320(this.placePos).method_26204() instanceof ShulkerBoxBlock) {
            return;
        }
        if (!this.timer.passed(this.placeDelay.getValueInt())) {
            return;
        }
        int shulkerSlot = this.findBestShulker();
        if (shulkerSlot == -1) {
            if (this.enderChest.getValue()) {
                BlockPos existingEChest = this.findNearbyEnderChestBlock();
                if (existingEChest != null) {
                    this.retrievingFromEnderChest = true;
                    this.openPos = existingEChest;
                    this.placePos = existingEChest;
                    BlockUtil.clickBlock(existingEChest, BlockUtil.getClickSide(existingEChest), false);
                    this.timer.reset();
                    return;
                }
                int eChestSlot = this.findEnderChestItem();
                if (eChestSlot != -1) {
                    this.doPlaceEnderChest(eChestSlot);
                    return;
                }
            }
            return;
        }
        int oldSlot = AutoRegear.mc.player.getInventory().selected;
        BlockPos playerFeet = EntityUtil.getPlayerPos(true);
        PlaceType effectiveType = this.placeType.getValue();
        if (effectiveType != PlaceType.Random && EntityUtil.isInsideBlock()) {
            effectiveType = PlaceType.Random;
        }
        switch (effectiveType.ordinal()) {
            case 1: {
                this.doPlaceBurrowShulker(shulkerSlot, oldSlot, playerFeet);
                break;
            }
            case 2: {
                this.doPlaceTowerShulker(shulkerSlot, oldSlot, playerFeet);
                break;
            }
            case 0: {
                this.doPlaceRandomShulker(shulkerSlot, oldSlot);
            }
        }
    }

    private void doPlaceEnderChest(int eChestSlot) {
        int oldSlot = AutoRegear.mc.player.getInventory().selected;
        BlockPos bestPos = this.findRandomPlacePos();
        if (bestPos != null) {
            this.retrievingFromEnderChest = true;
            this.executePlacement(eChestSlot, oldSlot, bestPos);
            this.placePos = bestPos;
            this.timer.reset();
        }
    }

    private void doPlaceBurrowShulker(int shulkerSlot, int oldSlot, BlockPos playerFeet) {
        if (!AutoRegear.mc.player.onGround() || !AutoRegear.mc.level.method_8320(playerFeet).method_45474() || this.isPositionBeingMined(playerFeet)) {
            return;
        }
        ++this.currentTryCount;
        if (this.currentTryCount > this.placeTryCount.getValueInt()) {
            this.disable();
            return;
        }
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 0.4199999868869781, AutoRegear.mc.player.getZ(), false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 0.7531999805212017, AutoRegear.mc.player.getZ(), false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 0.9999957640154541, AutoRegear.mc.player.getZ(), false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 1.1661092609382138, AutoRegear.mc.player.getZ(), false));
        this.executePlacement(shulkerSlot, oldSlot, playerFeet);
        ArrayList<BlockPosX> list = new ArrayList<BlockPosX>();
        for (double x = AutoRegear.mc.player.position().method_10216() - this.smartX.getValue(); x < AutoRegear.mc.player.position().method_10216() + this.smartX.getValue(); x += 1.0) {
            for (double z = AutoRegear.mc.player.position().method_10215() - this.smartX.getValue(); z < AutoRegear.mc.player.position().method_10215() + this.smartX.getValue(); z += 1.0) {
                for (double d = AutoRegear.mc.player.position().method_10214() - this.smartDown.getValue(); d < AutoRegear.mc.player.position().method_10214() + this.smartUp.getValue(); d += 1.0) {
                    list.add(new BlockPosX(x, d, z));
                }
            }
        }
        double distance = 0.0;
        BlockPos bestSmartPos = null;
        for (BlockPos class_23382 : list) {
            if (!this.canMove(class_23382) || (double)Mth.method_15355((float)((float)AutoRegear.mc.player.method_5707(class_23382.method_46558().method_1031(0.0, -0.5, 0.0)))) < this.smartDistance.getValue() || bestSmartPos != null && !(AutoRegear.mc.player.method_5707(class_23382.method_46558()) < distance)) continue;
            bestSmartPos = class_23382;
            distance = AutoRegear.mc.player.method_5707(class_23382.method_46558());
        }
        if (bestSmartPos != null) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos((double)bestSmartPos.method_10263() + 0.5, (double)bestSmartPos.method_10264(), (double)bestSmartPos.getZ() + 0.5, false));
        } else {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 2.3400880035762786, AutoRegear.mc.player.getZ(), false));
        }
        this.placePos = playerFeet;
        this.timer.reset();
    }

    private void doPlaceTowerShulker(int shulkerSlot, int oldSlot, BlockPos playerFeet) {
        if (!AutoRegear.mc.player.onGround() || !AutoRegear.mc.level.method_8320(playerFeet).method_45474() || !AutoRegear.mc.level.method_8320(playerFeet.method_10086(2)).method_45474() || this.isPositionBeingMined(playerFeet)) {
            return;
        }
        ++this.currentTryCount;
        if (this.currentTryCount > this.placeTryCount.getValueInt()) {
            this.disable();
            return;
        }
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 0.42, AutoRegear.mc.player.getZ(), false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 0.75, AutoRegear.mc.player.getZ(), false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 1.0, AutoRegear.mc.player.getZ(), false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 1.16, AutoRegear.mc.player.getZ(), false));
        AutoRegear.mc.player.method_5814(AutoRegear.mc.player.getX(), AutoRegear.mc.player.getY() + 1.0, AutoRegear.mc.player.getZ());
        this.executePlacement(shulkerSlot, oldSlot, playerFeet);
        this.placePos = playerFeet;
        this.timer.reset();
    }

    private void doPlaceRandomShulker(int shulkerSlot, int oldSlot) {
        BlockPos bestPos = this.findRandomPlacePos();
        if (bestPos != null) {
            this.executePlacement(shulkerSlot, oldSlot, bestPos);
            this.placePos = bestPos;
            this.timer.reset();
        }
    }

    public static enum PlaceType {
        Random,
        Burrow,
        Tower;

    }

    private static class Pulse {
        public final int slotId;
        public final Timer timer;
        public final long maxTime;

        public Pulse(int slotId) {
            this.slotId = slotId;
            this.timer = new Timer();
            this.timer.reset();
            this.maxTime = 400L;
        }
    }
}

