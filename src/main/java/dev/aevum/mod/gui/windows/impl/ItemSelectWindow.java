/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.I18n
 *  net.minecraft.Item
 *  net.minecraft.Block
 *  net.minecraft.BufferUploader
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tesselator
 *  net.minecraft.DefaultVertexFormat
 *  net.minecraft.VertexFormat$Mode
 *  net.minecraft.GuiGraphics
 *  net.minecraft.StringUtil
 *  net.minecraft.InputConstants
 *  net.minecraft.GameRenderer
 *  net.minecraft.BuiltInRegistries
 *  net.minecraft.MeshData
 */
package dev.aevum.mod.gui.windows.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.core.Manager;
import dev.aevum.core.impl.CleanerManager;
import dev.aevum.core.impl.FontManager;
import dev.aevum.core.impl.TradeManager;
import dev.aevum.core.impl.XrayManager;
import dev.aevum.mod.gui.items.buttons.StringButton;
import dev.aevum.mod.gui.windows.WindowBase;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.I18n;
import net.minecraft.Item;
import net.minecraft.Block;
import net.minecraft.BufferUploader;
import net.minecraft.BufferBuilder;
import net.minecraft.Tesselator;
import net.minecraft.DefaultVertexFormat;
import net.minecraft.VertexFormat;
import net.minecraft.GuiGraphics;
import net.minecraft.StringUtil;
import net.minecraft.InputConstants;
import net.minecraft.GameRenderer;
import net.minecraft.BuiltInRegistries;
import net.minecraft.MeshData;

public class ItemSelectWindow
extends WindowBase {
    private final Manager manager;
    private final ArrayList<ItemPlate> itemPlates = new ArrayList();
    private final ArrayList<ItemPlate> allItems = new ArrayList();
    private boolean allTab = true;
    private boolean listening = false;
    private String search = "Search";

    public ItemSelectWindow(Manager manager) {
        this((float)Wrapper.mc.getWindow().getGuiScaledWidth() / 2.0f - 100.0f, (float)Wrapper.mc.getWindow().getGuiScaledHeight() / 2.0f - 150.0f, 200.0f, 300.0f, manager);
    }

    public ItemSelectWindow(float x, float y, float width, float height, Manager manager) {
        super(x, y, width, height, "Items", null);
        this.manager = manager;
        this.refreshItemPlates();
        int id1 = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            this.allItems.add(new ItemPlate(id1, id1 * 20, block.method_8389(), block.getDescriptionId()));
            ++id1;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            this.allItems.add(new ItemPlate(id1, id1 * 20, item, item.method_7876()));
            ++id1;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY) {
        super.render(context, mouseX, mouseY);
        boolean hover1 = Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 90.0f, this.getY() + 3.0f, 70.0, 10.0);
        Render2DUtil.drawRect(context.method_51448(), this.getX() + this.getWidth() - 90.0f, this.getY() + 3.0f, 70.0f, 10.0f, hover1 ? new Color(-981236861, true) : new Color(-984131753, true));
        FontManager.drawSmall(context.method_51448(), this.search, this.getX() + this.getWidth() - 86.0f, this.getY() + 7.0f, new Color(0xD5D5D5).getRGB());
        RenderSystem.setShader(GameRenderer::method_34540);
        int tabColor1 = this.allTab ? new Color(0xD5D5D5).getRGB() : Color.GRAY.getRGB();
        int tabColor2 = this.allTab ? Color.GRAY.getRGB() : new Color(0xBDBDBD).getRGB();
        BufferBuilder bufferBuilder = Tesselator.getInstance().method_60827(VertexFormat.VertexFormat$Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.method_22912(this.getX() + 1.5f, this.getY() + 29.0f, 0.0f).method_39415(Color.DARK_GRAY.getRGB());
        bufferBuilder.method_22912(this.getX() + 8.0f, this.getY() + 29.0f, 0.0f).method_39415(tabColor1);
        bufferBuilder.method_22912(this.getX() + 8.0f, this.getY() + 19.0f, 0.0f).method_39415(tabColor1);
        bufferBuilder.method_22912(this.getX() + 48.0f, this.getY() + 19.0f, 0.0f).method_39415(tabColor1);
        bufferBuilder.method_22912(this.getX() + 54.0f, this.getY() + 29.0f, 0.0f).method_39415(tabColor1);
        bufferBuilder.method_22912(this.getX() + 52.0f, this.getY() + 25.0f, 0.0f).method_39415(tabColor2);
        bufferBuilder.method_22912(this.getX() + 52.0f, this.getY() + 19.0f, 0.0f).method_39415(tabColor2);
        bufferBuilder.method_22912(this.getX() + 92.0f, this.getY() + 19.0f, 0.0f).method_39415(tabColor2);
        bufferBuilder.method_22912(this.getX() + 100.0f, this.getY() + 29.0f, 0.0f).method_39415(Color.GRAY.getRGB());
        bufferBuilder.method_22912(this.getX() + this.getWidth() - 1.0f, this.getY() + 29.0f, 0.0f).method_39415(Color.DARK_GRAY.getRGB());
        BufferUploader.method_43433((MeshData)bufferBuilder.method_60800());
        FontManager.drawSmall(context.method_51448(), "All", this.getX() + 25.0f, this.getY() + 25.0f, tabColor1);
        FontManager.drawSmall(context.method_51448(), "Selected", this.getX() + 60.0f, this.getY() + 25.0f, tabColor2);
        if (!this.allTab && this.itemPlates.isEmpty()) {
            FontManager.drawCentered(context.method_51448(), "It's empty here yet", this.getX() + this.getWidth() / 2.0f, this.getY() + this.getHeight() / 2.0f, new Color(0xBDBDBD).getRGB());
        }
        context.method_44379((int)this.getX(), (int)(this.getY() + 30.0f), (int)(this.getX() + this.getWidth()), (int)(this.getY() + this.getHeight() - 1.0f));
        for (ItemPlate itemPlate : this.allTab ? this.allItems : this.itemPlates) {
            if (itemPlate.offset + this.getY() + 25.0f + this.getScrollOffset() > this.getY() + this.getHeight() || itemPlate.offset + this.getScrollOffset() + this.getY() + 10.0f < this.getY()) continue;
            context.method_51448().method_22903();
            context.method_51448().method_46416(this.getX() + 6.0f, itemPlate.offset + this.getY() + 32.0f + this.getScrollOffset(), 0.0f);
            context.method_51427(itemPlate.item().getDefaultInstance(), 0, 0);
            context.method_51448().method_22909();
            FontManager.draw(context.method_51448(), I18n.method_4662((String)itemPlate.key(), (Object[])new Object[0]), this.getX() + 26.0f, itemPlate.offset + this.getY() + 38.0f + this.getScrollOffset(), new Color(0xBDBDBD).getRGB());
            boolean hover2 = Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 20.0f, itemPlate.offset + this.getY() + 35.0f + this.getScrollOffset(), 11.0, 11.0);
            Render2DUtil.drawRect(context.method_51448(), this.getX() + this.getWidth() - 20.0f, itemPlate.offset + this.getY() + 35.0f + this.getScrollOffset(), 11.0f, 11.0f, hover2 ? new Color(-981828998, true) : new Color(-984131753, true));
            boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key, itemPlate.key));
            if (this.allTab && !selected) {
                FontManager.draw(context.method_51448(), "+", this.getX() + this.getWidth() - 17.0f, itemPlate.offset + this.getY() + 37.0f + this.getScrollOffset(), -1);
                continue;
            }
            FontManager.draw(context.method_51448(), "-", (double)(this.getX() + this.getWidth()) - 16.5, (double)(itemPlate.offset + this.getY()) + 37.5 + (double)this.getScrollOffset(), -1);
        }
        this.setMaxElementsHeight((this.allTab ? this.allItems : this.itemPlates).size() * 20);
        context.disableScissor();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (Render2DUtil.isHovered(mouseX, mouseY, this.getX() + 8.0f, this.getY() + 19.0f, 52.0, 19.0)) {
            this.allTab = true;
            this.resetScroll();
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.getX() + 54.0f, this.getY() + 19.0f, 70.0, 19.0)) {
            this.allTab = false;
            this.resetScroll();
        }
        if (Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 90.0f, this.getY() + 3.0f, 70.0, 10.0)) {
            this.listening = true;
            this.search = "";
        }
        ArrayList copy = Lists.newArrayList(this.allTab ? this.allItems : this.itemPlates);
        for (ItemPlate itemPlate : copy) {
            XrayManager m;
            CleanerManager m2;
            TradeManager m3;
            Manager manager;
            if ((float)((int)(itemPlate.offset + this.getY() + 50.0f)) + this.getScrollOffset() > this.getY() + this.getHeight()) continue;
            String name = itemPlate.key().replace("item.minecraft.", "").replace("block.minecraft.", "");
            if (!Render2DUtil.isHovered(mouseX, mouseY, this.getX() + this.getWidth() - 20.0f, itemPlate.offset + this.getY() + 35.0f + this.getScrollOffset(), 10.0, 10.0)) continue;
            boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key(), itemPlate.key));
            if (this.allTab && !selected) {
                manager = this.manager;
                if (manager instanceof TradeManager) {
                    m3 = (TradeManager)manager;
                    if (m3.inWhitelist(name)) continue;
                    m3.add(name);
                    this.refreshItemPlates();
                    continue;
                }
                manager = this.manager;
                if (manager instanceof CleanerManager) {
                    m2 = (CleanerManager)manager;
                    if (m2.inList(name)) continue;
                    m2.add(name);
                    this.refreshItemPlates();
                    continue;
                }
                manager = this.manager;
                if (!(manager instanceof XrayManager) || (m = (XrayManager)manager).inWhitelist(name)) continue;
                m.add(name);
                this.refreshItemPlates();
                continue;
            }
            manager = this.manager;
            if (manager instanceof TradeManager) {
                m3 = (TradeManager)manager;
                m3.remove(name);
            } else {
                manager = this.manager;
                if (manager instanceof CleanerManager) {
                    m2 = (CleanerManager)manager;
                    m2.remove(name);
                } else {
                    manager = this.manager;
                    if (manager instanceof XrayManager) {
                        m = (XrayManager)manager;
                        m.remove(name);
                    }
                }
            }
            this.refreshItemPlates();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 70 && (InputConstants.method_15987((long)Wrapper.mc.getWindow().getWindow(), (int)341) || InputConstants.method_15987((long)Wrapper.mc.getWindow().getWindow(), (int)345))) {
            this.listening = !this.listening;
            return;
        }
        if (this.listening) {
            switch (keyCode) {
                case 256: {
                    this.listening = false;
                    this.search = "Search";
                    this.refreshAllItems();
                    break;
                }
                case 259: {
                    this.search = StringButton.removeLastChar(this.search);
                    this.refreshAllItems();
                    if (!Objects.equals(this.search, "")) break;
                    this.listening = false;
                    this.search = "Search";
                    break;
                }
                case 32: {
                    this.search = this.search + " ";
                }
            }
        }
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringUtil.method_57175((char)key) && this.listening) {
            this.search = this.search + key;
            this.refreshAllItems();
        }
    }

    private void refreshItemPlates() {
        this.itemPlates.clear();
        int id = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            XrayManager m;
            Manager manager = this.manager;
            if (manager instanceof TradeManager) {
                TradeManager m2 = (TradeManager)manager;
                if (!m2.inWhitelist(item.method_7876())) continue;
                this.itemPlates.add(new ItemPlate(id, id * 20, item.method_8389(), item.method_7876()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (manager instanceof CleanerManager) {
                CleanerManager m3 = (CleanerManager)manager;
                if (!m3.inList(item.method_7876())) continue;
                this.itemPlates.add(new ItemPlate(id, id * 20, item.method_8389(), item.method_7876()));
                ++id;
                continue;
            }
            manager = this.manager;
            if (!(manager instanceof XrayManager) || !(m = (XrayManager)manager).inWhitelist(item.method_7876())) continue;
            this.itemPlates.add(new ItemPlate(id, id * 20, item.method_8389(), item.method_7876()));
            ++id;
        }
    }

    private void refreshAllItems() {
        this.allItems.clear();
        this.resetScroll();
        int id1 = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (!this.search.equals("Search") && !this.search.isEmpty() && !item.method_7876().contains(this.search) && !item.getDescription().getString().toLowerCase().contains(this.search.toLowerCase())) continue;
            this.allItems.add(new ItemPlate(id1, id1 * 20, item, item.method_7876()));
            ++id1;
        }
    }

    private record ItemPlate(float id, float offset, Item item, String key) {
    }
}

