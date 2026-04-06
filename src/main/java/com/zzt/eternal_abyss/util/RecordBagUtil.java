package com.zzt.eternal_abyss.util;

import com.zzt.eternal_abyss.inventory.RecordBagHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class RecordBagUtil {

    private static final String TAG_INV = "ca:record_bag_inv";
    private static final String TAG_BAG_ID = "ca:record_bag_id";

    public static RecordBagHandler getHandler(ItemStack bagStack) {
        RecordBagHandler handler = new RecordBagHandler();

        if (bagStack.isEmpty()) return handler;

        NBTTagCompound root = bagStack.getTagCompound();
        if (root != null && root.hasKey(TAG_INV, 10)) { // 10 = Compound
            handler.deserializeNBT(root.getCompoundTag(TAG_INV));
        }
        return handler;
    }

    public static void saveHandler(ItemStack bagStack, RecordBagHandler handler) {
        if (bagStack.isEmpty() || handler == null) return;

        NBTTagCompound root = bagStack.getTagCompound();
        if (root == null) root = new NBTTagCompound();

        root.setTag(TAG_INV, handler.serializeNBT());
        bagStack.setTagCompound(root);
    }

    /** 确保该唱片包拥有唯一ID（没有就生成并写入NBT），并返回该ID */
    public static UUID ensureBagId(ItemStack bagStack) {
        if (bagStack.isEmpty()) return null;

        NBTTagCompound tag = bagStack.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();

        if (!tag.hasUniqueId(TAG_BAG_ID)) {
            tag.setUniqueId(TAG_BAG_ID, UUID.randomUUID());
            bagStack.setTagCompound(tag);
        }
        return tag.getUniqueId(TAG_BAG_ID);
    }

    /** 读取唱片包的唯一ID（没有则返回null） */
    public static UUID getBagId(ItemStack bagStack) {
        if (bagStack.isEmpty()) return null;

        NBTTagCompound tag = bagStack.getTagCompound();
        if (tag != null && tag.hasUniqueId(TAG_BAG_ID)) {
            return tag.getUniqueId(TAG_BAG_ID);
        }
        return null;
    }
}