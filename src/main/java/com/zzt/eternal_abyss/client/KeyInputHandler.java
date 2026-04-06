package com.zzt.eternal_abyss.client;


import com.zzt.eternal_abyss.EternalAbyss;
import com.zzt.eternal_abyss.network.PacketToggleItemCollect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {

        if (KeyBindings.toggleItemCollect.isPressed()) {
            EternalAbyss.NETWORK.sendToServer(new PacketToggleItemCollect(0));
        }

        if (KeyBindings.toggleXpCollect.isPressed()) {
            EternalAbyss.NETWORK.sendToServer(new PacketToggleItemCollect(1));
        }

    }
}
