package com.zzt.eternal_abyss.util.compat;

import net.minecraft.entity.EntityLivingBase;
import net.silentchaos512.scalinghealth.event.BlightHandler;
public abstract class ScalingHealthCompat {

    public static boolean isEntityBlight(EntityLivingBase entity) {
        return BlightHandler.isBlight(entity);
    }
}