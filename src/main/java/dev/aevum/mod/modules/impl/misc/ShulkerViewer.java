/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.NonNullList
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.CompoundTag
 *  net.minecraft.ListTag
 *  net.minecraft.Lighting
 *  net.minecraft.Font
 *  net.minecraft.GuiGraphics
 *  net.minecraft.Screen
 *  net.minecraft.HolderLookup$Provider
 *  net.minecraft.AbstractClientPlayer
 *  net.minecraft.CustomData
 *  net.minecraft.DataComponentMap
 *  net.minecraft.DataComponents
 *  org.lwjgl.opengl.GL11
 *  org.spongepowered.asm.mixin.Unique
 */
package dev.aevum.mod.modules.impl.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.asm.accessors.IContainerComponent;
import dev.aevum.core.impl.PlayerManager;
import dev.aevum.mod.gui.PeekScreen;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.BlockItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.NonNullList;
import net.minecraft.ShulkerBoxBlock;
import net.minecraft.CompoundTag;
import net.minecraft.ListTag;
import net.minecraft.Lighting;
import net.minecraft.Font;
import net.minecraft.GuiGraphics;
import net.minecraft.Screen;
import net.minecraft.HolderLookup;
import net.minecraft.AbstractClientPlayer;
import net.minecraft.CustomData;
import net.minecraft.DataComponentMap;
import net.minecraft.DataComponents;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Unique;

public class ShulkerViewer
extends Module {
    private static final ItemStack[] ITEMS = new ItemStack[27];
    public static ShulkerViewer INSTANCE;
    private static int offset;
    public final BooleanSetting toolTips = this.add(new BooleanSetting("ToolTips", true));
    public final BooleanSetting icon = this.add(new BooleanSetting("Icon", true));
    private final HashMap<UUID, Shulker> map = new HashMap();
    private final BooleanSetting peek = this.add(new BooleanSetting("Peek", false).setParent());
    private final SliderSetting renderTime = this.add(new SliderSetting("RenderTime", 10.0, 0.0, 100.0, 0.1, this.peek::isOpen).setSuffix("s"));
    private final SliderSetting xOffset = this.add(new SliderSetting("X", 0, 0, 1500, this.peek::isOpen));
    private final SliderSetting yOffset = this.add(new SliderSetting("Y", 120, 0, 1000, this.peek::isOpen));
    private final SliderSetting space = this.add(new SliderSetting("Space", 78.0, 0.0, 200.0, 1.0, this.peek::isOpen));

    public ShulkerViewer() {
        super("ShulkerViewer", Module.Category.Misc);
        this.setChinese("\u6f5c\u5f71\u76d2\u67e5\u770b");
        INSTANCE = this;
    }

    public static void renderShulkerToolTip(GuiGraphics context, int mouseX, int mouseY, ItemStack stack) {
        ShulkerViewer.getItemsInContainerItem(stack, ITEMS);
        ShulkerViewer.draw(context, mouseX, mouseY);
    }

    @Unique
    private static void draw(GuiGraphics context, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        GL11.glClear((int)256);
        Render2DUtil.drawRect(context.method_51448(), (float)(mouseX += 8), (float)(mouseY -= 82), 176.0f, 67.0f, new Color(0, 0, 0, 120));
        Lighting.setupFor3DItems();
        int row = 0;
        int i = 0;
        for (ItemStack itemStack : ITEMS) {
            context.method_51427(itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            context.method_51431(ShulkerViewer.mc.font, itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            if (++i < 9) continue;
            i = 0;
            ++row;
        }
        Lighting.setupForFlatItems();
        RenderSystem.enableDepthTest();
    }

    public static boolean hasItems(ItemStack itemStack) {
        IContainerComponent container = (IContainerComponent)itemStack.method_57824(DataComponents.CONTAINER);
        if (container != null && !container.getStacks().isEmpty()) {
            return true;
        }
        CompoundTag compoundTag = ((CustomData)itemStack.method_57825(DataComponents.BLOCK_ENTITY_DATA, (Object)CustomData.EMPTY)).method_57463();
        return compoundTag != null && compoundTag.method_10573("Items", 9);
    }

    public static void getItemsInContainerItem(ItemStack itemStack, ItemStack[] items) {
        block5: {
            CustomData nbt2;
            DataComponentMap components;
            block4: {
                if (itemStack.method_7909() == Items.ENDER_CHEST) {
                    for (int i = 0; i < Aevum.PLAYER.ENDERCHEST_ITEM.size(); ++i) {
                        items[i] = (ItemStack)Aevum.PLAYER.ENDERCHEST_ITEM.get(i);
                    }
                    return;
                }
                Arrays.fill(items, ItemStack.EMPTY);
                components = itemStack.method_57353();
                if (!components.method_57832(DataComponents.CONTAINER)) break block4;
                IContainerComponent container = (IContainerComponent)components.method_57829(DataComponents.CONTAINER);
                NonNullList<ItemStack> stacks = container.getStacks();
                for (int i = 0; i < stacks.size(); ++i) {
                    if (i < 0 || i >= items.length) continue;
                    items[i] = (ItemStack)stacks.get(i);
                }
                break block5;
            }
            if (!components.method_57832(DataComponents.BLOCK_ENTITY_DATA) || !(nbt2 = (CustomData)components.method_57829(DataComponents.BLOCK_ENTITY_DATA)).method_57450("Items")) break block5;
            ListTag nbt3 = (ListTag)nbt2.method_57463().method_10580("Items");
            for (int i = 0; i < nbt3.size(); ++i) {
                byte slot = nbt3.method_10602(i).method_10571("Slot");
                if (slot < 0 || slot >= items.length) continue;
                items[slot] = ItemStack.method_57359((HolderLookup.HolderLookup$Provider)ShulkerViewer.mc.player.registryAccess(), (CompoundTag)nbt3.method_10602(i));
            }
        }
    }

    public static boolean openContainer(ItemStack itemStack, ItemStack[] contents, boolean pause) {
        if (ShulkerViewer.hasItems(itemStack) || itemStack.method_7909() == Items.ENDER_CHEST) {
            ShulkerViewer.getItemsInContainerItem(itemStack, contents);
            if (pause) {
                PlayerManager.screenToOpen = new PeekScreen(itemStack, contents);
            } else {
                mc.method_1507((Screen)new PeekScreen(itemStack, contents));
            }
            return true;
        }
        return false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.peek.getValue()) {
            for (AbstractClientPlayer player : Aevum.THREAD.getPlayers()) {
                BlockItem blockItem;
                ItemStack stack = player.getMainHandItem();
                Item item = stack.method_7909();
                if (!(item instanceof BlockItem) || !((blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock)) {
                    stack = player.getOffhandItem();
                }
                if (!((item = stack.method_7909()) instanceof BlockItem) || !((blockItem = (BlockItem)item).getBlock() instanceof ShulkerBoxBlock)) continue;
                this.map.put(player.getGameProfile().getId(), new Shulker(stack, player.getGameProfile().getName()));
            }
        }
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        if (this.peek.getValue()) {
            offset = 0;
            this.map.values().removeIf(shulker -> shulker.draw(drawContext));
        }
    }

    class Shulker {
        final ItemStack itemStack;
        final String name;
        private final Timer timer;

        public Shulker(ItemStack itemStack, String name) {
            this.itemStack = itemStack;
            this.timer = new Timer();
            this.name = name;
        }

        public boolean draw(GuiGraphics context) {
            if (this.timer.passedS(ShulkerViewer.this.renderTime.getValue())) {
                return true;
            }
            ShulkerViewer.renderShulkerToolTip(context, ShulkerViewer.this.xOffset.getValueInt() - 8, ShulkerViewer.this.yOffset.getValueInt() + offset, this.itemStack);
            Font textRenderer = Wrapper.mc.font;
            int n = ShulkerViewer.this.xOffset.getValueInt();
            int n2 = ShulkerViewer.this.yOffset.getValueInt() + offset;
            Objects.requireNonNull(Wrapper.mc.font);
            context.method_51433(textRenderer, this.name, n, n2 - 9 - 82, -1, true);
            offset += ShulkerViewer.this.space.getValueInt();
            return false;
        }
    }
}

