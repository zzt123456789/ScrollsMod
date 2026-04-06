package com.zzt.eternal_abyss.container;

import com.zzt.eternal_abyss.tileentity.TileEntityAutoAnvil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;

public class ContainerAutoAnvil extends Container {

    private final TileEntityAutoAnvil tileAutoAnvil;

    public ContainerAutoAnvil(InventoryPlayer playerInventory, TileEntityAutoAnvil tileEntity) {
        this.tileAutoAnvil = tileEntity;

        // 输入槽1（只允许附魔书）
        this.addSlotToContainer(new Slot(tileEntity, 0, 27, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemEnchantedBook;
            }
        });

        // 输入槽2（只允许附魔书）
        this.addSlotToContainer(new Slot(tileEntity, 1, 76, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemEnchantedBook;
            }
        });

        // 输出槽（不允许手动放物品）
        this.addSlotToContainer(new Slot(tileEntity, 2, 134, 47) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // 玩家背包栏 (标准布局)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 玩家快捷栏
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tileAutoAnvil.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 3) {
                // 从自动铁砧槽位取出，放回玩家背包
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家背包拿物品到自动铁砧
                if (itemstack1.getItem() instanceof ItemEnchantedBook) {
                    // 放到输入槽0或1
                    if (!this.mergeItemStack(itemstack1, 0, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY; // ❗不是附魔书，禁止放入
                }
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

}
