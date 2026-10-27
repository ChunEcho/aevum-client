/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Container
 *  net.minecraft.SimpleContainer
 *  net.minecraft.ShulkerBoxMenu
 *  net.minecraft.BlockItem
 *  net.minecraft.DyeColor
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Block
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.ResourceLocation
 *  net.minecraft.GuiGraphics
 *  net.minecraft.BookViewScreen
 *  net.minecraft.BookViewScreen$BookAccess
 *  net.minecraft.Screen
 *  net.minecraft.ShulkerBoxScreen
 *  net.minecraft.GameRenderer
 *  net.minecraft.DataComponents
 */
package dev.aevum.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.modules.impl.misc.ShulkerViewer;
import java.awt.Color;
import net.minecraft.Container;
import net.minecraft.SimpleContainer;
import net.minecraft.ShulkerBoxMenu;
import net.minecraft.BlockItem;
import net.minecraft.DyeColor;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Block;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.ResourceLocation;
import net.minecraft.GuiGraphics;
import net.minecraft.BookViewScreen;
import net.minecraft.Screen;
import net.minecraft.ShulkerBoxScreen;
import net.minecraft.GameRenderer;
import net.minecraft.DataComponents;

public class PeekScreen
extends ShulkerBoxScreen {
    private final ResourceLocation TEXTURE = ResourceLocation.method_60654((String)"textures/gui/container/shulker_box.png");
    private final ItemStack[] contents;
    private final ItemStack storageBlock;

    public PeekScreen(ItemStack storageBlock, ItemStack[] contents) {
        super(new ShulkerBoxMenu(0, Wrapper.mc.player.getInventory(), (Container)new SimpleContainer(contents)), Wrapper.mc.player.getInventory(), storageBlock.getHoverName());
        this.contents = contents;
        this.storageBlock = storageBlock;
    }

    public static Color getShulkerColor(ItemStack shulkerItem) {
        BlockItem blockItem;
        Block block;
        Item item = shulkerItem.method_7909();
        if (item instanceof BlockItem && (block = (blockItem = (BlockItem)item).getBlock()) instanceof ShulkerBoxBlock) {
            ShulkerBoxBlock shulkerBlock = (ShulkerBoxBlock)block;
            DyeColor dye = shulkerBlock.method_10528();
            if (dye == null) {
                return Color.WHITE;
            }
            int color = dye.getTextureDiffuseColor();
            return new Color(color);
        }
        return Color.WHITE;
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (button == 2 && this.hoveredSlot != null && !this.hoveredSlot.getItem().method_7960() && Wrapper.mc.player.containerMenu.getCarried().method_7960()) {
            ItemStack itemStack = this.hoveredSlot.getItem();
            if (ShulkerViewer.hasItems(itemStack) || itemStack.method_7909() == Items.ENDER_CHEST) {
                return ShulkerViewer.openContainer(this.hoveredSlot.getItem(), this.contents, false);
            }
            if (itemStack.method_57824(DataComponents.WRITTEN_BOOK_CONTENT) != null || itemStack.method_57824(DataComponents.WRITABLE_BOOK_CONTENT) != null) {
                this.method_25419();
                Wrapper.mc.method_1507((Screen)new BookViewScreen(BookViewScreen.BookViewScreen$BookAccess.method_17562((ItemStack)itemStack)));
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || Wrapper.mc.options.keyInventory.method_1417(keyCode, scanCode)) {
            this.method_25419();
            return true;
        }
        return false;
    }

    public boolean method_16803(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.method_25419();
            return true;
        }
        return false;
    }

    protected void method_2389(GuiGraphics context, float delta, int mouseX, int mouseY) {
        Color color = PeekScreen.getShulkerColor(this.storageBlock);
        RenderSystem.setShader(GameRenderer::method_34542);
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        context.method_25302(this.TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }
}

