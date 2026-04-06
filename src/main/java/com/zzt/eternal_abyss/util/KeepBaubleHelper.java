package com.zzt.eternal_abyss.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import java.util.*;

public class KeepBaubleHelper {
    // 用 UUID 绑定玩家的物品不掉落列表
    private static final Map<UUID, Set<Item>> KEEP_MAP = new HashMap<>();

    // 添加物品到不掉落列表
    public static void addKeepItem(EntityPlayer player, Item item) {
        KEEP_MAP.computeIfAbsent(player.getUniqueID(), k -> new HashSet<>()).add(item);
    }

    // 移除物品（卸下时调用）
    public static void removeKeepItem(EntityPlayer player, Item item) {
        Set<Item> set = KEEP_MAP.get(player.getUniqueID());
        if (set != null) {
            set.remove(item);
            if (set.isEmpty()) {
                KEEP_MAP.remove(player.getUniqueID());
            }
        }
    }

    // 检查是否应该保留
    public static boolean shouldKeep(EntityPlayer player, Item item) {
        Set<Item> set = KEEP_MAP.get(player.getUniqueID());
        return set != null && set.contains(item);
    }
}
