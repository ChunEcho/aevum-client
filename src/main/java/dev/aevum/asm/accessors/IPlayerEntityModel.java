/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerModel
 *  net.minecraft.ModelPart
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import net.minecraft.PlayerModel;
import net.minecraft.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PlayerModel.class})
public interface IPlayerEntityModel {
    @Accessor(value="leftPants")
    public ModelPart getLeftPants();

    @Accessor(value="rightPants")
    public ModelPart getRightPants();

    @Accessor(value="leftSleeve")
    public ModelPart getLeftSleeve();

    @Accessor(value="rightSleeve")
    public ModelPart getRightSleeve();

    @Accessor(value="jacket")
    public ModelPart getJacket();
}

