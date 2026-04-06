package com.zzt.eternal_abyss.init;

import com.zzt.eternal_abyss.potion.PotionSwordQi;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

// ModPotions.java
@Mod.EventBusSubscriber(modid = "eternal_abyss")
@GameRegistry.ObjectHolder("eternal_abyss")
public class ModPotions {
    @GameRegistry.ObjectHolder("sword_qi")
    public static Potion SWORD_QI;

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> event) {
        SWORD_QI = new PotionSwordQi().setRegistryName("eternal_abyss", "sword_qi");
        event.getRegistry().register(SWORD_QI);
    }
}

