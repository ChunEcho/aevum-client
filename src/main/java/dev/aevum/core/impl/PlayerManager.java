/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Container
 *  net.minecraft.Player
 *  net.minecraft.ChestMenu
 *  net.minecraft.ItemStack
 *  net.minecraft.Blocks
 *  net.minecraft.EnderChestBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.NonNullList
 *  net.minecraft.AABB
 *  net.minecraft.Screen
 *  net.minecraft.ContainerScreen
 *  net.minecraft.Attributes
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.BlockActivateEvent;
import dev.aevum.api.events.impl.GameLeftEvent;
import dev.aevum.api.events.impl.OpenScreenEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.mod.modules.Module;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.Container;
import net.minecraft.Player;
import net.minecraft.ChestMenu;
import net.minecraft.ItemStack;
import net.minecraft.Blocks;
import net.minecraft.EnderChestBlock;
import net.minecraft.BlockPos;
import net.minecraft.NonNullList;
import net.minecraft.AABB;
import net.minecraft.Screen;
import net.minecraft.ContainerScreen;
import net.minecraft.Attributes;

public class PlayerManager
implements Wrapper {
    public static Screen screenToOpen;
    public final NonNullList<ItemStack> ENDERCHEST_ITEM = NonNullList.method_10213((int)27, (Object)ItemStack.EMPTY);
    public final Map<Player, EntityAttribute> map = new ConcurrentHashMap<Player, EntityAttribute>();
    public final CopyOnWriteArrayList<Player> inWebPlayers = new CopyOnWriteArrayList();
    public boolean known = false;
    private int echestOpenedState;

    public PlayerManager() {
        Aevum.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onLogout(GameLeftEvent event) {
        this.inWebPlayers.clear();
        this.map.clear();
        this.ENDERCHEST_ITEM.clear();
        this.known = false;
    }

    @EventListener
    private void onBlockActivate(BlockActivateEvent event) {
        if (event.blockState.method_26204() instanceof EnderChestBlock && this.echestOpenedState == 0) {
            this.echestOpenedState = 1;
        }
    }

    @EventListener
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (this.echestOpenedState == 1 && event.screen instanceof ContainerScreen) {
            this.echestOpenedState = 2;
            return;
        }
        if (this.echestOpenedState == 0) {
            return;
        }
        if (!(PlayerManager.mc.screen instanceof ContainerScreen)) {
            return;
        }
        ChestMenu container = (ChestMenu)((ContainerScreen)PlayerManager.mc.screen).method_17577();
        if (container == null) {
            return;
        }
        Container inv = container.getContainer();
        for (int i = 0; i < 27; ++i) {
            this.ENDERCHEST_ITEM.set(i, (Object)inv.method_5438(i));
        }
        this.known = true;
        this.echestOpenedState = 0;
    }

    public void onUpdate() {
        if (Module.nullCheck()) {
            return;
        }
        if (screenToOpen != null && PlayerManager.mc.screen == null) {
            mc.method_1507(screenToOpen);
            screenToOpen = null;
        }
        this.inWebPlayers.clear();
        for (Player class_16572 : Aevum.THREAD.getPlayers()) {
            this.map.put(class_16572, new EntityAttribute(class_16572.getArmorValue(), class_16572.method_45325(Attributes.ARMOR_TOUGHNESS)));
            this.webUpdate(class_16572);
        }
    }

    public boolean isInWeb(Player player) {
        return this.inWebPlayers.contains(player);
    }

    private void webUpdate(Player player) {
        for (float x : new float[]{0.0f, 0.3f, -0.3f}) {
            for (float z : new float[]{0.0f, 0.3f, -0.3f}) {
                for (int y : new int[]{-1, 0, 1, 2}) {
                    BlockPos pos = new BlockPosX(player.getX() + (double)x, player.getY(), player.getZ() + (double)z).method_10086(y);
                    if (!new AABB(pos).method_994(player.method_5829()) || PlayerManager.mc.level.method_8320(pos).method_26204() != Blocks.COBWEB) continue;
                    this.inWebPlayers.add(player);
                    return;
                }
            }
        }
    }

    public record EntityAttribute(int armor, double toughness) {
    }
}

