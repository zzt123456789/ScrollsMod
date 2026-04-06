package com.zzt.eternal_abyss.packages;

import com.zzt.eternal_abyss.client.CritDisplay;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCritMessage implements IMessage {

    private int layers;

    public PacketCritMessage() {}  // 必须要有一个空构造

    public PacketCritMessage(int layers) {
        this.layers = layers;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(layers);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        layers = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketCritMessage, IMessage> {
        @Override
        public IMessage onMessage(PacketCritMessage msg, MessageContext ctx) {

            // 必须在客户端主线程执行
            Minecraft.getMinecraft().addScheduledTask(() -> {
                CritDisplay.addCrit(msg.layers);
            });

            return null;
        }
    }
}
