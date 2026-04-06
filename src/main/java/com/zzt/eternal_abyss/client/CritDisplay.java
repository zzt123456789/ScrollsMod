package com.zzt.eternal_abyss.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CritDisplay {

    private static int critLayers = 0;
    private static int displayTicks = 0;

    public static void addCrit(int layers) {
        critLayers = layers;
        displayTicks = 100;
    }

    @SubscribeEvent
    public static void onRenderHUD(RenderGameOverlayEvent.Text event) {

        if (displayTicks > 0 && critLayers > 0) {
            String txt = critLayers == 1 ?
                    "\u00A7l暴击!" :
                    "\u00A7l暴击 ×" + critLayers + "!!!";

            Minecraft mc = Minecraft.getMinecraft();

            int x = mc.displayWidth / 2;
            int y = mc.displayHeight / 4;

            int color = getCritColor(critLayers);

            mc.fontRenderer.drawStringWithShadow(
                    txt,
                    x / 2f,
                    y / 2f,
                    color
            );

            displayTicks--;
        }
    }


    private static int getCritColor(int layer) {

        if (layer <= 1) return 0xFFFF55;      // 黄色 (Gold)
        if (layer == 2) return 0xFFAA00;      // 橙色
        if (layer == 3) return 0xFF5555;      // 强烈红
        if (layer == 4) return 0xAA00FF;      // 紫色


        float hue = (System.currentTimeMillis() % 1500L) / 1000.0f;
        return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }

}
