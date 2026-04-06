package com.zzt.eternal_abyss.client.overlay;

import com.zzt.eternal_abyss.client.gui.GuiPlayerStats;
import com.zzt.eternal_abyss.client.gui.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class InventoryButtonOverlay {

    private static final int BTN_ID = 9988; // 唯一ID

    private static final ResourceLocation ICON =
            new ResourceLocation("eternal_abyss", "textures/gui/stats_button.png");

    @SubscribeEvent
    public static void addButtons(GuiScreenEvent.InitGuiEvent.Post event) {

        if (!(event.getGui() instanceof GuiInventory)) return;

        GuiInventory gui = (GuiInventory) event.getGui();

        int guiLeft = (gui.width - 176) / 2;
        int guiTop = (gui.height - 166) / 2;

        int x = guiLeft + 176;
        int y = guiTop + 166 - 24;

        // 添加你的按钮
        event.getButtonList().add(new ImageButton(
                BTN_ID,
                x, y,
                24, 24,
                ICON,
                0, 0,
                24, 24
        ));
    }


    /** 处理按钮点击 */
    @SubscribeEvent
    public static void onAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (!(event.getGui() instanceof GuiInventory)) return;

        GuiButton button = event.getButton();
        if (button.id == BTN_ID) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiPlayerStats());
        }
    }
}
