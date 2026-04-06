package com.zzt.eternal_abyss.recordbag;

import com.zzt.eternal_abyss.config.ModConfig;

import java.util.*;

public class RecordBagEffectRegistry {

    public static class Rule {

        public String name;

        // 指定唱片
        public Set<String> requiredRecords = new HashSet<>();

        // any(n)
        public int anyCount = 0;

        // 支持三类：
        // special:xxx@...
        // potion:xxx@...
        // attribute:xxx@...
        public List<String> effects = new ArrayList<>();

        public String tooltip = "";

        public boolean isTriggered(Set<String> records) {
            if (anyCount > 0) {
                return records.size() >= anyCount;
            }

            return records.containsAll(requiredRecords);
        }
    }

    private static final List<Rule> RULES = new ArrayList<>();

    public static void reload() {
        RULES.clear();

        for (String line : ModConfig.RECORD_BAG_RULES) {
            Rule rule = parseRule(line);

            // 过滤空规则
            if (rule != null && !rule.effects.isEmpty()) {
                RULES.add(rule);
            }
        }

        System.out.println("[RecordBag] Loaded rules: " + RULES.size());
    }

    public static List<Rule> getRules() {
        return RULES;
    }

    private static Rule parseRule(String line) {
        if (line == null) return null;

        line = line.trim();
        if (line.isEmpty()) return null;

        int eq = line.indexOf('=');
        if (eq <= 0) return null;

        String name = line.substring(0, eq).trim();
        String body = line.substring(eq + 1).trim();

        Rule rule = new Rule();
        rule.name = name;

        String[] parts = body.split("\\|");

        for (String part : parts) {
            part = part.trim();

            // required
            if (part.startsWith("required:") || part.startsWith("required=")) {

                String req;
                if (part.startsWith("required:")) {
                    req = part.substring("required:".length()).trim();
                } else {
                    req = part.substring("required=".length()).trim();
                }

                if (req.startsWith("any(")) {
                    int start = req.indexOf('(');
                    int end = req.indexOf(')');

                    if (start >= 0 && end > start) {
                        String num = req.substring(start + 1, end).trim();
                        try {
                            rule.anyCount = Integer.parseInt(num);
                        } catch (NumberFormatException ignored) {
                            rule.anyCount = 0;
                        }
                    }
                } else {
                    String[] list = req.split(",");
                    for (String r : list) {
                        String id = r.trim();
                        if (!id.isEmpty()) {
                            rule.requiredRecords.add(id);
                        }
                    }
                }
            }

            // effects
            else if (part.startsWith("effects=") || part.startsWith("effects:")) {

                String eff;
                if (part.startsWith("effects=")) {
                    eff = part.substring("effects=".length()).trim();
                } else {
                    eff = part.substring("effects:".length()).trim();
                }

                String[] list = eff.split(";");
                for (String e : list) {
                    String effect = e.trim();
                    if (!effect.isEmpty()) {
                        rule.effects.add(effect);
                    }
                }
            }

            // tooltip
            else if (part.startsWith("tooltip=") || part.startsWith("tooltip:")) {

                if (part.startsWith("tooltip=")) {
                    rule.tooltip = part.substring("tooltip=".length()).trim();
                } else {
                    rule.tooltip = part.substring("tooltip:".length()).trim();
                }
            }
        }

        return rule;
    }

    // =========================
    // 三类效果判断
    // =========================

    public static boolean isSpecialEffect(String effect) {
        return effect != null && effect.startsWith("special:");
    }

    public static boolean isPotionEffect(String effect) {
        return effect != null && effect.startsWith("potion:");
    }

    public static boolean isAttributeEffect(String effect) {
        return effect != null && effect.startsWith("attribute:");
    }

    // =========================
    // 取掉前缀后的主体内容
    // =========================

    public static String getSpecialBody(String effect) {
        return isSpecialEffect(effect) ? effect.substring("special:".length()).trim() : "";
    }

    public static String getPotionBody(String effect) {
        return isPotionEffect(effect) ? effect.substring("potion:".length()).trim() : "";
    }

    public static String getAttributeBody(String effect) {
        return isAttributeEffect(effect) ? effect.substring("attribute:".length()).trim() : "";
    }

    // =========================
    // special 解析工具
    // 例：
    // special:damage_bonus@0.5
    // special:on_hit_potion@minecraft:wither@100@1@0.25
    // special:nearby_debuff@minecraft:slowness@60@1@6.0
    // =========================

    /**
     * 获取 special 的类型名
     * 例如：
     * special:damage_bonus@0.5 -> damage_bonus
     * special:on_hit_potion@minecraft:wither@100@1@0.25 -> on_hit_potion
     */
    public static String getSpecialType(String effect) {
        String body = getSpecialBody(effect);
        if (body.isEmpty()) return "";

        int at = body.indexOf('@');
        return at >= 0 ? body.substring(0, at).trim() : body.trim();
    }

    /**
     * 获取 special 的参数数组（不含 type）
     * 例如：
     * special:on_hit_potion@minecraft:wither@100@1@0.25
     * 返回：
     * [minecraft:wither, 100, 1, 0.25]
     */
    public static String[] getSpecialArgs(String effect) {
        String body = getSpecialBody(effect);
        if (body.isEmpty()) return new String[0];

        String[] parts = body.split("@");
        if (parts.length <= 1) return new String[0];

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }

    /**
     * 判断是否为指定 special 类型
     */
    public static boolean isSpecialType(String effect, String type) {
        return isSpecialEffect(effect) && type != null && type.equals(getSpecialType(effect));
    }

    /**
     * 获取 special 指定位置参数
     * index=0 表示第一个 @ 后面的参数
     */
    public static String getSpecialArg(String effect, int index) {
        String[] args = getSpecialArgs(effect);
        return index >= 0 && index < args.length ? args[index].trim() : "";
    }

    public static int getSpecialArgInt(String effect, int index, int def) {
        try {
            return Integer.parseInt(getSpecialArg(effect, index));
        } catch (Exception e) {
            return def;
        }
    }

    public static double getSpecialArgDouble(String effect, int index, double def) {
        try {
            return Double.parseDouble(getSpecialArg(effect, index));
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean getSpecialArgBoolean(String effect, int index, boolean def) {
        try {
            String s = getSpecialArg(effect, index);
            return s.isEmpty() ? def : Boolean.parseBoolean(s);
        } catch (Exception e) {
            return def;
        }
    }
}