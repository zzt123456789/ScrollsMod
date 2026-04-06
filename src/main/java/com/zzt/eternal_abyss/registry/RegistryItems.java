package com.zzt.eternal_abyss.registry;

import com.zzt.eternal_abyss.init.ModItems;
//import com.zzt.eternal_abyss.items.CatastropheScroll;
import com.zzt.eternal_abyss.items.ExperienceAttractionScroll;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

@Mod.EventBusSubscriber
public class RegistryItems {
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ModItems.CATASTROPHE_SCROLL,
                ModItems.EXPERIENCE_ATTRACTION_SCROLL,
                ModItems.DEPTH_RING,
                ModItems.VILLAGER_UNLOCKER,
                ModItems.LUCKIFY_SCROLL,
                ModItems.LUCK_MODIFIER,
                ModItems.ABYSSAL_ESSENCE,
                ModItems.WAILING_CORE,
                ModItems.INSANE_ABYSS_DICE,
                ModItems.CLOVER,
                ModItems.VOID_ARTIFACT,
                ModItems.EXOBLADE,
                ModItems.THE_TWISTED_FATE,
                ModItems.THE_DIMINISHED_SHADE,
                ModItems.THE_ABYSSAL_COGNITION,
                ModItems.THE_ARCANE_ANNIHILATION,
                ModItems.THE_SORROWED_SHRIEK,
                ModItems.YUWAN_RING,
                ModItems.SUPERNOVA,
                ModItems.RECORD_BAG
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        registerModel(ModItems.CATASTROPHE_SCROLL);
        registerModel(ModItems.EXPERIENCE_ATTRACTION_SCROLL);
        registerModel(ModItems.DEPTH_RING);
        registerModel(ModItems.VILLAGER_UNLOCKER);
        registerModel(ModItems.LUCKIFY_SCROLL);
        registerModel(ModItems.LUCK_MODIFIER);
        registerModel(ModItems.ABYSSAL_ESSENCE);
        registerModel(ModItems.WAILING_CORE);
        registerModel(ModItems.INSANE_ABYSS_DICE);
        registerModel(ModItems.CLOVER);
        registerModel(ModItems.VOID_ARTIFACT);
        registerModel(ModItems.EXOBLADE);
        registerModel(ModItems.THE_TWISTED_FATE);
        registerModel(ModItems.THE_DIMINISHED_SHADE);
        registerModel(ModItems.THE_ABYSSAL_COGNITION);
        registerModel(ModItems.THE_ARCANE_ANNIHILATION);
        registerModel(ModItems.THE_SORROWED_SHRIEK);
        registerModel(ModItems.YUWAN_RING);
        registerModel(ModItems.SUPERNOVA);
        registerModel(ModItems.RECORD_BAG);
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
