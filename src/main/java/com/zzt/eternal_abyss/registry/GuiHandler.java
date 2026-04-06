package com.zzt.eternal_abyss.registry;

import com.zzt.eternal_abyss.container.ContainerAutoAnvil;
import com.zzt.eternal_abyss.container.ContainerRecordBag;
import com.zzt.eternal_abyss.gui.GuiAutoAnvil;
import com.zzt.eternal_abyss.gui.GuiRecordBag;
import com.zzt.eternal_abyss.tileentity.TileEntityAutoAnvil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import com.zzt.eternal_abyss.items.RecordBag;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class GuiHandler implements IGuiHandler {

    public static final int AUTO_ANVIL_GUI_ID = 0;

    public static final int RECORD_BAG_GUI_ID = 20;


    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

        if (ID == AUTO_ANVIL_GUI_ID) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityAutoAnvil) {
                return new ContainerAutoAnvil(player.inventory, (TileEntityAutoAnvil) te);
            }
            return null;
        }

        // ===== RecordBag（物品GUI）=====
        if (ID == RECORD_BAG_GUI_ID) {
            ItemStack stack = ItemStack.EMPTY;

            ItemStack main = player.getHeldItem(EnumHand.MAIN_HAND);
            ItemStack off  = player.getHeldItem(EnumHand.OFF_HAND);

            if (!main.isEmpty() && main.getItem() instanceof RecordBag) stack = main;
            else if (!off.isEmpty() && off.getItem() instanceof RecordBag) stack = off;

            if (!stack.isEmpty()) {
                return new ContainerRecordBag(player.inventory, stack);
            }
            return null;
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

        if (ID == AUTO_ANVIL_GUI_ID) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityAutoAnvil) {
                return new GuiAutoAnvil(player.inventory, (TileEntityAutoAnvil) te);
            }
            return null;
        }

        // ===== RecordBag（物品GUI）=====
        if (ID == RECORD_BAG_GUI_ID) {
            ItemStack stack = ItemStack.EMPTY;

            ItemStack main = player.getHeldItem(EnumHand.MAIN_HAND);
            ItemStack off  = player.getHeldItem(EnumHand.OFF_HAND);

            if (!main.isEmpty() && main.getItem() instanceof RecordBag) stack = main;
            else if (!off.isEmpty() && off.getItem() instanceof RecordBag) stack = off;

            if (!stack.isEmpty()) {
                return new GuiRecordBag(new ContainerRecordBag(player.inventory, stack));
            }
            return null;
        }

        return null;
    }

}
