package com.zzt.eternal_abyss.event;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.zzt.eternal_abyss.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Mod.EventBusSubscriber
public class TheSorrowedShriekHandler {

    private static final String TAG_ULTIMATE_FLIGHT = "sorrowed_ultimate_flight";
    private static final String TAG_GRANTED_FLIGHT  = "sorrowed_granted_flight";

    private static final Map<UUID, MerchantRecipeList> ORIGINAL_RECIPES = new HashMap<>();


    private static final Map<UUID, List<String>> ORIGINAL_SIGS = new HashMap<>();
    private static final Map<UUID, int[][]> ORIGINAL_PRICES = new HashMap<>();


    private static final float BASE_FLY_SPEED  = 0.05f;
    private static final float BONUS_FLY_SPEED = 0.075f;

    private static final float DISCOUNT = 0.5f;


    /**
     * 饰品佩戴时在 onWornTick 调用
     */
    public static void tickFlight(EntityPlayer player) {

        // ★ 必须：只在服务端执行
        if (player.world.isRemote) return;

        // 标记拥有飞行效果
        player.getEntityData().setBoolean(TAG_ULTIMATE_FLIGHT, true);

        // ====== 开启飞行能力 ======
        if (!player.capabilities.allowFlying) {
            player.capabilities.allowFlying = true;
            player.getEntityData().setBoolean(TAG_GRANTED_FLIGHT, true);
        }
        player.sendPlayerAbilities();
    }

    /**
     * 饰品脱下时调用
     */

    public static void removeFlight(EntityPlayer player) {

        player.getEntityData().removeTag(TAG_ULTIMATE_FLIGHT);

        boolean granted = player.getEntityData().getBoolean(TAG_GRANTED_FLIGHT);

        if (granted && !player.isCreative() && !player.isSpectator()) {

            player.capabilities.allowFlying = false;
            player.capabilities.isFlying = false;

            player.sendPlayerAbilities();
        }

        player.getEntityData().removeTag(TAG_GRANTED_FLIGHT);

        UUID id = player.getUniqueID();
        if (ORIGINAL_RECIPES.containsKey(id)) {
            IMerchant m = player.openContainer instanceof ContainerMerchant
                    ? ReflectionHelper.getPrivateValue(ContainerMerchant.class,
                    (ContainerMerchant) player.openContainer,
                    "merchant", "theMerchant", "merchantObj", "field_75178_e")
                    : null;

            if (m != null) m.setRecipes(ORIGINAL_RECIPES.get(id));
        }
    }


    @SubscribeEvent
    public static void onMerchantOpen(PlayerContainerEvent.Open event) {

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        if (!(event.getContainer() instanceof ContainerMerchant)) return;
        ContainerMerchant container = (ContainerMerchant) event.getContainer();

        // 反射拿 Merchant
        IMerchant merchant;
        try {
            merchant = ReflectionHelper.getPrivateValue(
                    ContainerMerchant.class,
                    container,
                    "merchant", "theMerchant", "merchantObj", "field_75178_e"
            );
        } catch (Exception e) {
            return;
        }

        // 没戴 A → 完全不介入
        if (!hasMyShriek(player)) {
            return;
        }

        // 戴了 A + 戴了 B → 什么都不做，直接放行
        if (hasOtherMask(player)) {
            return;
        }

        if (!(merchant instanceof net.minecraft.entity.Entity)) {
            return;
        }
        NBTTagCompound data = ((Entity) merchant).getEntityData();


        MerchantRecipeList list = merchant.getRecipes(player);
        if (list == null || list.isEmpty()) return;

        // 是否佩戴饰品
        boolean hasShriek = BaublesApi.isBaubleEquipped(player, ModItems.THE_SORROWED_SHRIEK) != -1;


        // 村民先恢复为未折扣状态（避免旧数据）
        data.removeTag("shriek_last_size");

        // 没带饰品 → 不折扣
        if (!hasShriek) return;

        boolean discounted = data.getBoolean("shriek_discounted");

        // 第一次打开 → 折扣全部
        if (!discounted) {

            for (MerchantRecipe r : list) {
                applyDiscount(r);
            }

            data.setBoolean("shriek_discounted", true);
            data.setInteger("shriek_last_size", list.size());
            return;
        }

        // 已折扣 → 检查是否有新交易（村民升级）
        int last = data.getInteger("shriek_last_size");
        int now = list.size();

        if (now > last) {
            for (int i = last; i < now; i++) {
                applyDiscount(list.get(i)); // 只折扣新增的
            }
            data.setInteger("shriek_last_size", now);
        }
    }


    @SubscribeEvent
    public static void onMerchantClose(PlayerContainerEvent.Close event) {

        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;

        if (!(event.getContainer() instanceof ContainerMerchant)) return;
        ContainerMerchant container = (ContainerMerchant) event.getContainer();

        IMerchant merchant;
        try {
            merchant = ReflectionHelper.getPrivateValue(
                    ContainerMerchant.class,
                    container,
                    "merchant", "theMerchant", "merchantObj", "field_75178_e"
            );
        } catch (Exception e) {
            return;
        }

        if (!hasMyShriek(player)) {
            return;
        }

        if (hasOtherMask(player)) {
            return;
        }

        if (!(merchant instanceof net.minecraft.entity.Entity)) {
            return;
        }

        NBTTagCompound data = ((Entity) merchant).getEntityData();


        if (!data.getBoolean("shriek_discounted"))
            return;

        MerchantRecipeList list = merchant.getRecipes(player);
        if (list == null) return;

        // 恢复原价
        for (MerchantRecipe r : list) {
            reverseDiscount(r);
        }

        // 清理 nbt
        data.removeTag("shriek_discounted");
        data.removeTag("shriek_last_size");
    }



    private static void applyDiscount(MerchantRecipe r) {

        // buy1
        ItemStack buy1 = r.getItemToBuy();
        if (!buy1.isEmpty()) {
            int orig = buy1.getCount();
            if (orig > 1) { // 原价为1的不变
                buy1.setCount(Math.max(1, orig / 2));
            }
        }

        // buy2
        ItemStack buy2 = r.getSecondItemToBuy();
        if (!buy2.isEmpty()) {
            int orig = buy2.getCount();
            if (orig > 1) {
                buy2.setCount(Math.max(1, orig / 2));
            }
        }
    }

    private static void reverseDiscount(MerchantRecipe r) {

        // buy1
        ItemStack buy1 = r.getItemToBuy();
        if (!buy1.isEmpty()) {
            int now = buy1.getCount();
            if (now > 1) { // 原本为1的不恢复
                buy1.setCount(now * 2);
            }
        }

        // buy2
        ItemStack buy2 = r.getSecondItemToBuy();
        if (!buy2.isEmpty()) {
            int now = buy2.getCount();
            if (now > 1) {
                buy2.setCount(now * 2);
            }
        }
    }


    private static boolean hasMyShriek(EntityPlayer player) {
        return BaublesApi.isBaubleEquipped(player, ModItems.THE_SORROWED_SHRIEK) != -1;
    }

    private static boolean hasOtherMask(EntityPlayer player) {
        Item item = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("enigmaticlegacy", "half_heart_mask")
        );
        if (item == null) return false;

        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }



}
