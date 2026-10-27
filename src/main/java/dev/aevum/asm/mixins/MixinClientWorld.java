/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.Level
 *  net.minecraft.Vec3
 *  net.minecraft.DimensionType
 *  net.minecraft.ProfilerFiller
 *  net.minecraft.WritableLevelData
 *  net.minecraft.DimensionSpecialEffects
 *  net.minecraft.DimensionSpecialEffects$OverworldEffects
 *  net.minecraft.ResourceKey
 *  net.minecraft.RegistryAccess
 *  net.minecraft.ClientLevel
 *  net.minecraft.Holder
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.EntitySpawnEvent;
import dev.aevum.api.events.impl.EntitySpawnedEvent;
import dev.aevum.api.events.impl.RemoveEntityEvent;
import dev.aevum.api.events.impl.TickEntityEvent;
import dev.aevum.mod.modules.impl.render.Ambience;
import dev.aevum.mod.modules.impl.render.NoRender;
import dev.aevum.mod.modules.impl.render.skybox.Skybox;
import java.awt.Color;
import java.util.function.Supplier;
import net.minecraft.Entity;
import net.minecraft.Level;
import net.minecraft.Vec3;
import net.minecraft.DimensionType;
import net.minecraft.ProfilerFiller;
import net.minecraft.WritableLevelData;
import net.minecraft.DimensionSpecialEffects;
import net.minecraft.ResourceKey;
import net.minecraft.RegistryAccess;
import net.minecraft.ClientLevel;
import net.minecraft.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientLevel.class})
public abstract class MixinClientWorld
extends Level {
    @Unique
    private final DimensionSpecialEffects overworld = new DimensionSpecialEffects.DimensionSpecialEffects$OverworldEffects();

    protected MixinClientWorld(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method={"tickEntity"}, at={@At(value="HEAD")}, cancellable=true)
    public void onTickEntity(Entity entity, CallbackInfo ci) {
        TickEntityEvent event = TickEntityEvent.get(entity);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"addEntity"}, at={@At(value="HEAD")}, cancellable=true)
    public void onAddEntity(Entity entity, CallbackInfo ci) {
        EntitySpawnEvent event = EntitySpawnEvent.get(entity);
        Aevum.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"removeEntity"}, at={@At(value="HEAD")})
    private void hookRemoveEntity(int entityId, Entity.Entity$RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = this.method_8469(entityId);
        if (entity == null) {
            return;
        }
        RemoveEntityEvent removeEntityEvent = RemoveEntityEvent.get(entity, removalReason);
        Aevum.EVENT_BUS.post(removeEntityEvent);
    }

    @Inject(method={"addEntity"}, at={@At(value="TAIL")})
    public void onAddEntityTail(Entity entity, CallbackInfo ci) {
        EntitySpawnedEvent event = EntitySpawnedEvent.get(entity);
        Aevum.EVENT_BUS.post(event);
    }

    @Inject(method={"getSkyColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetSkyColor(Vec3 cameraPos, float tickDelta, CallbackInfoReturnable<Vec3> info) {
        if (Skybox.INSTANCE != null && Skybox.INSTANCE.isOn()) {
            return;
        }
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.sky.booleanValue) {
            Color sky = Ambience.INSTANCE.sky.getValue();
            info.setReturnValue((Object)new Vec3((double)sky.getRed() / 255.0, (double)sky.getGreen() / 255.0, (double)sky.getBlue() / 255.0));
        }
    }

    @Inject(method={"getCloudsColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookGetCloudsColor(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.cloud.booleanValue) {
            Color sky = Ambience.INSTANCE.cloud.getValue();
            cir.setReturnValue((Object)new Vec3((double)sky.getRed() / 255.0, (double)sky.getGreen() / 255.0, (double)sky.getBlue() / 255.0));
        }
    }

    public float method_8430(float delta) {
        return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0.0f : super.method_8430(delta);
    }

    public float method_8478(float delta) {
        return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0.0f : super.method_8478(delta);
    }
}

