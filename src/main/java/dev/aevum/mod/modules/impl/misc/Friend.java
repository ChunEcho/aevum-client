/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.HitResult
 *  net.minecraft.EntityHitResult
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.mod.modules.Module;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.HitResult;
import net.minecraft.EntityHitResult;

public class Friend
extends Module {
    public static Friend INSTANCE;

    public Friend() {
        super("Friend", Module.Category.Misc);
        this.setChinese("\u597d\u53cb");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        EntityHitResult entityHitResult;
        Entity entity;
        if (Friend.nullCheck()) {
            this.disable();
            return;
        }
        HitResult target = Friend.mc.hitResult;
        if (target instanceof EntityHitResult && (entity = (entityHitResult = (EntityHitResult)target).getEntity()) instanceof Player) {
            Player player = (Player)entity;
            Aevum.FRIEND.friend(player);
        }
        this.disable();
    }
}

