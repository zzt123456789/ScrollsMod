package com.zzt.eternal_abyss.registry;

import com.zzt.eternal_abyss.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.model.ModelLoader;

@Mod.EventBusSubscriber
public class RegistryBlocks {


    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ModBlocks.AUTO_ANVIL);
        event.getRegistry().register(ModBlocks.XPDrain);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ModBlocks.AUTO_ANVIL_ITEM);
        event.getRegistry().register(ModBlocks.XPDrain_ITEM);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerBlockModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                ModBlocks.AUTO_ANVIL_ITEM,
                0,
                new ModelResourceLocation(ModBlocks.AUTO_ANVIL.getRegistryName(), "inventory")
        );
    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerItemModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                ModBlocks.XPDrain_ITEM,
                0,
                new ModelResourceLocation(ModBlocks.XPDrain.getRegistryName(), "inventory")
        );
    }
}
