package com.zzt.eternal_abyss.client.tooltip;

import net.minecraft.item.ItemStack;

public class AltRevealState {

    private static int holdTicks = 0;
    private static String lastKey = "";

    public static void update(ItemStack hoveredStack, boolean altDown) {
        String key = makeKey(hoveredStack);

        // 切换物品时重置
        if (!key.equals(lastKey)) {
            lastKey = key;
            holdTicks = 0;
        }

        if (altDown) {
            holdTicks++;
        } else {
            holdTicks = 0;
        }
    }

    public static void reset() {
        holdTicks = 0;
        lastKey = "";
    }

    public static int getHoldTicks() {
        return holdTicks;
    }

    public static int getVisibleChars(int ticksPerChar) {
        if (ticksPerChar <= 0) ticksPerChar = 1;
        return holdTicks / ticksPerChar;
    }

    private static String makeKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "empty";
        return String.valueOf(stack.getItem().getRegistryName()) + "#" + stack.getMetadata();
    }
}