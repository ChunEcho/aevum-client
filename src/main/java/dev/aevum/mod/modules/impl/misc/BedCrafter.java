/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.CraftingMenu
 *  net.minecraft.BedItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Mth
 *  net.minecraft.RecipeCollection
 *  net.minecraft.HolderLookup$Provider
 *  net.minecraft.RecipeHolder
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.CraftingMenu;
import net.minecraft.BedItem;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Mth;
import net.minecraft.RecipeCollection;
import net.minecraft.HolderLookup;
import net.minecraft.RecipeHolder;

public class BedCrafter
extends Module {
    public static BedCrafter INSTANCE;
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5, 0, 8));
    private final BooleanSetting disable = this.add(new BooleanSetting("Disable", true));

    public BedCrafter() {
        super("BedCrafter", Module.Category.Misc);
        this.setChinese("\u81ea\u52a8\u5236\u4f5c\u5e8a");
        INSTANCE = this;
    }

    public static int getEmptySlots() {
        int emptySlots = 0;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = BedCrafter.mc.player.getInventory().method_5438(i);
            if (!itemStack.method_7960()) continue;
            ++emptySlots;
        }
        return emptySlots;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (BedCrafter.getEmptySlots() == 0) {
            if (BedCrafter.mc.player.containerMenu instanceof CraftingMenu) {
                BedCrafter.mc.player.method_7346();
            }
            if (this.disable.getValue()) {
                this.disable();
            }
            return;
        }
        if (BedCrafter.mc.player.containerMenu instanceof CraftingMenu) {
            boolean craft = false;
            block0: for (RecipeCollection recipeResult : BedCrafter.mc.player.getRecipeBook().method_1393()) {
                for (RecipeHolder recipe : recipeResult.method_2648(true)) {
                    if (!(recipe.comp_1933().method_8110((HolderLookup.HolderLookup$Provider)BedCrafter.mc.level.method_30349()).method_7909() instanceof BedItem)) continue;
                    for (int i = 0; i < BedCrafter.getEmptySlots(); ++i) {
                        craft = true;
                        BedCrafter.mc.gameMode.method_2912(BedCrafter.mc.player.containerMenu.containerId, recipe, false);
                        BedCrafter.mc.gameMode.method_2906(BedCrafter.mc.player.containerMenu.containerId, 0, 1, ClickType.QUICK_MOVE, (Player)BedCrafter.mc.player);
                    }
                    continue block0;
                }
            }
            if (!craft) {
                if (BedCrafter.mc.player.containerMenu instanceof CraftingMenu) {
                    BedCrafter.mc.player.method_7346();
                }
                if (this.disable.getValue()) {
                    this.disable();
                }
            }
        } else {
            this.doPlace();
        }
    }

    private void doPlace() {
        BlockPos bestPos = null;
        double getDistance = 100.0;
        boolean place = true;
        for (BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat())) {
            if (BedCrafter.mc.level.method_8320(pos).method_26204() == Blocks.CRAFTING_TABLE && BlockUtil.getClickSideStrict(pos) != null) {
                place = false;
                bestPos = pos;
                break;
            }
            if (!BlockUtil.canPlace(pos) || bestPos != null && !((double)Mth.method_15355((float)((float)BedCrafter.mc.player.method_5707(pos.method_46558()))) < getDistance)) continue;
            bestPos = pos;
            getDistance = Mth.method_15355((float)((float)BedCrafter.mc.player.method_5707(pos.method_46558())));
        }
        if (bestPos != null) {
            if (!place) {
                BlockUtil.clickBlock(bestPos, BlockUtil.getClickSide(bestPos), this.rotate.getValue());
            } else {
                if (InventoryUtil.findItem(Items.CRAFTING_TABLE) == -1) {
                    return;
                }
                int old = BedCrafter.mc.player.getInventory().selected;
                InventoryUtil.switchToSlot(InventoryUtil.findItem(Items.CRAFTING_TABLE));
                BlockUtil.placeBlock(bestPos, this.rotate.getValue());
                InventoryUtil.switchToSlot(old);
            }
        }
    }
}

