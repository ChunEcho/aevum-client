/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod;

import dev.aevum.api.utils.Wrapper;

public class Mod
implements Wrapper {
    private final String name;

    public Mod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

