package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.BaublesApi;
import com.zzt.eternal_abyss.init.ModCreativeTabs;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static com.zzt.eternal_abyss.config.ModConfig.expAttractRange;
import static com.zzt.eternal_abyss.config.ModConfig.expAttractRangeBoosted;

public class ExperienceAttractionScroll extends Item implements IBauble {

    /** 卷轴只有“吸物品”开关 */
    private static final String TAG_ITEM吸取 = "ItemCollectEnabled";

    public ExperienceAttractionScroll() {
        super();
        this.setRegistryName("experience_attraction_scroll");
        this.setTranslationKey("experience_attraction_scroll");
        this.setMaxStackSize(1);
        this.setCreativeTab(ModCreativeTabs.CelestialArtifactsTab);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.RING;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        World world = player.world;
        if (world.isRemote) return;


        double range = DepthRing.isExpBoostReversedFor(player)
                ? expAttractRangeBoosted
                : expAttractRange;

        List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(
                EntityXPOrb.class,
                player.getEntityBoundingBox().grow(range)
        );

        for (EntityXPOrb orb : orbs) {
            if (!orb.isDead) {
                orb.setPosition(player.posX, player.posY, player.posZ);
                player.xpCooldown = 0; // 强制无 CD
                orb.onCollideWithPlayer(player);
            }
        }

        if (!isItemCollectEnabled(stack)) return;

        List<EntityItem> items = world.getEntitiesWithinAABB(
                EntityItem.class,
                player.getEntityBoundingBox().grow(range)
        );

        for (EntityItem item : items) {
            if (item.isDead || item.getItem().isEmpty()) continue;

            // 停止漂移
            item.motionX = item.motionY = item.motionZ = 0;

            // 蹲下 → 立即吸入
            if (player.isSneaking()) {
                item.setPosition(player.posX, player.posY, player.posZ);
                item.onCollideWithPlayer(player);
            } else {
                // 悬浮在玩家背后 1 格
                double yaw = Math.toRadians(player.rotationYaw);
                double ox = Math.sin(yaw);
                double oz = -Math.cos(yaw);

                item.setPosition(
                        player.posX + ox,
                        player.posY,
                        player.posZ + oz
                );
            }
        }
    }

    public static boolean isItemCollectEnabled(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(TAG_ITEM吸取);
    }

    public static void setItemCollectEnabled(ItemStack stack, boolean enabled) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean(TAG_ITEM吸取, enabled);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn,
                               List<String> tooltip, ITooltipFlag flagIn) {

        if (!GuiScreen.isAltKeyDown()) {
            tooltip.add(I18n.translateToLocal("tooltip.eternal_abyss.experience_attraction_scroll.desc"));

            EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;

            if (player != null && DepthRing.isExpBoostReversedFor(player)) {
                tooltip.add(TextFormatting.AQUA +
                        "吸收范围提升至 " + expAttractRangeBoosted);
            } else {
                tooltip.add(TextFormatting.GRAY +
                        "吸收范围：" + expAttractRange + " 格");
            }

            tooltip.add("");

            // 经验吸取（卷轴默认永久开启）
            tooltip.add(TextFormatting.GOLD + "经验吸取： " + TextFormatting.GREEN + "已启用");

            // 物品吸取（跟随 NBT）
            if (isItemCollectEnabled(stack)){
                tooltip.add(TextFormatting.YELLOW + "物品吸取：" +
                        (TextFormatting.GREEN + "开启"));
            }
            else {
                tooltip.add(TextFormatting.YELLOW + "物品吸取：" +
                        (TextFormatting.RED + "未启用"));
            }

            tooltip.add("");

        }
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }


    @Override
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 1.9f);
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 2f);
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        if (!(player instanceof EntityPlayer)) return false;

        EntityPlayer p = (EntityPlayer) player;
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(p);

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack other = handler.getStackInSlot(i);

            if (!other.isEmpty()) {
                if (other.getItem() instanceof ExperienceAttractionScroll) return false;
            }
        }
        return true;
    }
}
