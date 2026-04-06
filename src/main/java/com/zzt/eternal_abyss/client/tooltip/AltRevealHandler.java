package com.zzt.eternal_abyss.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber
public class AltRevealHandler {

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (!(mc.currentScreen instanceof GuiContainer)) {
            AltRevealState.reset();
            return;
        }

        GuiContainer gui = (GuiContainer) mc.currentScreen;
        Slot slot = gui.getSlotUnderMouse();
        ItemStack hovered = slot != null ? slot.getStack() : ItemStack.EMPTY;

        boolean altDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        AltRevealState.update(hovered, altDown);
    }
}