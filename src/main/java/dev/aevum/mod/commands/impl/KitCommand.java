/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.Aevum;
import dev.aevum.core.Manager;
import dev.aevum.mod.commands.Command;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KitCommand
extends Command {
    public KitCommand() {
        super("kit", "[list] | [create/delete] [name]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        switch (parameters[0]) {
            case "list": {
                if (parameters.length == 1) {
                    try {
                        for (File file : Manager.getFolder().listFiles()) {
                            if (!file.getName().endsWith(".kit")) continue;
                            String name = file.getName();
                            this.sendChatMessage("Kit: [" + name.substring(0, name.length() - 4) + "]");
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                this.sendUsage();
                return;
            }
            case "create": {
                if (parameters.length == 2) {
                    if (KitCommand.mc.player == null) {
                        return;
                    }
                    try {
                        File file = Manager.getFile(parameters[1] + ".kit");
                        PrintWriter printwriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(file), StandardCharsets.UTF_8));
                        for (int i = 0; i < 36; ++i) {
                            printwriter.println(i + ":" + KitCommand.mc.player.getInventory().method_5438(i).method_7909().method_7876());
                        }
                        printwriter.close();
                        this.sendChatMessage("\u00a7fKit [" + parameters[1] + "] created");
                    }
                    catch (Exception e) {
                        this.sendChatMessage("\u00a7fKit [" + parameters[1] + "] create failed");
                        e.printStackTrace();
                    }
                    return;
                }
                this.sendUsage();
                return;
            }
            case "delete": {
                if (parameters.length == 2) {
                    try {
                        File file = Manager.getFile(parameters[1] + ".kit");
                        if (file.exists()) {
                            file.delete();
                        }
                        this.sendChatMessage("\u00a7fKit [" + parameters[1] + "] removed");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        this.sendUsage();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            List<String> list = List.of("list", "create", "delete");
            for (String x : list) {
                if (!input.equalsIgnoreCase(Aevum.getPrefix() + "kit") && !x.toLowerCase().startsWith(input)) continue;
                correct.add(x);
            }
            int numCmds = correct.size();
            String[] commands = new String[numCmds];
            int i = 0;
            for (String x : correct) {
                commands[i++] = x;
            }
            return commands;
        }
        return null;
    }
}

