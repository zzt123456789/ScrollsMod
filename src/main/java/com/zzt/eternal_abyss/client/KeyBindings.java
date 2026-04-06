package com.zzt.eternal_abyss.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    public static KeyBinding toggleItemCollect;
    public static KeyBinding toggleXpCollect; // 新增

    public static void init() {

        toggleItemCollect = new KeyBinding(
                "key.eternal_abyss.toggle_item_collect",
                Keyboard.KEY_Z,
                "key.categories.eternal_abyss"
        );

        toggleXpCollect = new KeyBinding(
                "key.eternal_abyss.toggle_xp_collect",
                Keyboard.KEY_X,
                "key.categories.eternal_abyss"
        );

        ClientRegistry.registerKeyBinding(toggleItemCollect);
        ClientRegistry.registerKeyBinding(toggleXpCollect);
    }
}
