/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.ArmorItem
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PotionContents
 *  net.minecraft.BedBlock
 *  net.minecraft.Blocks
 *  net.minecraft.LadderBlock
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.PistonBaseBlock
 *  net.minecraft.Screen
 *  net.minecraft.DataComponents
 *  org.apache.commons.io.IOUtils
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.core.Manager;
import dev.aevum.mod.gui.windows.WindowsScreen;
import dev.aevum.mod.gui.windows.impl.ItemSelectWindow;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.ArmorItem;
import net.minecraft.BlockItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContents;
import net.minecraft.BedBlock;
import net.minecraft.Blocks;
import net.minecraft.LadderBlock;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.PistonBaseBlock;
import net.minecraft.Screen;
import net.minecraft.DataComponents;
import org.apache.commons.io.IOUtils;

public class Sorter
extends Module {
    public static Sorter INSTANCE;
    final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final SliderSetting tasksPerTicks = this.add(new SliderSetting("TasksPerTick", 2, 1, 20));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 0.1, 0.0, 5.0, 0.01).setSuffix("s"));
    private final BooleanSetting stack = this.add(new BooleanSetting("Stack", true));
    private final EnumSetting<Mode> trashMode = this.add(new EnumSetting<Mode>("TrashMode", Mode.Whitelist));
    private final BooleanSetting edit = this.add(new BooleanSetting("EditTrash", false).injectTask(this::openGui));
    private final BooleanSetting sort = this.add(new BooleanSetting("Sort", true));
    private final BooleanSetting kit = this.add(new BooleanSetting("Kit", false).injectTask(this::onEnable));
    private final StringSetting kitName = this.add(new StringSetting("KitName", "kit1"));
    private final BooleanSetting drop = this.add(new BooleanSetting("Drop", true).setParent());
    private final BooleanSetting trash = this.add(new BooleanSetting("Trash", true, this.drop::isOpen));
    private final BooleanSetting rename = this.add(new BooleanSetting("Rename", true, this.drop::isOpen));
    private final BooleanSetting kitExceed = this.add(new BooleanSetting("KitExceed", true, this.drop::isOpen));
    private final BooleanSetting exceed = this.add(new BooleanSetting("Exceed", true, this.drop::isOpen));
    private final SliderSetting crystal = this.add(new SliderSetting("Crystal", 4, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting exp = this.add(new SliderSetting("Exp", 4, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting totem = this.add(new SliderSetting("Totem", 6, 0, 36, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting eGapple = this.add(new SliderSetting("EGapple", 2, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting gapple = this.add(new SliderSetting("Gapple", 2, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting obsidian = this.add(new SliderSetting("Obsidian", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting web = this.add(new SliderSetting("Web", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting glowstone = this.add(new SliderSetting("Glowstone", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting anchor = this.add(new SliderSetting("Anchor", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting pearl = this.add(new SliderSetting("Pearl", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting piston = this.add(new SliderSetting("Piston", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting redstone = this.add(new SliderSetting("RedStone", 1, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting ladder = this.add(new SliderSetting("Ladder", 2, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting bed = this.add(new SliderSetting("Bed", 4, 0, 12, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting speed = this.add(new SliderSetting("Speed", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting turtle = this.add(new SliderSetting("Resistance", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final SliderSetting strength = this.add(new SliderSetting("Strength", 1, 0, 8, () -> this.drop.isOpen() && this.exceed.isOpen()));
    private final Timer timer = new Timer();
    private final Map<Integer, String> kitMap = new ConcurrentHashMap<Integer, String>();

    public Sorter() {
        super("Sorter", Module.Category.Player);
        this.setChinese("\u80cc\u5305\u6574\u7406");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (Sorter.nullCheck()) {
            return;
        }
        Aevum.THREAD.execute(() -> {
            this.kitMap.clear();
            try {
                File file = Manager.getFile(this.kitName.getValue() + ".kit");
                if (!file.exists()) {
                    return;
                }
                List list = IOUtils.readLines((InputStream)new FileInputStream(file), (Charset)StandardCharsets.UTF_8);
                for (String s : list) {
                    String[] split = s.split(":");
                    if (split.length != 2) {
                        return;
                    }
                    this.kitMap.put(Integer.valueOf(split[0]), split[1]);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().method_7909() != item) continue;
            ++count;
        }
        if (Sorter.mc.player.getOffhandItem().method_7909() == item) {
            ++count;
        }
        return count;
    }

    public static int getItemCount(Class<?> clazz) {
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (!(entry.getValue().method_7909() instanceof BlockItem) || !clazz.isInstance(((BlockItem)entry.getValue().method_7909()).getBlock())) continue;
            ++count;
        }
        return count;
    }

    private void openGui() {
        this.edit.setValueWithoutTask(false);
        if (!Sorter.nullCheck()) {
            mc.method_1507((Screen)new WindowsScreen(new ItemSelectWindow(Aevum.CLEANER)));
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!this.timer.passedS(this.delay.getValue())) {
            return;
        }
        if (!EntityUtil.inInventory()) {
            return;
        }
        if (this.exceed.getValue()) {
            this.updateItem();
        }
        int i = 0;
        while ((double)i < this.tasksPerTicks.getValue()) {
            this.tweak();
            ++i;
        }
    }

    private void tweak() {
        ItemStack stack;
        int slot1;
        if (this.drop.getValue()) {
            for (slot1 = 35; slot1 >= 0; --slot1) {
                stack = Sorter.mc.player.getInventory().method_5438(slot1);
                if (stack.method_7960() || !this.shouldDrop(stack)) continue;
                this.timer.reset();
                Sorter.mc.gameMode.method_2906(Sorter.mc.player.containerMenu.containerId, slot1 < 9 ? slot1 + 36 : slot1, 1, ClickType.THROW, (Player)Sorter.mc.player);
                return;
            }
        }
        if (this.stack.getValue()) {
            for (slot1 = 35; slot1 >= 9; --slot1) {
                stack = Sorter.mc.player.getInventory().method_5438(slot1);
                if (stack.method_7960() || !stack.isStackable() || stack.getCount() == stack.getMaxStackSize()) continue;
                for (int slot2 = 0; slot2 < 36; ++slot2) {
                    ItemStack stack2;
                    if (slot1 == slot2 || (stack2 = Sorter.mc.player.getInventory().method_5438(slot2)).getCount() == stack2.getMaxStackSize() || !Sorter.canMerge(stack, stack2)) continue;
                    Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot1, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                    Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot2 < 9 ? slot2 + 36 : slot2, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                    Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot1, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                    this.timer.reset();
                    return;
                }
            }
        }
        if (this.drop.getValue()) {
            for (slot1 = 35; slot1 >= 0; --slot1) {
                stack = Sorter.mc.player.getInventory().method_5438(slot1);
                if (stack.method_7960() || !this.exceed.getValue() || !this.exceed(stack, false)) continue;
                this.timer.reset();
                Sorter.mc.gameMode.method_2906(Sorter.mc.player.containerMenu.containerId, slot1 < 9 ? slot1 + 36 : slot1, 1, ClickType.THROW, (Player)Sorter.mc.player);
                return;
            }
        }
        if (this.sort.getValue()) {
            if (this.kit.getValue()) {
                for (slot1 = 0; slot1 < 36; ++slot1) {
                    if (!this.kitMap.containsKey(slot1)) continue;
                    String target = this.kitMap.get(slot1);
                    String name = Sorter.mc.player.getInventory().method_5438(slot1).method_7909().method_7876();
                    if (name.equals(target)) continue;
                    for (slot2 = 0; slot2 < 36; ++slot2) {
                        String itemID;
                        String slot2Target = this.kitMap.get(slot2);
                        ItemStack stack2 = Sorter.mc.player.getInventory().method_5438(slot2);
                        if (stack2.method_7960() || (itemID = stack2.method_7909().method_7876()).equals(slot2Target) || !itemID.equals(target)) continue;
                        Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot1 < 9 ? slot1 + 36 : slot1, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                        Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot2 < 9 ? slot2 + 36 : slot2, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                        Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot1 < 9 ? slot1 + 36 : slot1, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                        this.timer.reset();
                        return;
                    }
                }
            } else {
                for (slot1 = 9; slot1 < 36; ++slot1) {
                    int minId;
                    int id = Item.method_7880((Item)Sorter.mc.player.getInventory().method_5438(slot1).method_7909());
                    if (Sorter.mc.player.getInventory().method_5438(slot1).method_7960()) {
                        id = 114514;
                    }
                    if ((minId = this.getMinId(slot1, id)) >= id) continue;
                    for (slot2 = 35; slot2 > slot1; --slot2) {
                        int itemID;
                        ItemStack stack3 = Sorter.mc.player.getInventory().method_5438(slot2);
                        if (stack3.method_7960() || (itemID = Item.method_7880((Item)stack3.method_7909())) != minId) continue;
                        Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot1, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                        Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot2, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                        Sorter.mc.gameMode.method_2906(Sorter.mc.player.inventoryMenu.containerId, slot1, 0, ClickType.PICKUP, (Player)Sorter.mc.player);
                        this.timer.reset();
                        return;
                    }
                }
            }
        }
        if (this.drop.getValue() && this.kitExceed.getValue()) {
            for (slot1 = 35; slot1 >= 0; --slot1) {
                BlockItem blockItem;
                Item item;
                if (!this.kitMap.containsKey(slot1)) continue;
                ItemStack stack4 = Sorter.mc.player.getInventory().method_5438(slot1);
                if (this.exceed.getValue() && !this.exceed(stack4, true) || stack4.method_7960() || stack4.method_7909() instanceof ArmorItem || (item = stack4.method_7909()) instanceof BlockItem && (blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock || stack4.method_7909().method_7876().equals(this.kitMap.get(slot1))) continue;
                this.timer.reset();
                Sorter.mc.gameMode.method_2906(Sorter.mc.player.containerMenu.containerId, slot1 < 9 ? slot1 + 36 : slot1, 1, ClickType.THROW, (Player)Sorter.mc.player);
                return;
            }
        }
    }

    private boolean shouldDrop(ItemStack stack) {
        boolean inList;
        BlockItem blockItem;
        Item item = stack.method_7909();
        if (this.trash.getValue() && (!(item instanceof BlockItem) || !((blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock)) && (!(inList = Aevum.CLEANER.inList(item.method_7876())) && this.trashMode.is(Mode.Whitelist) || inList && this.trashMode.is(Mode.Blacklist))) {
            return true;
        }
        if (this.rename.getValue()) {
            return stack.isStackable() && !stack.getHoverName().getString().equals(item.getDescription().getString());
        }
        return false;
    }

    private boolean exceed(ItemStack i, boolean dropOther) {
        if (i.method_7909().equals(Items.END_CRYSTAL)) {
            if ((double)this.stealCountList[0] > this.crystal.getValue()) {
                this.stealCountList[0] = this.stealCountList[0] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Items.EXPERIENCE_BOTTLE)) {
            if ((double)this.stealCountList[1] > this.exp.getValue()) {
                this.stealCountList[1] = this.stealCountList[1] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Items.TOTEM_OF_UNDYING)) {
            if ((double)this.stealCountList[2] > this.totem.getValue()) {
                this.stealCountList[2] = this.stealCountList[2] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
            if ((double)this.stealCountList[3] > this.eGapple.getValue()) {
                this.stealCountList[3] = this.stealCountList[3] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Blocks.OBSIDIAN.method_8389())) {
            if ((double)this.stealCountList[4] > this.obsidian.getValue()) {
                this.stealCountList[4] = this.stealCountList[4] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Blocks.COBWEB.method_8389())) {
            if ((double)this.stealCountList[5] > this.web.getValue()) {
                this.stealCountList[5] = this.stealCountList[5] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Blocks.GLOWSTONE.method_8389())) {
            if ((double)this.stealCountList[6] > this.glowstone.getValue()) {
                this.stealCountList[6] = this.stealCountList[6] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Blocks.RESPAWN_ANCHOR.method_8389())) {
            if ((double)this.stealCountList[7] > this.anchor.getValue()) {
                this.stealCountList[7] = this.stealCountList[7] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Items.ENDER_PEARL)) {
            if ((double)this.stealCountList[8] > this.pearl.getValue()) {
                this.stealCountList[8] = this.stealCountList[8] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909() instanceof BlockItem && ((BlockItem)i.method_7909()).getBlock() instanceof PistonBaseBlock) {
            if ((double)this.stealCountList[9] > this.piston.getValue()) {
                this.stealCountList[9] = this.stealCountList[9] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909().equals(Blocks.REDSTONE_BLOCK.method_8389())) {
            if ((double)this.stealCountList[10] > this.redstone.getValue()) {
                this.stealCountList[10] = this.stealCountList[10] - 1;
                return true;
            }
            return false;
        }
        if (i.method_7909() instanceof BlockItem && ((BlockItem)i.method_7909()).getBlock() instanceof BedBlock) {
            if ((double)this.stealCountList[11] > this.bed.getValue()) {
                this.stealCountList[11] = this.stealCountList[11] - 1;
                return true;
            }
            return false;
        }
        if (Item.method_7880((Item)i.method_7909()) == Item.method_7880((Item)Items.SPLASH_POTION)) {
            PotionContents potionContentsComponent = (PotionContents)i.method_57825(DataComponents.POTION_CONTENTS, (Object)PotionContents.EMPTY);
            for (MobEffectInstance effect : potionContentsComponent.method_57397()) {
                if (effect.method_5579().comp_349() == MobEffects.MOVEMENT_SPEED.comp_349()) {
                    if ((double)this.stealCountList[12] > this.speed.getValue()) {
                        this.stealCountList[12] = this.stealCountList[12] - 1;
                        return true;
                    }
                    return false;
                }
                if (effect.method_5579().comp_349() == MobEffects.DAMAGE_RESISTANCE.comp_349()) {
                    if ((double)this.stealCountList[13] > this.turtle.getValue()) {
                        this.stealCountList[13] = this.stealCountList[13] - 1;
                        return true;
                    }
                    return false;
                }
                if (effect.method_5579().comp_349() != MobEffects.DAMAGE_BOOST.comp_349()) continue;
                if ((double)this.stealCountList[16] > this.strength.getValue()) {
                    this.stealCountList[16] = this.stealCountList[16] - 1;
                    return true;
                }
                return false;
            }
        }
        if (i.method_7909().equals(Items.GOLDEN_APPLE) && (double)this.stealCountList[14] > this.gapple.getValue()) {
            this.stealCountList[14] = this.stealCountList[14] - 1;
            return true;
        }
        if (i.method_7909() instanceof BlockItem && ((BlockItem)i.method_7909()).getBlock() instanceof LadderBlock && (double)this.stealCountList[15] > this.ladder.getValue()) {
            this.stealCountList[15] = this.stealCountList[15] - 1;
            return true;
        }
        return dropOther;
    }

    private void updateItem() {
        this.stealCountList[0] = Sorter.getItemCount(Items.END_CRYSTAL);
        this.stealCountList[1] = Sorter.getItemCount(Items.EXPERIENCE_BOTTLE);
        this.stealCountList[2] = Sorter.getItemCount(Items.TOTEM_OF_UNDYING);
        this.stealCountList[3] = Sorter.getItemCount(Items.ENCHANTED_GOLDEN_APPLE);
        this.stealCountList[4] = Sorter.getItemCount(Items.OBSIDIAN);
        this.stealCountList[5] = Sorter.getItemCount(Items.COBWEB);
        this.stealCountList[6] = Sorter.getItemCount(Items.GLOWSTONE);
        this.stealCountList[7] = Sorter.getItemCount(Items.RESPAWN_ANCHOR);
        this.stealCountList[8] = Sorter.getItemCount(Items.ENDER_PEARL);
        this.stealCountList[9] = Sorter.getItemCount(Items.PISTON) - Sorter.getItemCount(Items.STICKY_PISTON);
        this.stealCountList[10] = Sorter.getItemCount(Items.REDSTONE_BLOCK);
        this.stealCountList[11] = Sorter.getItemCount(BedBlock.class);
        this.stealCountList[12] = InventoryUtil.getPotionCount((MobEffect)MobEffects.MOVEMENT_SPEED.comp_349());
        this.stealCountList[13] = InventoryUtil.getPotionCount((MobEffect)MobEffects.DAMAGE_RESISTANCE.comp_349());
        this.stealCountList[14] = Sorter.getItemCount(Items.GOLDEN_APPLE);
        this.stealCountList[15] = Sorter.getItemCount(LadderBlock.class);
        this.stealCountList[16] = InventoryUtil.getPotionCount((MobEffect)MobEffects.DAMAGE_BOOST.comp_349());
    }

    private int getMinId(int slot, int currentId) {
        int id = currentId;
        for (int slot1 = slot + 1; slot1 < 36; ++slot1) {
            int itemID;
            ItemStack stack = Sorter.mc.player.getInventory().method_5438(slot1);
            if (stack.method_7960() || (itemID = Item.method_7880((Item)stack.method_7909())) >= id) continue;
            id = itemID;
        }
        return id;
    }

    public static boolean canMerge(ItemStack source, ItemStack stack) {
        return ItemStack.method_31577((ItemStack)source, (ItemStack)stack);
    }

    private static enum Mode {
        Whitelist,
        Blacklist;

    }
}

