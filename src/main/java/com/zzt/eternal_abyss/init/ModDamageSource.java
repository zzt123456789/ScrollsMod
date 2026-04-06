package com.zzt.eternal_abyss.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import java.util.Random;

public class ModDamageSource extends DamageSource {

    public static final DamageSource VOID_DAMAGE = new ModDamageSource("abyss_void")
            .setDamageBypassesArmor()
            .setMagicDamage()
            .setDamageIsAbsolute();

    public static final DamageSource ABYSS_SWEEP = new ModDamageSource("abyss_sweep")
            .setDamageBypassesArmor()
            .setMagicDamage();

    protected ModDamageSource(String damageTypeIn) {
        super(damageTypeIn);
    }


    public static DamageSource abyssVoidWithOwner(Entity attacker) {
        return new EntityDamageSource("abyss_void", attacker)
                .setDamageBypassesArmor()
                .setMagicDamage()
                .setDamageIsAbsolute();
    }



    public static DamageSource abyssSweepWithPlayer(EntityPlayer player) {
        return new EntityDamageSource("abyss_sweep", player)
                .setMagicDamage()
                .setDamageBypassesArmor();
    }


    public static DamageSource projectileWithPlayer(Entity attacker) {
        return new EntityDamageSource("projectile", attacker)
                .setExplosion()
                .setProjectile();
    }

    public static final DamageSource VOID_CURSE = (new ModDamageSource("void_curse")).setDamageBypassesArmor();



}
