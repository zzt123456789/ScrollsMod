package com.zzt.eternal_abyss.recordbag;

import com.zzt.eternal_abyss.inventory.RecordBagHandler;
import com.zzt.eternal_abyss.items.RecordBag;
import com.zzt.eternal_abyss.util.RecordBagUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RecordBagRuntimeUtil {

    public static ItemStack findRecordBag(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() instanceof RecordBag) {
                return stack;
            }
        }

        for (ItemStack stack : player.inventory.offHandInventory) {
            if (!stack.isEmpty() && stack.getItem() instanceof RecordBag) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static Set<String> collectRecordIds(EntityPlayer player) {
        Set<String> result = new HashSet<>();

        ItemStack bag = findRecordBag(player);
        if (bag.isEmpty()) return result;

        RecordBagHandler handler = RecordBagUtil.getHandler(bag);

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem().getRegistryName() != null) {
                result.add(stack.getItem().getRegistryName().toString());
            }
        }

        return result;
    }

    public static Set<String> getActiveEffects(EntityPlayer player) {
        Set<String> recordIds = collectRecordIds(player);
        Set<String> activeEffects = new LinkedHashSet<>();

        if (recordIds.isEmpty()) return activeEffects;

        for (RecordBagEffectRegistry.Rule rule : RecordBagEffectRegistry.getRules()) {
            if (rule.isTriggered(recordIds)) {
                activeEffects.addAll(rule.effects);
            }
        }

        return activeEffects;
    }

    public static Set<RecordBagEffectRegistry.Rule> getActiveRules(EntityPlayer player) {
        Set<String> recordIds = collectRecordIds(player);
        Set<RecordBagEffectRegistry.Rule> activeRules = new LinkedHashSet<>();

        if (recordIds.isEmpty()) return activeRules;

        for (RecordBagEffectRegistry.Rule rule : RecordBagEffectRegistry.getRules()) {
            if (rule.isTriggered(recordIds)) {
                activeRules.add(rule);
            }
        }

        return activeRules;
    }
}