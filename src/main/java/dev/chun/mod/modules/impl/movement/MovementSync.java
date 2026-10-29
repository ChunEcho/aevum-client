/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.mod.modules.impl.movement;

import dev.chun.mod.modules.Module;

public class MovementSync
extends Module {
    public static MovementSync INSTANCE;

    public MovementSync() {
        super("MovementSync", Module.Category.Movement);
        this.setChinese("\u79fb\u52a8\u540c\u6b65");
        INSTANCE = this;
    }
}

