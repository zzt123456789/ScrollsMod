package com.zzt.eternal_abyss.event;

import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

import static com.zzt.eternal_abyss.config.ModConfig.CloverDropChance;

public class DropHandler {

    private static final Random random = new Random();

    @SubscribeEvent
    public void onGrassBreak(HarvestDropsEvent event) {
        if (event.getHarvester() == null) return;

        World world = event.getWorld();
        BlockPos pos = event.getPos();

        if (!world.isRemote && event.getState().getBlock() == Blocks.TALLGRASS||  event.getState().getBlock() == Blocks.DOUBLE_PLANT) {
            ItemStack ring = DepthRingHandler.getEquippedDepthRing(event.getHarvester());
            if (!ring.isEmpty()) {
                if (random.nextFloat() < CloverDropChance) {
                    event.getDrops().add(new ItemStack(ModItems.CLOVER));
                }
            }
        }
    }
}
