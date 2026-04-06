package com.zzt.eternal_abyss.inventory; // 建议放 inventory 包

import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class RecordBagHandler extends ItemStackHandler {

    public static final int SIZE = 54;

    public RecordBagHandler() {
        super(SIZE);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemRecord;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    // ⭐ 关键逻辑：禁止重复唱片
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

        if (!(stack.getItem() instanceof ItemRecord)) {
            return stack; // 不是唱片直接拒绝
        }

        // 如果已经存在相同唱片，拒绝插入
        if (containsSameRecordId(stack)) {
            return stack;
        }

        return super.insertItem(slot, stack, simulate);
    }

    public boolean containsSameRecordId(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) return false;

        String id = stack.getItem().getRegistryName().toString();

        for (int i = 0; i < getSlots(); i++) {
            ItemStack existing = getStackInSlot(i);
            if (!existing.isEmpty() && existing.getItem().getRegistryName() != null) {
                if (id.equals(existing.getItem().getRegistryName().toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}