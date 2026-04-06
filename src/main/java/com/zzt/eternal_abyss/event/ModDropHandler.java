package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModDropHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntityLiving() instanceof EntityWither)) return;

        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) source.getTrueSource();

        // 检查是否佩戴了深渊戒指
        int slot = BaublesApi.isBaubleEquipped(player, ModItems.DEPTH_RING);
        if (slot == -1) return;

        // 检查是否有至少 3 个负面效果
        long negativeEffectCount = player.getActivePotionEffects().stream()
                .filter(effect -> effect.getPotion().isBadEffect())
                .count();

        if (negativeEffectCount < 3) return;

        // 满足所有条件，掉落 2~5 个 AbyssalEssence
        World world = event.getEntityLiving().getEntityWorld();
        int amount = 2 + world.rand.nextInt(4); // [2, 5]
        ItemStack drop = new ItemStack(ModItems.ABYSSAL_ESSENCE, amount);

        EntityItem entityItem = new EntityItem(world,
                event.getEntityLiving().posX,
                event.getEntityLiving().posY,
                event.getEntityLiving().posZ,
                drop);

        event.getDrops().add(entityItem);
        entityItem.setNoDespawn();        // 可选：不消失
        entityItem.setFire(0);

    }

}
