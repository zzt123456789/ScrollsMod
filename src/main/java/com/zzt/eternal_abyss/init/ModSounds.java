package com.zzt.eternal_abyss.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "eternal_abyss")
public class ModSounds {

    public static SoundEvent EXOBLADE_SWING;
    public static SoundEvent CRIT_HIT;
    public static SoundEvent PARRY;
    public static SoundEvent SUPERNOVA_EXPLODE;
    public static SoundEvent SUPERNOVA_CHARGE;
    public static SoundEvent TARGET_FOUND;



    /** 在注册事件中注册所有自定义音效 */
    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        EXOBLADE_SWING = register(event, "exoblade_swing");
        CRIT_HIT = register(event, "crit_hit");
        PARRY = register(event, "parry");
        SUPERNOVA_EXPLODE = register(event, "supernova_explode");
        SUPERNOVA_CHARGE = register(event, "supernova_charge");
        TARGET_FOUND = register(event, "target_found");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event, String name) {
        ResourceLocation loc = new ResourceLocation("eternal_abyss", name);
        SoundEvent sound = new SoundEvent(loc).setRegistryName(loc);
        event.getRegistry().register(sound);
        return sound;
    }
}
