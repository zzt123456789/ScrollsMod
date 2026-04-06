package com.zzt.eternal_abyss.potion;

import com.zzt.eternal_abyss.client.particle.ParticleMiracleBlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.zzt.eternal_abyss.init.ModParticles.MIRACLE_BLIGHT;

public class PotionSwordQi extends Potion {

    public PotionSwordQi() {
        super(false, 0x00FFFF);
        setPotionName("effect.sword_qi");
        setIconIndex(0, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        float dmg = 4.0F * (amplifier + 1);
        entity.attackEntityFrom(DamageSource.MAGIC, dmg);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int interval = 40;
        return duration % interval == 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.getTextureManager().bindTexture(
                new ResourceLocation("eternal_abyss", "textures/potions/sword_qi.png"));
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7,
                0, 0, 18, 18, 18, 18);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect,
                                Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(
                new ResourceLocation("eternal_abyss", "textures/potions/sword_qi.png"));
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3,
                0, 0, 18, 18, 18, 18);
    }



    @Override
    public boolean isBadEffect() {
        return true;
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return false;
    }
}
