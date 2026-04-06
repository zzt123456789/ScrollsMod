package com.zzt.eternal_abyss.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientDepthRingData {

    private static long loginTime = -1;

    public static void onLogin() {
        loginTime = System.currentTimeMillis();
    }

    public static long getLoginTime() {
        return loginTime;
    }
}
