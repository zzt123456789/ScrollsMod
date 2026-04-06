package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.items.YuwanRing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class YuwanRingHandler {

    private static final String TAG_WINDOW = "YuwanWindow";
    private static final String TAG_COUNT  = "YuwanCount";

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {

        // ===== 只在服务端执行 =====
        if (event.getEntity().world.isRemote) return;

        // ===== 必须是玩家造成的伤害 =====
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

        // ===== 必须佩戴失温戒指 =====
        if (!hasYuwanRing(player)) return;

        EntityLivingBase target = event.getEntityLiving();

        // ===== 无敌实体不处理（Boss / 创造 / 特殊实体）=====
        if (target.isEntityInvulnerable(event.getSource())) return;

        // ===== 每秒最多 4 次（20 tick 滑动窗口）=====
        long nowTick = player.world.getTotalWorldTime();
        NBTTagCompound data = player.getEntityData();

        long window = data.getLong(TAG_WINDOW);
        int count   = data.getInteger(TAG_COUNT);

        if (nowTick - window >= 20) {
            window = nowTick;
            count = 0;
        }

        if (count >= 4) return;

        // ===== 额外 25 点 setHealth 伤害（不覆盖原伤害）=====
        float damage = 25.0F;
        target.setHealth(Math.max(0.0F, target.getHealth() - damage));


        // ===== 播放触发成功音效（只播一次）=====
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_ENDERDRAGON_HURT,
                SoundCategory.PLAYERS,
                0.8F,
                0.9F + player.getRNG().nextFloat() * 0.2F
        );

        data.setLong(TAG_WINDOW, window);
        data.setInteger(TAG_COUNT, count + 1);

    }

    private static boolean hasYuwanRing(EntityPlayer player) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof YuwanRing) {
                return true;
            }
        }
        return false;
    }
}
