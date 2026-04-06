package com.zzt.eternal_abyss.util;

import com.zzt.eternal_abyss.config.ModConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class TargetBlacklistUtil {

    public static boolean isBlacklisted(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return ModConfig.swordQiBlacklist.contains("minecraft:player");
        }
        ResourceLocation rl = EntityList.getKey(entity);
        return rl != null && ModConfig.swordQiBlacklist.contains(rl.toString());
    }
}
