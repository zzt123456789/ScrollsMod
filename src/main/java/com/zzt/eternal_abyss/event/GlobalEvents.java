package com.zzt.eternal_abyss.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public class GlobalEvents {

    private static final Set<EntityPlayer> tickedPlayers = new HashSet<>();

    /**
     * 每 tick 重置列表
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote) {
            tickedPlayers.remove(event.player);
        }
    }

    /**
     * 是否是当前 tick 第一次处理这个玩家（用于避免重复提示）
     */
    public static boolean isFirstTick(EntityPlayer player) {
        if (tickedPlayers.contains(player)) {
            return false;
        } else {
            tickedPlayers.add(player);
            return true;
        }
    }

    /**
     * 清除（例如在卸下饰品时）
     */
    public static void clearPlayer(EntityPlayer player) {
        tickedPlayers.remove(player);
    }


}
