package com.zzt.eternal_abyss.container;


import com.zzt.eternal_abyss.inventory.RecordBagHandler;
import com.zzt.eternal_abyss.util.RecordBagUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.UUID;

public class ContainerRecordBag extends Container {

    // ===== 包内槽位数 =====
    private static final int BAG_ROWS = 6;
    private static final int BAG_COLS = 9;
    private static final int BAG_SIZE = BAG_ROWS * BAG_COLS; // 54

    // ===== 玩家背包槽位数 =====
    private static final int PLAYER_INV_SIZE = 27;
    private static final int HOTBAR_SIZE = 9;

    private final ItemStack bagStack;
    private final RecordBagHandler handler;
    private final UUID bagId;

    public ContainerRecordBag(InventoryPlayer playerInv, ItemStack bagStack) {
        this.bagStack = bagStack;
        this.handler = RecordBagUtil.getHandler(bagStack);
        this.bagId = RecordBagUtil.ensureBagId(bagStack);

        // ===== 1) 唱片包 6×9（对应 generic_54）=====
        // 原版 generic_54 的槽位起点：x=8, y=18
        for (int row = 0; row < BAG_ROWS; row++) {
            for (int col = 0; col < BAG_COLS; col++) {
                int slotIndex = col + row * BAG_COLS;
                int x = 8 + col * 18;
                int y = 18 + row * 18;

                this.addSlotToContainer(new SlotItemHandler(handler, slotIndex, x, y) {

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return ContainerRecordBag.this.handler.isItemValid(this.getSlotIndex(), stack);
                    }

                    @Override
                    public int getSlotStackLimit() {
                        return 1;
                    }

                    @Override
                    public void onSlotChanged() {
                        super.onSlotChanged();
                        RecordBagUtil.saveHandler(ContainerRecordBag.this.bagStack, handler);
                    }
                });
            }
        }

        // ===== 2) 玩家背包（3×9），generic_54 对应 y=140 起 =====
        int playerInvY = 140;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                int x = 8 + col * 18;
                int y = playerInvY + row * 18;

                this.addSlotToContainer(new Slot(playerInv, index, x, y));
            }
        }

        // ===== 3) 快捷栏（1×9），generic_54 对应 y=198 =====
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            this.addSlotToContainer(new Slot(playerInv, col, x, hotbarY));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        // 玩家身上必须还存在同一个bagId的唱片包，否则关闭GUI
        return findBagWithId(playerIn, this.bagId) != ItemStack.EMPTY;
    }

    private static ItemStack findBagWithId(EntityPlayer player, UUID id) {
        if (id == null) return ItemStack.EMPTY;

        // 主背包+快捷栏
        for (ItemStack s : player.inventory.mainInventory) {
            if (isSameBag(s, id)) return s;
        }
        // 副手
        for (ItemStack s : player.inventory.offHandInventory) {
            if (isSameBag(s, id)) return s;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isSameBag(ItemStack stack, UUID id) {
        if (stack.isEmpty()) return false;
        UUID other = RecordBagUtil.getBagId(stack);
        return id.equals(other);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            ret = stack.copy();

            // index < 54：来自包内 → 送到玩家背包
            if (index < BAG_SIZE) {
                if (!this.mergeItemStack(stack, BAG_SIZE, BAG_SIZE + PLAYER_INV_SIZE + HOTBAR_SIZE, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stack.getItem() instanceof ItemRecord) {

                    // ===== 防止shift放入重复唱片 =====
                    if (handler.containsSameRecordId(stack)) {
                        return ItemStack.EMPTY;
                    }

                    if (!this.mergeItemStack(stack, 0, BAG_SIZE, false)) {
                        return ItemStack.EMPTY;
                    }

                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(playerIn, stack);
            RecordBagUtil.saveHandler(this.bagStack, this.handler);
        }

        return ret;
    }

    public ItemStack getBagStack() {
        return bagStack;
    }

    public com.zzt.eternal_abyss.inventory.RecordBagHandler getHandler() {
        return handler;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote) {
            RecordBagUtil.saveHandler(this.bagStack, this.handler);
        }
    }
}