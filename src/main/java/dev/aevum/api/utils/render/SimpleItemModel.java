/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.TextureAtlasSprite
 *  net.minecraft.BakedModel
 *  net.minecraft.Direction
 *  net.minecraft.BlockState
 *  net.minecraft.RandomSource
 *  net.minecraft.BakedQuad
 *  net.minecraft.ItemOverrides
 *  net.minecraft.ItemTransforms
 *  org.jetbrains.annotations.Nullable
 */
package dev.aevum.api.utils.render;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.TextureAtlasSprite;
import net.minecraft.BakedModel;
import net.minecraft.Direction;
import net.minecraft.BlockState;
import net.minecraft.RandomSource;
import net.minecraft.BakedQuad;
import net.minecraft.ItemOverrides;
import net.minecraft.ItemTransforms;
import org.jetbrains.annotations.Nullable;

public class SimpleItemModel
implements BakedModel {
    private BakedModel flattenedItem;
    private final List<BakedQuad> nullQuadList = new ArrayList<BakedQuad>();

    public void setItem(BakedModel model) {
        this.flattenedItem = model;
    }

    private boolean isCorrectDirectionForType(Direction direction) {
        return direction == Direction.SOUTH;
    }

    public List<BakedQuad> method_4707(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        if (face != null) {
            return this.isCorrectDirectionForType(face) ? this.flattenedItem.method_4707(state, face, random) : ImmutableList.of();
        }
        this.nullQuadList.clear();
        List realList = this.flattenedItem.method_4707(state, null, random);
        for (BakedQuad quad : realList) {
            if (!this.isCorrectDirectionForType(quad.getDirection())) continue;
            this.nullQuadList.add(quad);
        }
        return this.nullQuadList;
    }

    public boolean method_4708() {
        return this.flattenedItem.method_4708();
    }

    public boolean method_4712() {
        return this.flattenedItem.method_4712();
    }

    public boolean method_24304() {
        return this.flattenedItem.method_24304();
    }

    public boolean method_4713() {
        return this.flattenedItem.method_4713();
    }

    public TextureAtlasSprite method_4711() {
        return this.flattenedItem.method_4711();
    }

    public ItemTransforms method_4709() {
        return this.flattenedItem.method_4709();
    }

    public ItemOverrides method_4710() {
        return this.flattenedItem.method_4710();
    }
}

