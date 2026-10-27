/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.api.utils.i18n;

import dev.aevum.mod.modules.impl.client.ClientSetting;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Lang {
    private static final Map<String, String> T = new HashMap<String, String>();
    private static final Map<String, String> TL = new HashMap<String, String>();

    private static void load() {
        try (InputStream in = Lang.class.getResourceAsStream("/assets/aevum/lang/map.txt");){
            String line;
            if (in == null) {
                return;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            while ((line = r.readLine()) != null) {
                int i = line.indexOf(9);
                if (i <= 0) continue;
                String en = line.substring(0, i).trim();
                String zh = line.substring(i + 1).trim();
                if (en.isEmpty() || zh.isEmpty()) continue;
                T.put(en, zh);
                TL.put(en.toLowerCase(), zh);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static boolean isChinese() {
        try {
            return ClientSetting.INSTANCE.chinese.getValue();
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static String tr(String s) {
        if (s == null || s.isEmpty() || !Lang.isChinese()) {
            return s;
        }
        if (Lang.containsCjk(s)) {
            return s;
        }
        String v = T.get(s);
        if (v != null) {
            return v;
        }
        v = TL.get(s.toLowerCase());
        if (v != null) {
            return v;
        }
        return s;
    }

    public static String trEnum(Enum<?> e) {
        return e == null ? "" : Lang.tr(e.name());
    }

    private static boolean containsCjk(String s) {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c < '\u4e00' || c > '\u9fff') continue;
            return true;
        }
        return false;
    }

    static {
        Lang.load();
    }
}

