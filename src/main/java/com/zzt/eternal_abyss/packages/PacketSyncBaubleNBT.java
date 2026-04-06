package com.zzt.eternal_abyss.packages;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSyncBaubleNBT implements IMessage {

    private int slot;
    private NBTTagCompound nbt;

    public PacketSyncBaubleNBT() {}

    public PacketSyncBaubleNBT(int slot, NBTTagCompound nbt) {
        this.slot = slot;
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slot = buf.readInt();
        this.nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slot);
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler implements IMessageHandler<PacketSyncBaubleNBT, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(PacketSyncBaubleNBT message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
                ItemStack stack = handler.getStackInSlot(message.slot);
                if (!stack.isEmpty()) {
                    stack.setTagCompound(message.nbt); // 更新 NBT，无需检查类型
                }
            });
            return null;
        }
    }
}
