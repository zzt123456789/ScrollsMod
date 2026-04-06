package com.zzt.eternal_abyss.items;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import com.tmtravlr.potioncore.PotionCoreAttributes;
import com.zzt.eternal_abyss.event.DepthRingHandler;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.zzt.eternal_abyss.config.ModConfig.expAttractRange;
import static com.zzt.eternal_abyss.config.ModConfig.expAttractRangeBoosted;
import static com.zzt.eternal_abyss.items.TheTwistedFate.revealFormattedText;

public class TheArcaneAnnihilation extends ItemBase implements IBauble {

    private static final String TAG_AN_ACTIVE = "ArcaneActive";
    private static final String TAG_AN_CONVERT_DROPS = "ArcaneConvertDrops";

    private static final UUID MAGIC_DAMAGE_ADD =
            UUID.fromString("9e8b8c3f-4dd1-4a27-b703-9cfe92afc221");

    private static final UUID MAGIC_DEFENSE_ADD =
            UUID.fromString("4d338df7-3a0d-4db9-bf41-2cb9f020c987");



    public TheArcaneAnnihilation(String name) {
        super(name);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.RING;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        double range = DepthRing.isExpBoostReversedFor(player)
                ? expAttractRangeBoosted
                : expAttractRange;

        if (isArcaneActive(stack)) {

            List<EntityXPOrb> orbs = player.world.getEntitiesWithinAABB(
                    EntityXPOrb.class,
                    player.getEntityBoundingBox().grow(range)
            );

            for (EntityXPOrb orb : orbs) {
                if (!orb.isDead) {
                    orb.setPosition(player.posX, player.posY, player.posZ);
                    player.xpCooldown = 0;
                    orb.onCollideWithPlayer(player);
                }
            }
        }
//
//        if (isArcaneItemCollect(stack)) {
//            List<EntityItem> items = player.world.getEntitiesWithinAABB(
//                    EntityItem.class,
//                    player.getEntityBoundingBox().grow(range)
//            );
//
//            for (EntityItem item : items) {
//                if (item.isDead || item.getItem().isEmpty()) continue;
//
//                boolean absorbed = false;
//
//                item.motionX = 0;
//                item.motionY = 0;
//                item.motionZ = 0;
//
//                if (player.isSneaking()) {
//                    item.setPosition(player.posX, player.posY, player.posZ);
//                    item.onCollideWithPlayer(player);
//                    absorbed = true;
//                } else {
//                    double yaw = Math.toRadians(player.rotationYaw);
//                    double offsetX = Math.sin(yaw);
//                    double offsetZ = -Math.cos(yaw);
//
//                    item.setPosition(
//                            player.posX + offsetX,
//                            player.posY,
//                            player.posZ + offsetZ
//                    );
//                }
//
//                if (!absorbed) {
//                    item.setNoGravity(false);
//                }
//            }
//        }

        if (DepthRing.isExpBoostReversedFor(player)) {
            int xp = TheAbyssalCognition.getPlayerXP(player);

            float coef = 0.1447F;
            float maxMagic = 3.0F;

            float rawMagic = (float) Math.log(xp + 1) * coef;
            float bounce_factor = Math.min(rawMagic, maxMagic);

            // MAGIC DAMAGE
            IAttributeInstance magicDamage = player.getEntityAttribute(PotionCoreAttributes.MAGIC_DAMAGE);

            if (magicDamage.getModifier(MAGIC_DAMAGE_ADD) != null) {
                magicDamage.removeModifier(MAGIC_DAMAGE_ADD);
            }

            magicDamage.applyModifier(new AttributeModifier(
                    MAGIC_DAMAGE_ADD,
                    "magic_damage_add",
                    bounce_factor,
                    2
            ));


            double K = 1440000.0;
            double resistanceValue = 20.0 * (1.0 - Math.exp(-xp / K));
            resistanceValue = Math.min(resistanceValue, 20.0);

            IAttributeInstance magicShield = player.getEntityAttribute(PotionCoreAttributes.MAGIC_SHIELDING);
            if (magicShield.getModifier(MAGIC_DEFENSE_ADD) != null) {
                magicShield.removeModifier(MAGIC_DEFENSE_ADD);
            }
            magicShield.applyModifier(new AttributeModifier(
                    MAGIC_DEFENSE_ADD,
                    "magic_shield_add",
                    resistanceValue,
                    0
            ));
        }
    }

    public static boolean isArcaneActive(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(TAG_AN_ACTIVE);
    }

    public static void setArcaneActive(ItemStack stack, boolean value) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean(TAG_AN_ACTIVE, value);
    }

    public static boolean isArcaneConvertDrops(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(TAG_AN_CONVERT_DROPS);
    }

    public static void setArcaneConvertDrops(ItemStack stack, boolean enabled) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean(TAG_AN_CONVERT_DROPS, enabled);
    }


    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 0.75F, 1.9F);
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 0.75F, 2.0F);
        TheAbyssalCognition.removeModifier(player.getEntityAttribute(PotionCoreAttributes.MAGIC_DAMAGE), MAGIC_DAMAGE_ADD);
        TheAbyssalCognition.removeModifier(player.getEntityAttribute(PotionCoreAttributes.MAGIC_SHIELDING), MAGIC_DEFENSE_ADD);
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;

        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);

        ItemStack ring = DepthRingHandler.getEquippedDepthRing(player);
        if (ring.isEmpty()) {
            return false; // 未佩戴深渊之戒 → 禁止佩戴
        }
        int equippedIndex = BaublesApi.isBaubleEquipped(player, this);
        if (equippedIndex != -1) {
            return false;
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack other = handler.getStackInSlot(i);

            if (other.isEmpty()) continue;

            if (other.getItem() instanceof TheArcaneAnnihilation) return false;
            if (other.getItem() instanceof ExperienceAttractionScroll) return false;
        }
        return true;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("tooltip.eternal_abyss.pref"));
        tooltip.add("");

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        boolean shift = GuiScreen.isShiftKeyDown();
        boolean alt = GuiScreen.isAltKeyDown();

        int xp = TheAbyssalCognition.getPlayerXP(player);

        // 魔法伤害（和 onWornTick 同公式）
        float coef = 0.1447F;
        float maxMagic = 3.0F;

        float rawMagic = (float) Math.log(xp + 1) * coef * 100F;
        float magicDamageBonus = Math.min(rawMagic, maxMagic * 100F);

        double K = 1440000.0;
        double shield = 20.0 * (1.0 - Math.exp(-xp / K));
        shield = Math.min(shield, 20.0);

        // ===== Shift 区域 =====
        if (shift) {
            if (DepthRing.isExpBoostReversedFor(player)) {
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line1"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line2"));
                tooltip.add("");
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line3"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line4"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line7"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.magic_damage",
                        String.format("%.2f", magicDamageBonus)));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.magic_shield",
                        String.format("%.2f", shield)));
                tooltip.add("");
            } else {
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line1"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line2"));
                tooltip.add("");
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line3"));
                tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.line4"));
                tooltip.add("");
            }

            tooltip.add(I18n.format("tooltip.eternal_abyss.arcane_annihilation.xp",
                    String.format("%,d", xp)));
        }

        // ===== Alt 区域 =====
        if (alt) {
            if (shift) {
                tooltip.add("");
            }

            String brief = I18n.format("tooltip.eternal_abyss.arcane_annihilation.brief");
            int visibleChars = com.zzt.eternal_abyss.client.tooltip.AltRevealState.getVisibleChars(15);
            String partial = revealFormattedText(brief, visibleChars);

            if (partial != null && !partial.isEmpty()) {
                tooltip.add(partial);
            } else {
                tooltip.add(" ");
            }
        }

        // ===== 都没按时，显示两个提示 =====
        if (!shift && !alt) {
            tooltip.add(I18n.format("tooltip.depth_ring.hold_shift"));
            tooltip.add(I18n.format("tooltip.eternal_abyss.holdAlt"));
        }

        tooltip.add("");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return ModFontRenderers.SPECIAL_FONT;
    }

}
