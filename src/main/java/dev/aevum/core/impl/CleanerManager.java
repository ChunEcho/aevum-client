/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Items
 *  org.apache.commons.io.IOUtils
 */
package dev.aevum.core.impl;

import dev.aevum.core.Manager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Items;
import org.apache.commons.io.IOUtils;

public class CleanerManager
extends Manager {
    private final ArrayList<String> list = new ArrayList();

    public CleanerManager() {
        this.read();
    }

    public ArrayList<String> getList() {
        return this.list;
    }

    public boolean inList(String name) {
        return this.list.contains(name) || this.list.contains(name.replace("block.minecraft.", "").replace("item.minecraft.", ""));
    }

    public void clear() {
        this.list.clear();
    }

    public void remove(String name) {
        name = name.replace("block.minecraft.", "").replace("item.minecraft.", "");
        this.list.remove(name);
    }

    public void add(String name) {
        if (!this.list.contains(name = name.replace("block.minecraft.", "").replace("item.minecraft.", ""))) {
            this.list.add(name);
        }
    }

    public void read() {
        try {
            File friendFile = CleanerManager.getFile("cleaner.txt");
            if (!friendFile.exists()) {
                this.add(Items.NETHERITE_SWORD.method_7876());
                this.add(Items.NETHERITE_PICKAXE.method_7876());
                this.add(Items.NETHERITE_HELMET.method_7876());
                this.add(Items.NETHERITE_CHESTPLATE.method_7876());
                this.add(Items.NETHERITE_LEGGINGS.method_7876());
                this.add(Items.NETHERITE_BOOTS.method_7876());
                this.add(Items.OBSIDIAN.method_7876());
                this.add(Items.ENDER_CHEST.method_7876());
                this.add(Items.ENDER_PEARL.method_7876());
                this.add(Items.ENCHANTED_GOLDEN_APPLE.method_7876());
                this.add(Items.EXPERIENCE_BOTTLE.method_7876());
                this.add(Items.COBWEB.method_7876());
                this.add(Items.POTION.method_7876());
                this.add(Items.SPLASH_POTION.method_7876());
                this.add(Items.TOTEM_OF_UNDYING.method_7876());
                this.add(Items.END_CRYSTAL.method_7876());
                this.add(Items.ELYTRA.method_7876());
                this.add(Items.FLINT_AND_STEEL.method_7876());
                this.add(Items.PISTON.method_7876());
                this.add(Items.STICKY_PISTON.method_7876());
                this.add(Items.REDSTONE_BLOCK.method_7876());
                this.add(Items.GLOWSTONE.method_7876());
                this.add(Items.RESPAWN_ANCHOR.method_7876());
                this.add(Items.ANVIL.method_7876());
                return;
            }
            List list = IOUtils.readLines((InputStream)new FileInputStream(friendFile), (Charset)StandardCharsets.UTF_8);
            for (String s : list) {
                this.add(s);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            File friendFile = CleanerManager.getFile("cleaner.txt");
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : this.list) {
                printwriter.println(str);
            }
            printwriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

