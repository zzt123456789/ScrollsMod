package com.zzt.eternal_abyss.init;

import net.minecraft.potion.PotionType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModPotionTypes {
    public static PotionType SWORD_QI_TYPE;

    public static void registerPotionTypes(IForgeRegistry<PotionType> registry) {
        SWORD_QI_TYPE = new PotionType("sword_qi",
                new net.minecraft.potion.PotionEffect(ModPotions.SWORD_QI, 600))
                .setRegistryName("sword_qi");
        registry.register(SWORD_QI_TYPE);
    }
}
