package com.zzt.eternal_abyss.util.compat;

import net.minecraftforge.fml.common.Loader;

public class ModCompat {

    private static Boolean isScalingHealthLoaded = null;
    private static final String SCALINGHEALTH_MODID = "scalinghealth";
    public static final boolean COMPAT_FIRSTAID = Loader.isModLoaded("firstaid");

    public static boolean isScalingHealthLoaded() {
        if(isScalingHealthLoaded == null) isScalingHealthLoaded = Loader.isModLoaded(SCALINGHEALTH_MODID);
        return isScalingHealthLoaded;
    }
}
