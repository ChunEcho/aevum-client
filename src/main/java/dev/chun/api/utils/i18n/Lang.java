package dev.chun.api.utils.i18n;

import dev.chun.mod.modules.impl.client.ClientSetting;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Lang {
    private static final Map<String, String> T = new HashMap<>();
    private static final Map<String, String> TL = new HashMap<>();
    private static final Map<String, String> CACHE = new HashMap<>();
    private static boolean loaded;

    public Lang() {
    }

    private static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try (InputStream in = Lang.class.getResourceAsStream("/assets/aevum/lang/map.txt")) {
            if (in == null) {
                return;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = r.readLine()) != null) {
                int i = line.indexOf('\t');
                if (i <= 0) {
                    continue;
                }
                String en = line.substring(0, i).trim();
                String zh = line.substring(i + 1).trim();
                if (en.isEmpty() || zh.isEmpty()) {
                    continue;
                }
                T.put(en, zh);
                TL.put(en.toLowerCase(), zh);
            }
        } catch (Throwable ignored) {
        }
    }

    public static boolean isChinese() {
        try {
            return ClientSetting.INSTANCE.chinese.getValue();
        } catch (Throwable t) {
            return false;
        }
    }

    public static String tr(String s) {
        if (s == null || s.isEmpty() || !isChinese() || containsCjk(s)) {
            return s;
        }
        load();
        String hit = T.get(s);
        if (hit != null) {
            return hit;
        }
        hit = TL.get(s.toLowerCase());
        if (hit != null) {
            return hit;
        }
        String cached = CACHE.get(s);
        if (cached != null) {
            return cached;
        }
        String built = translateCompound(s);
        CACHE.put(s, built);
        return built;
    }

    private static String translateCompound(String s) {
        if (!s.isEmpty() && s.charAt(0) == '\u00a7') {
            return s;
        }
        String[] parts = split(s);
        if (parts.length <= 1) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String zh = lookupWord(parts[i]);
            if (zh == null) {
                return s;
            }
            sb.append(zh);
        }
        return sb.length() > 0 ? sb.toString() : s;
    }

    private static String lookupWord(String word) {
        if (word.isEmpty()) {
            return null;
        }
        String zh = T.get(word);
        if (zh != null) {
            return zh;
        }
        zh = TL.get(word.toLowerCase());
        if (zh != null) {
            return zh;
        }
        return null;
    }

    private static String[] split(String s) {
        StringBuilder cur = new StringBuilder();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '_' || c == '-' || c == ' ' || c == '.') {
                flush(cur, out);
                continue;
            }
            if (Character.isUpperCase(c) && cur.length() > 0) {
                flush(cur, out);
            }
            cur.append(c);
        }
        flush(cur, out);
        if (out.length() == 0) {
            return new String[0];
        }
        String[] raw = out.toString().split("\u0001");
        return raw;
    }

    private static void flush(StringBuilder cur, StringBuilder out) {
        if (cur.length() == 0) {
            return;
        }
        if (out.length() > 0) {
            out.append('\u0001');
        }
        out.append(cur);
        cur.setLength(0);
    }

    public static String trEnum(Enum<?> e) {
        return e == null ? "" : tr(e.name());
    }

    public static String label(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        if (!isChinese() || containsCjk(s)) {
            return s;
        }
        load();
        String direct = T.get(s);
        if (direct != null) {
            return direct;
        }
        direct = TL.get(s.toLowerCase());
        if (direct != null) {
            return direct;
        }
        return s;
    }

    private static boolean containsCjk(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 19968 && c <= '\u9fff') {
                return true;
            }
        }
        return false;
    }

    static {
        load();
    }
}