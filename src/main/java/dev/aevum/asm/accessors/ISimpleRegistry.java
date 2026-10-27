/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MappedRegistry
 *  net.minecraft.Holder$Reference
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import java.util.Map;
import net.minecraft.MappedRegistry;
import net.minecraft.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MappedRegistry.class})
public interface ISimpleRegistry<T> {
    @Accessor(value="valueToEntry")
    public Map<T, Holder.Holder$Reference<T>> getValueToEntry();
}

