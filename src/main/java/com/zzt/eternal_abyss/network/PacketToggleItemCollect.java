package com.zzt.eternal_abyss.network;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.items.ExperienceAttractionScroll;
import com.zzt.eternal_abyss.items.TheArcaneAnnihilation;
import com.zzt.eternal_abyss.util.BaubleSyncUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToggleItemCollect implements IMessage {

    /** 0 = 物品吸取, 1 = 经验吸取 */
    private int mode;

    public PacketToggleItemCollect() {}
    public PacketToggleItemCollect(int mode) {
        this.mode = mode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(mode);
    }


    public static class Handler implements IMessageHandler<PacketToggleItemCollect, IMessage> {

        @Override
        public IMessage onMessage(PacketToggleItemCollect message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            player.getServerWorld().addScheduledTask(() -> {


                IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
                if (baubles != null) {

                    for (int i = 0; i < baubles.getSlots(); i++) {
                        ItemStack st = baubles.getStackInSlot(i);
                        if (handleToggle(st, player, message.mode)) {
                            return; // 已处理一个饰品 → 立即退出
                        }
                    }
                }

                for (ItemStack st : player.inventory.mainInventory) {
                    if (handleToggle(st, player, message.mode)) {
                        return; // 已处理一个 → 立即退出
                    }
                }

                player.sendStatusMessage(
                        new TextComponentString(TextFormatting.RED + "未佩戴可切换的饰品"),
                        true
                );
            });

            return null;
        }

        /**
         * 只处理一个饰品
         * @return true = 已处理，需要退出外循环
         */
        private boolean handleToggle(ItemStack stack, EntityPlayerMP player, int mode) {

            if (stack.isEmpty()) return false;

            // 卷轴（只有物品吸取）
            if (stack.getItem() instanceof ExperienceAttractionScroll) {

                if (mode == 0) {
                    boolean now = ExperienceAttractionScroll.isItemCollectEnabled(stack);
                    ExperienceAttractionScroll.setItemCollectEnabled(stack, !now);

                    // 同步
                    player.inventoryContainer.detectAndSendChanges();
                    BaubleSyncUtil.syncBaubleNBT(player, stack);

                    player.sendStatusMessage(
                            new TextComponentString(
                                    TextFormatting.GRAY + "[卷轴] 物品吸取: " +
                                            (!now ? TextFormatting.GREEN + "开启" : TextFormatting.RED + "关闭")
                            ),
                            true
                    );
                }

                return true;
            }

            // 湮灭之环（双模式）
            if (stack.getItem() instanceof TheArcaneAnnihilation) {

                // 物品吸取
                if (mode == 0) {
                    boolean now = TheArcaneAnnihilation.isArcaneConvertDrops(stack);
                    TheArcaneAnnihilation.setArcaneConvertDrops(stack, !now);

                    player.inventoryContainer.detectAndSendChanges();
                    BaubleSyncUtil.syncBaubleNBT(player, stack);

                    player.sendStatusMessage(
                            new TextComponentString(
                                    TextFormatting.LIGHT_PURPLE + "[湮灭之环] 物品湮灭: " +
                                            (!now ? TextFormatting.GREEN + "开启" : TextFormatting.RED + "关闭")
                            ),
                            true
                    );

                    return true;
                }

                // 经验吸取
                if (mode == 1) {
                    boolean now = TheArcaneAnnihilation.isArcaneActive(stack);
                    TheArcaneAnnihilation.setArcaneActive(stack, !now);

                    player.inventoryContainer.detectAndSendChanges();
                    BaubleSyncUtil.syncBaubleNBT(player, stack);

                    player.sendStatusMessage(
                            new TextComponentString(
                                    TextFormatting.AQUA + "[湮灭之环] 经验吸取: " +
                                            (!now ? TextFormatting.GREEN + "开启" : TextFormatting.RED + "关闭")
                            ),
                            true
                    );
                    return true;
                }
            }
            return false;
        }


    }
}
