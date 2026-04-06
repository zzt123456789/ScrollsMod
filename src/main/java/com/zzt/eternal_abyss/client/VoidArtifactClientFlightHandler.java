package com.zzt.eternal_abyss.client;

import com.zzt.eternal_abyss.util.ImmunityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class VoidArtifactClientFlightHandler {

    private static final float ULTIMATE_FLY_SPEED = 0.1f;
    private static final float DEFAULT_FLY_SPEED = 0.05f;

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        EntityPlayer player = mc.player;

        boolean hasArtifact = ImmunityHelper.hasUltimateItem(player);

        if (hasArtifact && player.capabilities.allowFlying) {
            if (player.capabilities.getFlySpeed() != ULTIMATE_FLY_SPEED) {
                player.capabilities.setFlySpeed(ULTIMATE_FLY_SPEED);
            }
        } else {
            if (!player.isCreative() && player.capabilities.getFlySpeed() != DEFAULT_FLY_SPEED) {
                player.capabilities.setFlySpeed(DEFAULT_FLY_SPEED);
            }
        }
    }
}
