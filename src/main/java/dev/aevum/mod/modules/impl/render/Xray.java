/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockGetter
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.BlockPos$MutableBlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.Shapes
 *  net.minecraft.BlockState
 *  net.minecraft.Screen
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.AmbientOcclusionEvent;
import dev.aevum.api.events.impl.ChunkOcclusionEvent;
import dev.aevum.api.events.impl.RenderBlockEntityEvent;
import dev.aevum.mod.gui.windows.WindowsScreen;
import dev.aevum.mod.gui.windows.impl.ItemSelectWindow;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.BlockGetter;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Vec3i;
import net.minecraft.Shapes;
import net.minecraft.BlockState;
import net.minecraft.Screen;

public class Xray
extends Module {
    public static Xray INSTANCE;
    public final BooleanSetting edit = this.add(new BooleanSetting("Edit", false).injectTask(this::openGui));
    private static final ThreadLocal<BlockPos.BlockPos$MutableBlockPos> EXPOSED_POS;

    public Xray() {
        super("Xray", Module.Category.Render);
        this.setChinese("\u77ff\u7269\u900f\u89c6");
        INSTANCE = this;
    }

    private void openGui() {
        this.edit.setValueWithoutTask(false);
        if (!Xray.nullCheck()) {
            mc.method_1507((Screen)new WindowsScreen(new ItemSelectWindow(Aevum.XRAY)));
        }
    }

    @Override
    public void onEnable() {
        if (!Xray.nullCheck()) {
            Xray.mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onDisable() {
        Xray.mc.levelRenderer.allChanged();
    }

    public boolean isBlocked(Block block) {
        return !Aevum.XRAY.inWhitelist(block.getDescriptionId());
    }

    @EventListener
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (this.isBlocked(event.blockEntity.getBlockState().method_26204())) {
            event.cancel();
        }
    }

    @EventListener
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventListener
    private void onAmbientOcclusion(AmbientOcclusionEvent event) {
        event.lightLevel = 1.0f;
    }

    public static boolean shouldBlock(BlockState state) {
        return INSTANCE.isOn() && INSTANCE.isBlocked(state.method_26204());
    }

    public boolean modifyDrawSide(BlockState state, BlockGetter view, BlockPos pos, Direction facing, boolean returns) {
        if (!returns && !this.isBlocked(state.method_26204())) {
            BlockPos adjPos = pos.method_10093(facing);
            BlockState adjState = view.method_8320(adjPos);
            return adjState.method_26173(view, adjPos, facing.getOpposite()) != Shapes.method_1077() || adjState.method_26204() != state.method_26204() || Xray.isExposed(adjPos);
        }
        return returns;
    }

    public static boolean isExposed(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (Xray.mc.level.method_8320((BlockPos)EXPOSED_POS.get().method_25505((Vec3i)blockPos, direction)).method_26225()) continue;
            return true;
        }
        return false;
    }

    static {
        EXPOSED_POS = ThreadLocal.withInitial(BlockPos.BlockPos$MutableBlockPos::new);
    }
}

