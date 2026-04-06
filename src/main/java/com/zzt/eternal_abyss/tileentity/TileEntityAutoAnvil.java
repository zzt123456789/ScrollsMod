package com.zzt.eternal_abyss.tileentity;

import com.zzt.eternal_abyss.util.AnvilHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TileEntityAutoAnvil extends TileEntity implements ITickable, ISidedInventory {

    private static final int INPUT_SLOT_1 = 0;
    private static final int INPUT_SLOT_2 = 1;
    private static final int OUTPUT_SLOT = 2;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
    private int anvilCost = 0;
    private int cooldown = 0;
    private UUID boundPlayerUUID = null;

    private static final int[] SLOTS_TOP = new int[]{INPUT_SLOT_1, INPUT_SLOT_2};
    private static final int[] SLOTS_BOTTOM = new int[]{OUTPUT_SLOT};

    @Override public String getName() { return "container.auto_anvil"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public int getSizeInventory() { return inventory.size(); }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStackInSlot(int index) { return inventory.get(index); }
    @Override public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(inventory, index, count); }
    @Override public ItemStack removeStackFromSlot(int index) { return ItemStackHelper.getAndRemove(inventory, index); }
    @Override public void setInventorySlotContents(int index, ItemStack stack) { inventory.set(index, stack); }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.add(0.5, 0.5, 0.5)) <= 64.0; }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return index < 2; }
    @Override public int getField(int id) { return id == 0 ? this.anvilCost : 0; }
    @Override public void setField(int id, int value) { if (id == 0) this.anvilCost = value; }
    @Override public int getFieldCount() { return 1; }
    @Override public void clear() { inventory.clear(); }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound stackTag = list.getCompoundTagAt(i);
            int slot = stackTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.inventory.size()) {
                this.inventory.set(slot, new ItemStack(stackTag));
            }
        }
        this.anvilCost = compound.getInteger("AnvilCost");
        this.cooldown = compound.getInteger("Cooldown");
        if (compound.hasUniqueId("BoundPlayerUUID")) {
            this.boundPlayerUUID = compound.getUniqueId("BoundPlayerUUID");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < this.inventory.size(); i++) {
            if (!this.inventory.get(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stackTag.setByte("Slot", (byte) i);
                this.inventory.get(i).writeToNBT(stackTag);
                list.appendTag(stackTag);
            }
        }
        compound.setTag("Items", list);
        compound.setInteger("AnvilCost", this.anvilCost);
        compound.setInteger("Cooldown", this.cooldown);
        if (this.boundPlayerUUID != null) {
            compound.setUniqueId("BoundPlayerUUID", this.boundPlayerUUID);
        }
        return compound;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack input1 = inventory.get(INPUT_SLOT_1);
        ItemStack input2 = inventory.get(INPUT_SLOT_2);
        ItemStack output = inventory.get(OUTPUT_SLOT);

        if (!input1.isEmpty() && !input2.isEmpty() && output.isEmpty()) {
            ItemStack result = AnvilHelper.getAnvilResult(input1.copy(), input2.copy());
            if (!result.isEmpty()) {
                int levelCost = AnvilHelper.getAnvilCost(input1.copy(), input2.copy());
                int xpCost = AnvilHelper.getXpCostFromLevel(levelCost);

                EntityPlayer player = getBoundPlayer();
                if (player == null || AnvilHelper.getPlayerTotalXp(player) < xpCost) return;

                AnvilHelper.removeXp(player, xpCost);
                inventory.set(OUTPUT_SLOT, result);
                input1.shrink(1);
                input2.shrink(1);
                markDirty();
                cooldown = 5;
                world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.3F, 1.0F);
            }
        }
    }

    @Override
    public int[] getSlotsForFace(net.minecraft.util.EnumFacing side) {
        return side == net.minecraft.util.EnumFacing.DOWN ? SLOTS_BOTTOM : SLOTS_TOP;
    }

    @Override public boolean canInsertItem(int index, ItemStack stack, net.minecraft.util.EnumFacing dir) {
        return index < 2;
    }

    @Override public boolean canExtractItem(int index, ItemStack stack, net.minecraft.util.EnumFacing dir) {
        return index == OUTPUT_SLOT;
    }

    public UUID getBoundPlayerUUID() { return boundPlayerUUID; }

    public void bindPlayer(EntityPlayer player) {
        this.boundPlayerUUID = player.getUniqueID(); markDirty();
    }

    @Nullable
    public EntityPlayer getBoundPlayer() {
        return (this.boundPlayerUUID == null || this.world == null || this.world.isRemote)
                ? null : this.world.getPlayerEntityByUUID(this.boundPlayerUUID);
    }
}
