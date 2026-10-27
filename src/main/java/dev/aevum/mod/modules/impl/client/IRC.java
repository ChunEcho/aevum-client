/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.client;

import dev.aevum.mod.modules.Module;
import java.io.PrintWriter;

public class IRC
extends Module {
    public static PrintWriter printWriter;
    private static final String SERVER_HOST = "47.121.113.160";
    private static final int SERVER_PORT = 6667;
    public static volatile boolean connect;
    public static IRC INSTANCE;

    public IRC() {
        super("LowInputLatency", Module.Category.Client);
        INSTANCE = this;
    }

    public boolean isConnected() {
        return printWriter != null && connect;
    }

    public static void sendMark(double x, double y, double z, String type) {
        if (INSTANCE == null || !INSTANCE.isConnected() || printWriter == null) {
            return;
        }
        try {
            printWriter.println("MARK " + type + " " + (int)x + " " + (int)y + " " + (int)z);
            printWriter.flush();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

