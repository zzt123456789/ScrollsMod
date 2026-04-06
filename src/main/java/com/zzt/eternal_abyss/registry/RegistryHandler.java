package com.zzt.eternal_abyss.registry;

import com.zzt.eternal_abyss.init.ModPotionTypes;
import com.zzt.eternal_abyss.init.ModPotions;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent
    public static void onRegisterPotionTypes(RegistryEvent.Register<PotionType> event) {
        ModPotionTypes.registerPotionTypes(event.getRegistry());
    }

}
