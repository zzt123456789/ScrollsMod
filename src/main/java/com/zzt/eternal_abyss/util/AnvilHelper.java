package com.zzt.eternal_abyss.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AnvilHelper {

    public static ItemStack getAnvilResult(ItemStack input1, ItemStack input2) {
        if (input1.isEmpty() || input2.isEmpty()) return ItemStack.EMPTY;
        if (!(input1.getItem() instanceof ItemEnchantedBook) || !(input2.getItem() instanceof ItemEnchantedBook)) return ItemStack.EMPTY;

        Map<Enchantment, Integer> enchants1 = EnchantmentHelper.getEnchantments(input1);
        Map<Enchantment, Integer> enchants2 = EnchantmentHelper.getEnchantments(input2);

        if (enchants1.size() != 1 || enchants2.size() != 1) return ItemStack.EMPTY;

        Enchantment e1 = enchants1.keySet().iterator().next();
        Enchantment e2 = enchants2.keySet().iterator().next();
        int l1 = enchants1.get(e1);
        int l2 = enchants2.get(e2);

        if (e1 != e2 || l1 != l2) return ItemStack.EMPTY;

        int newLevel = l1 + 1; // ✨ 超越原版上限
        ItemStack output = new ItemStack(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> result = new HashMap<>();
        result.put(e1, newLevel);
        EnchantmentHelper.setEnchantments(result, output);
        return output;
    }

    public static int getAnvilCost(ItemStack input1, ItemStack input2) {
        ItemStack result = getAnvilResult(input1, input2);
        if (result.isEmpty()) return 0;
        return calculateEnchantmentCost(result);
    }

    private static int calculateEnchantmentCost(ItemStack stack) {
        int total = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();
            total += getRarityMultiplier(ench) * level;
        }
        return total;
    }

    private static int getRarityMultiplier(Enchantment ench) {
        switch (ench.getRarity()) {
            case COMMON: return 1;
            case UNCOMMON: return 2;
            case RARE: return 4;
            case VERY_RARE: return 8;
            default: return 1;
        }
    }

    // ---------------------------- 🔥 新增功能 ----------------------------

    /** 将等级转换为总经验值（原版公式） */
    public static int getXpCostFromLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return (int)(2.5 * level * level - 40.5 * level + 360);
        return (int)(4.5 * level * level - 162.5 * level + 2220);
    }

    /** 获取玩家当前的总经验值（真实经验点） */
    public static int getPlayerTotalXp(EntityPlayer player) {
        int level = player.experienceLevel;
        int base = getXpCostFromLevel(level);
        return base + (int)(player.experience * player.xpBarCap());
    }

    /** 扣除玩家经验值（非等级） */
    public static void removeXp(EntityPlayer player, int amount) {
        int xp = getPlayerTotalXp(player) - amount;
        xp = Math.max(0, xp);
        player.experienceLevel = 0;
        player.experienceTotal = 0;
        player.experience = 0.0F;

        while (xp > 0) {
            int next = player.xpBarCap();
            int deduct = Math.min(xp, next);
            player.addExperience(deduct);
            xp -= deduct;
        }
    }
}
