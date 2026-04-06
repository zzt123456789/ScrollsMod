package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import com.tmtravlr.potioncore.PotionCoreAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class LuckifyScroll extends ItemBase implements IBauble {
    public LuckifyScroll() {
        super("luckify_scroll");
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    private static final UUID LUCKIFY_DAMAGE_UUID =
            UUID.fromString("9a6a5f41-4b8a-4e8e-b2b7-7c7b3a000001");

    private static final UUID LUCKIFY_PROJECTILE_UUID =
            UUID.fromString("9a6a5f41-4b8a-4e8e-b2b7-7c7b3a000222");



    private static final Map<UUID, Long> lastDodgeTick = new HashMap<>();

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;

        // 仅当在饰品栏中佩戴该饰品时生效
        EntityPlayer player = (EntityPlayer) entity;
        if (!isEquippedInBaubles(player, stack)) return;

        float luck = player.getLuck();

        double bonusDamage = DepthRing.isDamageReversedFor(player) ? 0.04 * luck : 0.02 * luck;

        applyOrUpdateModifier(player, SharedMonsterAttributes.ATTACK_DAMAGE, bonusDamage, "luckify_damage");

        double bonusProjectile =
                DepthRing.isDamageReversedFor(player) ? 0.04 * luck : 0.02 * luck;

        applyProjectileModifier(player, bonusProjectile);
    }


    private void applyOrUpdateModifier(EntityPlayer player,
                                       IAttribute attribute,
                                       double value,
                                       String key) {

        IAttributeInstance instance = player.getEntityAttribute(attribute);
        if (instance == null) return;

        UUID id = LUCKIFY_DAMAGE_UUID;

        AttributeModifier old = instance.getModifier(id);
        if (old != null) {
            instance.removeModifier(old);
        }

        instance.applyModifier(new AttributeModifier(id, key, value, 2));
    }


    private void applyProjectileModifier(EntityPlayer player, double bonus) {
        IAttributeInstance attr = player.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE);
        if (attr == null) return;

        AttributeModifier old = attr.getModifier(LUCKIFY_PROJECTILE_UUID);
        if (old != null) attr.removeModifier(old);

        attr.applyModifier(new AttributeModifier(
                LUCKIFY_PROJECTILE_UUID,
                "luckify_projectile_damage",
                bonus,
                2
        ));
    }


    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, .75F, 1.9f);

    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        // projectile damage
        IAttributeInstance proj = player.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE);
        if (proj != null) {
            AttributeModifier m = proj.getModifier(LUCKIFY_PROJECTILE_UUID);
            if (m != null) proj.removeModifier(m);
        }

        // attack damage
        IAttributeInstance dmg = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (dmg != null) {
            AttributeModifier m = dmg.getModifier(LUCKIFY_DAMAGE_UUID);
            if (m != null) dmg.removeModifier(m);
        }
    }



    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;

        EntityPlayer player = (EntityPlayer) entity;
        int equippedIndex = BaublesApi.isBaubleEquipped(player, this);
        return equippedIndex == -1; // 只允许佩戴一个
    }


    public static boolean isEquippedInBaubles(EntityPlayer player, ItemStack target) {
        IItemHandler baublesHandler = BaublesApi.getBaublesHandler(player);
        if (baublesHandler == null) return false;

        for (int i = 0; i < baublesHandler.getSlots(); i++) {
            ItemStack stack = baublesHandler.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.areItemsEqual(stack, target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
        list.add("");
        EntityPlayer player = Minecraft.getMinecraft().player;


        if (GuiScreen.isShiftKeyDown()) {
            if(player != null && DepthRing.isDamageReversedFor(player)){
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + "4%" + I18n.format("tooltip.eternal_abyss.luckify1"));
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + "4%" + I18n.format("tooltip.eternal_abyss.luckify2"));
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + "2%" + I18n.format("tooltip.eternal_abyss.luckify3"));

                list.add("");
                list.add(I18n.format("tooltip.eternal_abyss.luckify4"));
                list.add(I18n.format("tooltip.eternal_abyss.luckify5"));
            }
            else{
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + "2%" + I18n.format("tooltip.eternal_abyss.luckify1"));
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + "2%" + I18n.format("tooltip.eternal_abyss.luckify2"));
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + "1%" + I18n.format("tooltip.eternal_abyss.luckify3"));

                list.add("");
                list.add(I18n.format("tooltip.eternal_abyss.luckify4"));
                list.add(I18n.format("tooltip.eternal_abyss.luckify5"));
            }
        } else {
            list.add(I18n.format("tooltip.eternal_abyss.holdShift"));
        }

        // 读取当前玩家幸运值并展示加成效果
        if (player != null && player.getEntityAttribute(SharedMonsterAttributes.LUCK) != null) {
            if(player != null && DepthRing.isDamageReversedFor(player)){
                double luck = player.getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue();

                list.add("");
                list.add(I18n.format("tooltip.eternal_abyss.luckify7"));
                list.add(I18n.format("tooltip.eternal_abyss.luckify6")); // 例如 "你的当前幸运值带来的加成："
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + String.format("%.1f%%", luck * 4) + I18n.format("tooltip.eternal_abyss.luckify1"));
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + String.format("%.1f%%", luck * 4) + I18n.format("tooltip.eternal_abyss.luckify2"));
                double dodgeChance = luck * 0.02;
                dodgeChance = Math.min(dodgeChance, 0.6);
                list.add(I18n.format("tooltip.eternal_abyss.luckify3")
                        + TextFormatting.GOLD + String.format("%.1f%%", dodgeChance * 100));
            }
            else{
                double luck = player.getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue();

                list.add("");
                list.add(I18n.format("tooltip.eternal_abyss.luckify6")); // 例如 "你的当前幸运值带来的加成："
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + String.format("%.1f%%", luck * 2) + I18n.format("tooltip.eternal_abyss.luckify1"));
                list.add(I18n.format("tooltip.eternal_abyss.add") + TextFormatting.GOLD + String.format("%.1f%%", luck * 2) + I18n.format("tooltip.eternal_abyss.luckify2"));
                double dodgeChance = luck * 0.01;
                dodgeChance = Math.min(dodgeChance, 0.6);
                list.add(I18n.format("tooltip.eternal_abyss.luckify3")
                        + TextFormatting.GOLD + String.format("%.1f%%", dodgeChance * 100));            }
        }
    }


}