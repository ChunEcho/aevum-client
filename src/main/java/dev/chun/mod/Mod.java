/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.mod;

import dev.chun.api.utils.Wrapper;

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

