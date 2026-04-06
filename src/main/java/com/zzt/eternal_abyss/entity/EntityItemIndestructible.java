package com.zzt.eternal_abyss.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityItemIndestructible extends EntityItem {

    public EntityItemIndestructible(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = true; // 设置为免疫火焰
        this.setNoDespawn();        // 永不消失（可选）
    }

    public EntityItemIndestructible(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
        this.isImmuneToFire = true; // 设置为免疫火焰
        this.setNoDespawn();        // 永不消失（可选）
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // 防爆炸、防火、防岩浆等
        if (source.isFireDamage() || source.isExplosion() || source == DamageSource.LAVA) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }
}
