package com.zzt.eternal_abyss.entity;

import com.zzt.eternal_abyss.config.ModConfig;
import com.zzt.eternal_abyss.init.ModPotions;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.zzt.eternal_abyss.util.TargetBlacklistUtil;

import java.util.LinkedList;
import java.util.List;

public class EntitySwordQi extends Entity {

    private int lifeTicks = 150;
    private EntityLivingBase target = null;

    private float baseSpeed = 0.5F;
    private float homingSpeedMultiplier = 3F;
    private double homingRange = 8.0D;

    private int hitCount = 0;
    private int maxHits = 4;

    private EntityPlayer shooter;
    private ItemStack renderStack = ItemStack.EMPTY;

    private int homingDelayTicks = 4;

    public final LinkedList<Vec3d> trail = new LinkedList<>();
    private static final int TRAIL_SIZE = 100;


    public EntitySwordQi(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
    }

    public EntitySwordQi(World worldIn, EntityPlayer shooter) {
        this(worldIn);
        this.shooter = shooter;
        this.setPosition(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ);
    }

    public void shoot(Vec3d direction, double velocity) {
        Vec3d dir = direction.normalize();
        this.motionX = dir.x * velocity;
        this.motionY = dir.y * velocity;
        this.motionZ = dir.z * velocity;
    }


    public ItemStack getRenderStack() {
        return renderStack;
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}


    @Override
    public void onUpdate() {
        super.onUpdate();


        // 记录当前位置到拖尾链表
        trail.add(new Vec3d(posX, posY, posZ));
        if (trail.size() > TRAIL_SIZE) {
            trail.removeFirst();
        }

        // 目标失效
        if (target != null) {
            if (target.isDead || target.getHealth() <= 0F || TargetBlacklistUtil.isBlacklisted(target)) {
                target = null;
            }
        }

        this.noClip = true;

        // 寻找新目标
        if (target == null && ticksExisted > homingDelayTicks) {
            List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(posX - homingRange, posY - homingRange, posZ - homingRange,
                            posX + homingRange, posY + homingRange, posZ + homingRange));

            double min = Double.MAX_VALUE;
            for (EntityLivingBase e : list) {
                if (e == shooter) continue;
                if (e.isDead || e.getHealth() <= 0F) continue;
                if (TargetBlacklistUtil.isBlacklisted(e)) continue;

                double d = this.getDistance(e);
                if (d < min) {
                    min = d;
                    target = e;
                }
            }
        }

        // 追踪逻辑
        if (target != null) {

            Vec3d dir = new Vec3d(
                    target.posX - posX,
                    target.posY + target.getEyeHeight() * 0.5 - posY,
                    target.posZ - posZ
            ).normalize();

            Vec3d current = new Vec3d(motionX, motionY, motionZ);
            double speed = current.length();
            if (speed <= 0.001) speed = baseSpeed;

            double turnRate = 0.3;
            Vec3d newMotion = current.scale(1 - turnRate).add(dir.scale(speed * homingSpeedMultiplier * turnRate));

            motionX = newMotion.x;
            motionY = newMotion.y;
            motionZ = newMotion.z;
        }

        // 移动
        this.move(MoverType.SELF, motionX, motionY, motionZ);

        // 攻击
        List<EntityLivingBase> hit = world.getEntitiesWithinAABB(EntityLivingBase.class,
                new AxisAlignedBB(posX - 0.25, posY - 0.25, posZ - 0.25,
                        posX + 0.25, posY + 0.25, posZ + 0.25));

        float damage = 2.0F;
        if (shooter != null && shooter.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            damage = (float) (shooter.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 0.35F);
        }

        for (EntityLivingBase e : hit) {
            if (e == shooter) continue;
            if (TargetBlacklistUtil.isBlacklisted(e)) continue;
            if (e.isDead || e.getHealth() <= 0F) continue;

            e.hurtResistantTime = 2;
            e.addPotionEffect(
                    new PotionEffect(
                            ModPotions.SWORD_QI,
                            200,
                            1,
                            false,
                            false
                    )
            );
            e.attackEntityFrom(DamageSource.causePlayerDamage(shooter), damage);

            hitCount++;
            if (hitCount >= maxHits) {
                setDead();
                break;
            }
        }

        // 生命周期
        lifeTicks--;
        if (lifeTicks <= 0) setDead();
    }
}
