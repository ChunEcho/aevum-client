/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.ItemStack
 *  net.minecraft.PoseStack
 */
package dev.aevum.api.events.impl;

import net.minecraft.InteractionHand;
import net.minecraft.ItemStack;
import net.minecraft.PoseStack;

public class HeldItemRendererEvent {
    private static final HeldItemRendererEvent INSTANCE = new HeldItemRendererEvent();
    private InteractionHand hand;
    private ItemStack item;
    private float ep;
    private PoseStack stack;

    private HeldItemRendererEvent() {
    }

    public static HeldItemRendererEvent get(InteractionHand hand, ItemStack item, float equipProgress, PoseStack stack) {
        HeldItemRendererEvent.INSTANCE.hand = hand;
        HeldItemRendererEvent.INSTANCE.item = item;
        HeldItemRendererEvent.INSTANCE.ep = equipProgress;
        HeldItemRendererEvent.INSTANCE.stack = stack;
        return INSTANCE;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public float getEp() {
        return this.ep;
    }

    public PoseStack getStack() {
        return this.stack;
    }
}

