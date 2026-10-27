/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Minecraft
 */
package dev.aevum.core;

import java.io.File;
import net.minecraft.Minecraft;

public class Manager {
    public static final Minecraft mc = Minecraft.getInstance();

    public static File getFile(String s) {
        File folder = Manager.getFolder();
        return new File(folder, s);
    }

    public static File getFolder() {
        File folder = new File(Manager.mc.gameDirectory.getPath() + File.separator + "aevum");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}

