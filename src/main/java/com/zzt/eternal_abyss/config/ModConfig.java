package com.zzt.eternal_abyss.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.util.*;

public class ModConfig {
        static Configuration config;

        public static Configuration getConfig() {
                return config;
        }

        public static double expAttractRange = 10.0;
        public static double expAttractRangeBoosted = 15.0;

        public static double hauntedAggroRangeBonus = 8.0; // 默认增加8格

        public static List<String> swordQiBlacklist = new ArrayList<>();

        public static double CloverDropChance = 0.05;
        public static boolean giveRingOnLogin = false;

        // 深渊之戒：未佩戴多长时间（分钟）后禁止再次佩戴，0 = 禁用
        public static int depthRingUnequipTimeoutMinutes = 1;

        // ===== 暴击 Softmin 曲线参数 =====
        public static double critSoftA = 1.0; // a：软上限强度
        public static double critSoftN = 2.0; // N：多项式阶数
        public static double critSoftK = 4.0; // k：平滑度

        // ===== 苦痛压制（攻击/攻速）数值 =====
        public static double damageCurseMultiplier = 0.80D;
        public static double damageReversedMultiplier = 1.50D;

        public static double attackSpeedCurse = -0.20D;
        public static double attackSpeedReversed = 0.40D;
        // ===== 经验诅咒（EXPERIENCE_DROP）=====
        public static double expCurseMultiplier = 0.5D;

        public static double expReversedChanceX4 = 0.20D;
        public static double expReversedChanceX2 = 0.40D;

        public static int expReversedMultiplierX4 = 4;
        public static int expReversedMultiplierX2 = 2;
        // ===== 怨影缠身 =====
        public static double hauntedMoveSpeedCurse = -0.20D;
        public static double hauntedMoveSpeedReversed = 0.80D;

        public static double hauntedArrowSpeedFactor = 100.0D;
        public static double hauntedArrowPercent = 0.5D;
        public static double hauntedArrowBonusRate = 0.4D;

        // ===== 虚空腐蚀（VOID_RESISTANCE）=====

        // 未反转：最低生命值伤害比例（默认 20%）
        public static double voidBaseHealthPercent = 0.20D;

        // 已反转：基础减伤
        public static double voidReversedBaseReduction = 0.05D;

        // 已反转：每个反转诅咒提供的额外减伤
        public static double voidReversedReductionPerCurse = 0.04D;

        // 已反转：每个反转诅咒提供的闪避概率
        public static double voidReversedDodgePerCurse = 0.02D;

        // ========= 唱片包效果 ==========
        public static String[] RECORD_BAG_RULES = new String[0];

        // 每个诅咒对应的信息
        public static class CurseConfig {
                public List<String> targets;
                public int requiredKills;

                public CurseConfig(List<String> targets, int requiredKills) {
                        this.targets = targets;
                        this.requiredKills = requiredKills;
                }
        }

        // 所有诅咒的配置，按类型存放
        public static Map<String, CurseConfig> curseConfigs = new HashMap<>();

        public static void init(File configFile) {
                if (config == null) {
                        config = new Configuration(configFile);
                        loadConfig();
                }
        }

        private static void loadConfig() {
                expAttractRange = config.getFloat(
                                "ExpAttractRange",
                                Configuration.CATEGORY_GENERAL,
                                10.0F,
                                1.0F,
                                64.0F,
                                "经验吸收饰品的基础吸收范围(调整需重启游戏)");

                CloverDropChance = config.getFloat(
                                "CloverDropChance",
                                Configuration.CATEGORY_GENERAL,
                                0.05F,
                                0.0F,
                                1.0F,
                                "掉落四叶草的概率");

                expAttractRangeBoosted = config.getFloat(
                                "ExpAttractRangeBoosted",
                                Configuration.CATEGORY_GENERAL,
                                15.0F,
                                1.0F,
                                64.0F,
                                "经验吸收饰品的祝福（反转）状态吸收范围(调整需重启游戏)");

                hauntedAggroRangeBonus = config.getFloat(
                                "HauntedAggroRangeBonus",
                                "HauntedShadows",
                                8.0F,
                                1.0F,
                                64.0F,
                                "未反转时，怨影缠身诅咒增加的怪物感知距离（单位：格）");

                giveRingOnLogin = config.getBoolean(
                                "GiveDepthRingOnLogin",
                                Configuration.CATEGORY_GENERAL,
                                true,
                                "是否在玩家第一次登录时发放深渊之戒（需要重启游戏）");

                depthRingUnequipTimeoutMinutes = config.getInt(
                                "DepthRingUnequipTimeoutMinutes",
                                Configuration.CATEGORY_GENERAL,
                                10,
                                0,
                                10080, // 最大7天
                                "深渊之戒未佩戴多长时间（分钟）后禁止再次佩戴。设为 0 则禁用此限制。");

                // 攻击力和攻速诅咒设置
                String[] damageSpeedTargets = config.getStringList(
                                "CurseTarget.DamageSpeed",
                                "DamageSpeed",
                                new String[] { "minecraft:zombie" },
                                "反转攻击力与攻速的击杀目标实体ID(调整需重启游戏)");
                int damageSpeedKills = config.getInt(
                                "CurseKillCount.DamageSpeed",
                                "DamageSpeed",
                                1,
                                1,
                                1000,
                                "攻击力与攻速诅咒反转所需击杀数");

                // 经验掉落诅咒设置
                String[] expDropTargets = config.getStringList(
                                "CurseTarget.ExperienceDrop",
                                "ExperienceDrop",
                                new String[] { "minecraft:zombie" },
                                "反转经验掉落的击杀目标实体ID(调整需重启游戏)");
                int expDropKills = config.getInt(
                                "CurseKillCount.ExperienceDrop",
                                "ExperienceDrop",
                                1,
                                1,
                                1000,
                                "经验掉落诅咒反转所需击杀数");

                // 怨影缠身诅咒设置
                String[] hauntedTargets = config.getStringList(
                                "CurseTarget.HauntedShadows",
                                "HauntedShadows",
                                new String[] { "minecraft:zombie" },
                                "反转怨影缠身诅咒的击杀目标实体ID(调整需重启游戏)");
                int hauntedKills = config.getInt(
                                "CurseKillCount.HauntedShadows",
                                "HauntedShadows",
                                1,
                                1,
                                1000,
                                "怨影缠身诅咒反转所需击杀数");

                // 虚空抗性诅咒设置
                String[] voidResistTargets = config.getStringList(
                                "CurseTarget.VoidResistance",
                                "VoidResistance",
                                new String[] { "minecraft:zombie" },
                                "反转虚空抗性诅咒的击杀目标实体ID(调整需重启游戏)");
                int voidResistKills = config.getInt(
                                "CurseKillCount.VoidResistance",
                                "VoidResistance",
                                1,
                                1,
                                1000,
                                "虚空抗性诅咒反转所需击杀数");

                // 最终目标
                String[] finalTargets = config.getStringList(
                                "CurseTarget.Final",
                                "Final",
                                new String[] { "minecraft:zombie" },
                                "最终目标实体id");
                int finalCount = config.getInt(
                                "CurseCount.Final",
                                "Final",
                                1,
                                1,
                                1000,
                                "最终目标反转所需击杀数");

                String[] luckyTargets = config.getStringList(
                                "CurseTarget.Lucky",
                                "Lucky",
                                new String[] { "minecraft:apple" },
                                "反转幸运诅咒的使用物品ID(此条目无法调整！！！)");
                int luckyCount = config.getInt(
                                "CurseCount.Lucky",
                                "Lucky",
                                1,
                                1,
                                1000,
                                "幸运诅咒反转所需使用物品数");

                String[] swordQiBlacklistArray = config.getStringList(
                                "SwordQiBlacklist",
                                Configuration.CATEGORY_GENERAL,
                                new String[] { "minecraft:villager", "minecraft:player", "minecraft:armor_stand" },
                                "剑气攻击的实体黑名单，填实体 ID");
                giveRingOnLogin = config.getBoolean(
                                "GiveDepthRingOnLogin",
                                Configuration.CATEGORY_GENERAL,
                                false,
                                "是否在玩家第一次登录时发放深渊之戒（需要重启游戏）");

                // ===== 暴击 Softmin 曲线参数 =====
                critSoftA = config.getFloat(
                                "CritSoftA",
                                "crit",
                                1.0F,
                                0.1F,
                                10.0F,
                                "暴击倍率软上限强度（a）\n"
                                                + "决定【暴击层数 n】大致在哪一层开始不再指数增长\n"
                                                + "经验法则：n ≈ sqrt(目标倍率 / a)\n"
                                                + "示例：a=0.25 → n=7 时 ≈13倍；a=5 → n=7 时 ≈1700倍");

                critSoftN = config.getFloat(
                                "CritSoftN",
                                "crit",
                                2.0F,
                                1.0F,
                                4.0F,
                                "暴击倍率后期增长阶数（N）\n"
                                                + "决定软上限 a·n^N 的增长速度\n"
                                                + "1 = 后期几乎不涨（强力防膨胀）\n"
                                                + "2 = 平方增长（推荐，稳定可控）\n"
                                                + "3 = 后期仍有明显成长（偏后期/毕业向）");

                critSoftK = config.getFloat(
                                "CritSoftK",
                                "crit",
                                4.0F,
                                1.0F,
                                10.0F,
                                "暴击软上限过渡硬度（K）\n"
                                                + "决定从指数增长到软上限的过渡方式\n"
                                                + "K 小：增长逐渐变慢（手感软，推荐 PvE）\n"
                                                + "K 大：接近突然封顶（手感硬，类似硬上限）\n"
                                                + "推荐范围：2.0~3.0 为平滑过渡");

                // ===== 苦痛压制：伤害倍率 =====
                damageCurseMultiplier = config.getFloat(
                                "CurseDamageMultiplier",
                                "DamageSpeed",
                                0.80F,
                                0.0F,
                                5.0F,
                                "苦痛压制未反转时的最终伤害倍率");

                damageReversedMultiplier = config.getFloat(
                                "ReversedDamageMultiplier",
                                "DamageSpeed",
                                1.50F,
                                1.0F,
                                10.0F,
                                "苦痛压制反转后的最终伤害倍率");

                attackSpeedCurse = config.getFloat(
                                "CurseAttackSpeed",
                                "DamageSpeed",
                                -0.20F,
                                -5.0F,
                                0.0F,
                                "苦痛压制未反转时的攻击速度惩罚");

                attackSpeedReversed = config.getFloat(
                                "ReversedAttackSpeed",
                                "DamageSpeed",
                                0.40F,
                                0.0F,
                                5.0F,
                                "苦痛压制反转后的攻击速度加成");

                // ===== 经验诅咒：未反转 =====
                expCurseMultiplier = config.getFloat(
                                "CurseMultiplier",
                                "ExperienceDrop",
                                0.5F,
                                0.0F,
                                1.0F,
                                "经验诅咒未反转时的经验倍率");

                expReversedChanceX4 = config.getFloat(
                                "ReversedChanceX4",
                                "ExperienceDrop",
                                0.20F,
                                0.0F,
                                1.0F,
                                "反转后 ×4 经验概率");

                expReversedChanceX2 = config.getFloat(
                                "ReversedChanceX2",
                                "ExperienceDrop",
                                0.40F,
                                0.0F,
                                1.0F,
                                "反转后 ×2 经验概率");

                expReversedMultiplierX4 = config.getInt(
                                "ReversedMultiplierX4",
                                "ExperienceDrop",
                                4,
                                1,
                                100,
                                "反转后 ×4 经验倍率");

                expReversedMultiplierX2 = config.getInt(
                                "ReversedMultiplierX2",
                                "ExperienceDrop",
                                2,
                                1,
                                100,
                                "反转后 ×2 经验倍率");

                // ===== 怨影缠身：移动速度 =====
                hauntedMoveSpeedCurse = config.getFloat(
                                "CurseMoveSpeed",
                                "HauntedShadows",
                                -0.20F,
                                -1.0F,
                                0.0F,
                                "未反转时移动速度惩罚");

                hauntedMoveSpeedReversed = config.getFloat(
                                "ReversedMoveSpeed",
                                "HauntedShadows",
                                0.80F,
                                0.0F,
                                5.0F,
                                "反转后的移动速度加成");

                hauntedArrowSpeedFactor = config.getFloat(
                                "ArrowSpeedFactor",
                                "HauntedShadows",
                                100.0F,
                                0.0F,
                                1000.0F,
                                "箭矢速度基础倍率");

                hauntedArrowPercent = config.getFloat(
                                "ArrowPercent",
                                "HauntedShadows",
                                0.5F,
                                0.0F,
                                5.0F,
                                "箭矢速度参与比例");

                hauntedArrowBonusRate = config.getFloat(
                                "ArrowBonusRate",
                                "HauntedShadows",
                                0.4F,
                                0.0F,
                                5.0F,
                                "箭矢最终伤害倍率");

                // ===== 虚空腐蚀：未反转 =====
                voidBaseHealthPercent = config.getFloat(
                                "BaseHealthPercent",
                                "VoidResistance",
                                0.20F,
                                0.01F,
                                1.0F,
                                "未反转时最低伤害比例");

                voidReversedBaseReduction = config.getFloat(
                                "ReversedBaseReduction",
                                "VoidResistance",
                                0.05F,
                                0.0F,
                                1.0F,
                                "反转后的基础减伤");

                voidReversedReductionPerCurse = config.getFloat(
                                "ReversedReductionPerCurse",
                                "VoidResistance",
                                0.04F,
                                0.0F,
                                1.0F,
                                "每反转一个诅咒获得的减伤");

                voidReversedDodgePerCurse = config.getFloat(
                                "ReversedDodgePerCurse",
                                "VoidResistance",
                                0.02F,
                                0.0F,
                                1.0F,
                                "每反转一个诅咒获得的闪避");

                RECORD_BAG_RULES = config.getStringList(
                        "record_rules",
                        "record_bag",
                        new String[] {
                                "test_damage=required:any(1)|effects=special:damage_bonus@0.50|tooltip=增伤50%",
                                "test_reduce=required:any(2)|effects=special:damage_reduction@0.30|tooltip=减伤30%",
                                "test_double=required:any(3)|effects=special:double_damage_chance@0.25|tooltip=25%概率双倍伤害",
                                "test_speed=required:any(4)|effects=potion:minecraft:speed@40@1@true@false|tooltip=速度 II",
                                "test_xp=required:any(5)|effects=special:xp_bonus@0.50|tooltip=经验获取提升50%",
                                "test_hit_wither=required:any(6)|effects=special:on_hit_potion@minecraft:wither@100@1@0.25|tooltip=攻击有概率附加凋零",
                                "test_near_slow=required:any(7)|effects=special:nearby_debuff@minecraft:slowness@60@1@6.0|tooltip=使附近敌人减速",
                                "test_crit=required:minecraft:record_far,minecraft:record_11|effects=attribute:eternal_abyss.crit_chance@2@0.25|tooltip=增加25%暴击率",
                                "test_critdmg=required:minecraft:record_far,minecraft:record_11|effects=attribute:eternal_abyss.crit_damage@2@0.25|tooltip=增加25%暴击伤害"
                        },
                        "唱片包规则配置。\n"
                                + "格式：ruleName=required:条件|effects=效果1;效果2;...|tooltip=提示文本\n"
                                + "\n"
                                + "一、required 写法：\n"
                                + "1. 指定唱片：required=modid:record_a,modid:record_b\n"
                                + "   表示必须同时拥有这些唱片才触发。\n"
                                + "2. 任意数量：required=any(n)\n"
                                + "   表示收纳的不同唱片数量达到 n 张即可触发。\n"
                                + "\n"
                                + "二、effects 支持三类效果，可用分号 ; 连接多个效果：\n"
                                + "\n"
                                + "1. special 特殊效果：\n"
                                + "   1) special:damage_bonus@数值\n"
                                + "      例：special:damage_bonus@0.50\n"
                                + "      说明：造成伤害提高 50%。\n"
                                + "\n"
                                + "   2) special:damage_reduction@数值\n"
                                + "      例：special:damage_reduction@0.30\n"
                                + "      说明：受到伤害降低 30%。\n"
                                + "\n"
                                + "   3) special:double_damage_chance@概率\n"
                                + "      例：special:double_damage_chance@0.25\n"
                                + "      说明：25% 概率造成双倍伤害。\n"
                                + "\n"
                                + "   4) special:xp_bonus@数值\n"
                                + "      例：special:xp_bonus@0.50\n"
                                + "      说明：击杀生物获得经验提高 50%。\n"
                                + "\n"
                                + "   5) special:on_hit_potion@药水ID@持续时间@等级@概率\n"
                                + "      例：special:on_hit_potion@minecraft:wither@100@1@0.25\n"
                                + "      说明：攻击时有 25% 概率附加凋零 II，持续 100 tick。\n"
                                + "\n"
                                + "   6) special:nearby_debuff@药水ID@持续时间@等级@范围\n"
                                + "      例：special:nearby_debuff@minecraft:slowness@60@1@6.0\n"
                                + "      说明：对周围 6 格内生物附加缓慢 II，持续 60 tick。\n"
                                + "\n"
                                + "2. potion 药水效果：\n"
                                + "   格式：potion:modid:potion_name@duration@amplifier@ambient@particles\n"
                                + "   例：potion:minecraft:speed@40@1@true@false\n"
                                + "   说明：duration 为持续时间（tick），amplifier 为等级（0=I，1=II）\n"
                                + "   ambient 表示是否为环境效果，particles 表示是否显示粒子。\n"
                                + "   支持原版和其他 Mod 已注册的药水效果。\n"
                                + "\n"
                                + "3. attribute 属性效果：\n"
                                + "   格式：attribute:属性名@operation@amount\n"
                                + "   例：attribute:generic.attackDamage@0@4.0\n"
                                + "   例：attribute:eternal_abyss.crit_chance@2@0.25\n"
                                + "   operation：0=直接加值，1=基础值倍率加成，2=最终乘算\n"
                                + "   注意：百分比效果通常建议写成 0.25 表示 25%。\n"
                                + "\n"
                                + "三、tooltip：\n"
                                + "用于在物品提示中显示该规则说明。\n"
                                + "\n"
                                + "四、补充说明：\n"
                                + "1. 多个效果可用分号 ; 连接。\n"
                                + "2. 药水与特殊效果中的药水ID必须是已注册药水，例如 minecraft:speed。\n"
                                + "3. nearby_debuff 的目标过滤与黑名单逻辑由代码控制。\n"
                                + "\n"
                                + "五、完整示例：\n"
                                + "test_damage=required:any(1)|effects=special:damage_bonus@0.50|tooltip=增伤50%\n"
                                + "test_speed=required:any(4)|effects=potion:minecraft:speed@40@1@true@false|tooltip=速度 II\n"
                                + "test_xp=required:any(5)|effects=special:xp_bonus@0.50|tooltip=经验获取提升50%\n"
                                + "test_hit_wither=required:any(6)|effects=special:on_hit_potion@minecraft:wither@100@1@0.25|tooltip=攻击有概率附加凋零\n"
                                + "test_near_slow=required:any(7)|effects=special:nearby_debuff@minecraft:slowness@60@1@6.0|tooltip=使附近敌人减速\n"
                                + "test_crit=required:minecraft:record_far,minecraft:record_11|effects=attribute:eternal_abyss.crit_chance@2@0.25|tooltip=增加25%暴击率"
                );



                swordQiBlacklist = Arrays.asList(swordQiBlacklistArray);

                // 添加到 map 中
                curseConfigs.put("DAMAGE_SPEED", new CurseConfig(Arrays.asList(damageSpeedTargets), damageSpeedKills));
                curseConfigs.put("EXPERIENCE_DROP", new CurseConfig(Arrays.asList(expDropTargets), expDropKills));
                curseConfigs.put("HAUNTED_SHADOWS", new CurseConfig(Arrays.asList(hauntedTargets), hauntedKills));
                curseConfigs.put("VOID_RESISTANCE", new CurseConfig(Arrays.asList(voidResistTargets), voidResistKills));
                curseConfigs.put("LUCKY", new CurseConfig(Arrays.asList(luckyTargets), luckyCount));
                curseConfigs.put("FINAL", new CurseConfig(Arrays.asList(finalTargets), finalCount));

                if (config.hasChanged()) {
                        config.save();
                }
        }

        public static CurseConfig getCurseConfig(String curseKey) {
                return curseConfigs.get(curseKey);
        }
}
