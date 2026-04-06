package com.zzt.eternal_abyss.items;

import com.google.common.collect.Multimap;
import com.zzt.eternal_abyss.entity.EntitySwordQi;
import com.zzt.eternal_abyss.init.ModCreativeTabs;
import com.zzt.eternal_abyss.init.ModDamageSource;
import com.zzt.eternal_abyss.init.ModPotions;
import com.zzt.eternal_abyss.init.ModSounds;
import com.zzt.eternal_abyss.util.RainbowTextHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;



public class Exoblade extends ItemSword {

    private final float attackSpeed;
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("12345678-1234-5678-1234-567812345678");

    public Exoblade(ToolMaterial material, String name) {
        super(material);
        this.attackSpeed = -2.5F;
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setCreativeTab(ModCreativeTabs.CelestialArtifactsTab);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }


    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> map = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            map.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, 0));
            map.put(EntityPlayer.REACH_DISTANCE.getName(),
                    new AttributeModifier(REACH_MODIFIER_UUID, "Exoblade reach bonus", 2.5D, 0).setSaved(false));
        }
        return map;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return RainbowTextHelper.rainbowText("§l§3星流之刃", 3.0F, 1.0F).getUnformattedText();
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (!entityLiving.world.isRemote && entityLiving instanceof EntityPlayer) {

            NBTTagCompound tag = stack.getSubCompound("ExobladeData");
            if (tag != null && tag.getBoolean("HitTarget")) {
                tag.setBoolean("HitTarget", false);
                return false;
            }

            EntityPlayer player = (EntityPlayer) entityLiving;

            // 播放音效
            player.world.playSound(
                    null,
                    player.posX, player.posY, player.posZ,
                    ModSounds.EXOBLADE_SWING,
                    net.minecraft.util.SoundCategory.PLAYERS,
                    1.0F,
                    0.9F + itemRand.nextFloat() * 0.2F
            );

            // 冷却
            float attackSpeed = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue();
            if (attackSpeed <= 0) attackSpeed = 1.0F;
            int cooldownTicks = (int) (40 / attackSpeed);
            if (player.getCooldownTracker().hasCooldown(this)) return false;
            player.getCooldownTracker().setCooldown(this, cooldownTicks);


            // 计算方向向量
            Vec3d look = player.getLookVec();

            // 左方向
            Vec3d left = new Vec3d(
                    -MathHelper.sin(player.rotationYaw * 0.017453292F),
                    0,
                    MathHelper.cos(player.rotationYaw * 0.017453292F)
            ).normalize();

            Vec3d up = left.crossProduct(look).normalize();


            // 头顶半圆弧 4 个角度
            double[] arcAngles = {-55, -20, 20, 55}; // 单位: 度
            double radius = 1.0;   // 弧线大小
            double height = 1.2;   // 离头顶的高度

            Vec3d eye = player.getPositionVector().add(0, player.getEyeHeight(), 0);

            for (double angleDeg : arcAngles) {

                double rad = Math.toRadians(angleDeg);

                // 半圆拱形位置 = 左右位移 + 头顶高度
                Vec3d offset =
                        left.scale(Math.sin(rad) * radius)
                                .add(up.scale(Math.cos(rad) * radius))
                                .add(0, height, 0);


                // 生成位置
                Vec3d spawnPos = eye.add(offset);

                // 创建实体
                EntitySwordQi qi = new EntitySwordQi(player.world, player);
                qi.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);

                // 发射方向 = 看向方向（保证一致）
                double speed = 1.5;
                Vec3d dir = look.normalize();

                qi.motionX = dir.x * speed;
                qi.motionY = dir.y * speed;
                qi.motionZ = dir.z * speed;

                player.world.spawnEntity(qi);
            }

        }
        return super.onEntitySwing(entityLiving, stack);
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;

        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.getItem() instanceof Exoblade) {
            player.removeActivePotionEffect(ModPotions.SWORD_QI);
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (attacker instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) attacker;

            // 标记这次攻击命中了目标
            stack.getOrCreateSubCompound("ExobladeData").setBoolean("HitTarget", true);

            float damage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            target.attackEntityFrom(ModDamageSource.abyssSweepWithPlayer(player), damage);

            // 横扫
            double radius = getSweepRange(stack);
            AxisAlignedBB area = target.getEntityBoundingBox().grow(radius, 1.0, radius);
            List<EntityLivingBase> targets = player.world.getEntitiesWithinAABB(
                    EntityLivingBase.class, area,
                    e -> e != player && e != target && e.isEntityAlive() && player.canEntityBeSeen(e)
            );

            for (EntityLivingBase aoeTarget : targets) {
                if (stack.isEmpty()) break; // 防止武器消失
                // 使用间接伤害源
                aoeTarget.attackEntityFrom(
                        new EntityDamageSourceIndirect("exobladesweep", player, player),
                        damage
                );
            }

        }
        return super.hitEntity(stack, target, attacker);
    }


    private double getSweepRange(ItemStack stack) {
        double baseRange = 3.0D; // 默认基础范围

        if (Loader.isModLoaded("somanyenchantments")) {
            // 弧光之刃 (Arc Slash)
            Enchantment arcSlash = Enchantment.REGISTRY.getObject(
                    new ResourceLocation("somanyenchantments", "arcslash")
            );
            if (arcSlash != null) {
                int arcSlashLevel = EnchantmentHelper.getEnchantmentLevel(arcSlash, stack);
                if (arcSlashLevel > 0) {
                    baseRange += arcSlashLevel * 1.0D; // 每级 +1 格范围
                }
            }

            // 横扫之刃 (Swiper)
            Enchantment swiper = Enchantment.REGISTRY.getObject(
                    new ResourceLocation("enchantment", "swiper")
            );
            if (swiper != null) {
                int swiperLevel = EnchantmentHelper.getEnchantmentLevel(swiper, stack);
                if (swiperLevel > 0) {
                    baseRange += swiperLevel * 0.5D; // 每级 +0.5 格范围
                }
            }
        }

        return baseRange;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("");
        tooltip.add(I18n.format("tooltip.exoblade.description1"));
        tooltip.add(I18n.format("tooltip.exoblade.description2"));
        tooltip.add("");
        tooltip.add(I18n.format("tooltip.exoblade.description3"));
    }
}


