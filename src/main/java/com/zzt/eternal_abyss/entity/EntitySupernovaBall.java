package com.zzt.eternal_abyss.entity;

import com.zzt.eternal_abyss.client.particle.ParticleSupernovaExplosion;
import com.zzt.eternal_abyss.init.ModParticles;
import com.zzt.eternal_abyss.init.ModPotions;
import com.zzt.eternal_abyss.init.ModSounds;
import com.zzt.eternal_abyss.util.TargetBlacklistUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class EntitySupernovaBall extends Entity {
    private State state = State.FLYING;
    private boolean exploding = false;
    private int explodeWarmupTicks = 0;
    protected static Random itemRand = new Random();
    private int idleTicks = 0;
    private int homingTicks = 0;
    private double homingSpeed = 0;
    public float spinYaw = 0F;
    private boolean explosionParticleSpawned = false;
    private static final DataParameter<Boolean> DATA_EXPLODING =
            EntityDataManager.createKey(EntitySupernovaBall.class, DataSerializers.BOOLEAN);


    private enum State {
        FLYING,
        BRAKING,
        HOMING,
        IDLE
    }

    private EntityLivingBase owner;
    private EntityLivingBase target;
    private float charge;

    // Forge / 网络 / 客户端必须要的无参构造
    public EntitySupernovaBall(World world) {
        super(world);
        this.setSize(0.6F, 0.6F);
    }

    // 你真正用来生成实体的构造器
    public EntitySupernovaBall(World world, EntityLivingBase owner, float charge) {
        this(world); // 调用上面的构造器
        this.owner = owner;
        this.charge = charge;

        this.setPosition(
                owner.posX,
                owner.posY + owner.getEyeHeight(),
                owner.posZ
        );
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(DATA_EXPLODING, false);
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}

    public void shoot(EntityLivingBase shooter, float pitch, float yaw, float velocity) {

        // 角度 → 方向向量
        float fYaw = yaw * 0.017453292F;
        float fPitch = pitch * 0.017453292F;

        double x = -Math.sin(fYaw) * Math.cos(fPitch);
        double y = -Math.sin(fPitch);
        double z =  Math.cos(fYaw) * Math.cos(fPitch);

        // 归一化
        Vec3d dir = new Vec3d(x, y, z).normalize();

        // 初始速度
        this.motionX = dir.x * velocity;
        this.motionY = dir.y * velocity;
        this.motionZ = dir.z * velocity;

        // 朝向
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;

        this.prevRotationYaw = yaw;
        this.prevRotationPitch = pitch;
    }

    @Override
    public void onUpdate() {

        if (world.isRemote) {
            if (this.dataManager.get(DATA_EXPLODING) && !explosionParticleSpawned) {
                spawnExplosionParticle();
            }
        }

        super.onUpdate();
        if (world.isRemote) {
            spinYaw += 20F; // 每 tick 转 20°
            if (spinYaw >= 360F) spinYaw -= 360F;
        }

        if (exploding) {
            explodeWarmupTicks++;
            doSuction();

            if (explodeWarmupTicks >= getExplodeWarmup()) {
                explode();
            }
            return;
        }

        // onUpdate 里
        if ((state == State.FLYING || state == State.BRAKING) && !exploding) {
            findTarget();
        }




        switch (state) {
            case FLYING:
                if (ticksExisted > 20) {
                    if (target != null) {
                        state = State.BRAKING;
                    } else {
                        state = State.IDLE;
                        idleTicks = 0;
                    }
                }
                break;


            case BRAKING:
                motionX *= 0.9;
                motionY *= 0.9;
                motionZ *= 0.9;

                if (getSpeed() < 0.03) {
                    motionX = motionY = motionZ = 0;

                    if (target != null && target.isEntityAlive()) {

                        if (!world.isRemote) {
                            world.playSound(
                                    null,
                                    posX, posY, posZ,
                                    ModSounds.TARGET_FOUND,
                                    SoundCategory.PLAYERS,
                                    1.0F,
                                    0.9F + itemRand.nextFloat() * 0.2F
                            );
                        }

                        state = State.HOMING;
                        homingTicks = 0;
                        homingSpeed = 0.5;

                    } else {
                        state = State.IDLE;
                    }
                }
                break;


            case HOMING:
                if (target == null || !target.isEntityAlive()) {
                    state = State.IDLE;
                    break;
                }

                Vec3d toTarget = new Vec3d(
                        target.posX - posX,
                        target.posY + target.height * 0.5 - posY,
                        target.posZ - posZ
                );

                double dist = toTarget.length();
                if (dist < 0.001) break;

                Vec3d dir = toTarget.normalize();

                Vec3d vel = new Vec3d(motionX, motionY, motionZ);
                double speed = vel.length();

                // ===== ① 永远存在的“推进力” =====
                double thrust =
                        0.08 + charge * 0.04;   // 基础推力

                // 距离越近，推力越强（不会归零）
                thrust *= (1.0 + 1.5 / (dist + 0.5));

                Vec3d forwardForce = dir.scale(thrust);

                // ===== ② 方向修正力（负责转向）=====
                Vec3d turnForce = Vec3d.ZERO;
                if (speed > 0.01) {
                    Vec3d velDir = vel.normalize();
                    Vec3d correction = dir.subtract(velDir);
                    turnForce = correction.scale(0.25);
                }

                // ===== ③ 合成力 =====
                Vec3d force = forwardForce.add(turnForce);

                motionX += force.x;
                motionY += force.y;
                motionZ += force.z;

                // ===== ④ 速度上限 =====
                double maxSpeed = 1.4 + charge * 0.4;
                Vec3d newVel = new Vec3d(motionX, motionY, motionZ);
                if (newVel.length() > maxSpeed) {
                    newVel = newVel.normalize().scale(maxSpeed);
                    motionX = newVel.x;
                    motionY = newVel.y;
                    motionZ = newVel.z;
                }

                break;

            case IDLE:
                idleTicks++;

                // 可以顺便做个小收缩/暗淡（留给渲染）
                if (idleTicks > 40) { // 0.5 秒
                    setDead();
                }
                break;


        }

        // 移动
        applyPhysics();


        // 方块命中
        if (this.collidedHorizontally || this.collidedVertically) {
            startExplode();
            return;

        }


        // === 实体命中检测 ===
        List<EntityLivingBase> hit = world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                getEntityBoundingBox().grow(0.2)
        );

        for (EntityLivingBase e : hit) {

            if (e == owner) continue;
            if (!e.isEntityAlive()) continue;

            if (TargetBlacklistUtil.isBlacklisted(e)) {
                continue; // 黑名单命中：忽略
            }

            // 非黑名单实体：直接爆
            startExplode();
            return;

        }


    }


    private double getSpeed() {
        return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
    }
    private void findTarget() {
        if (target != null && target.isEntityAlive()) return;

        EntityLivingBase oldTarget = target;
        target = null;

        double range = 16 + charge * 8;
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                getEntityBoundingBox().grow(range)
        );

        double minDist = Double.MAX_VALUE;

        for (EntityLivingBase e : list) {
            if (e == owner) continue;
            if (!e.isEntityAlive()) continue;
            if (TargetBlacklistUtil.isBlacklisted(e)) continue;

            double d = getDistance(e);
            if (d < minDist) {
                minDist = d;
                target = e;
            }
        }
    }


    private void explode() {

        if (!world.isRemote) {
            world.playSound(
                    null,
                    posX, posY, posZ,
                    ModSounds.SUPERNOVA_EXPLODE,
                    SoundCategory.PLAYERS,
                    1.0F,
                    0.9F + itemRand.nextFloat() * 0.2F
            );
        }


        DamageSource src;
        if (owner instanceof EntityPlayer) {
            // “玩家释放的超新星球造成伤害”：归属玩家，直接来源是 this（超新星球）
            src = new EntityDamageSourceIndirect("supernova_magic", this, owner)
                    .setMagicDamage()
                    .setExplosion();
        } else if (owner != null) {
            // 非玩家也保留归属（比如某些召唤物/怪物释放）
            src = new EntityDamageSourceIndirect("supernova_magic", this, owner)
                    .setMagicDamage()
                    .setExplosion();
        } else {
            // 没有归属：环境魔法爆炸
            src = new DamageSource("supernova_magic")
                    .setMagicDamage()
                    .setExplosion();
        }

        float radius = 6.0F + charge * 2.0F;
        float baseDamage = 15.0F + charge * 10.0F;

        List<EntityLivingBase> list = world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                getEntityBoundingBox().grow(radius)
        );

        for (EntityLivingBase e : list) {

            if (e == owner) continue;
            if (!e.isEntityAlive()) continue;
            if (TargetBlacklistUtil.isBlacklisted(e)) continue;

            double dist = this.getDistance(e);
            float factor = 1.0F - (float)(dist / radius);
            factor = Math.max(0F, factor);

            float damage = baseDamage * factor;

            e.attackEntityFrom(src, damage);

            PotionEffect old = e.getActivePotionEffect(ModPotions.SWORD_QI);
            if (old == null || old.getAmplifier() < 1 || old.getDuration() < 100) {
                e.addPotionEffect(new PotionEffect(ModPotions.SWORD_QI, 200, 1, false, false));
            }
        }

        setDead();
    }


    private void doSuction() {

        float radius = 6F + charge * 2F;
        // 0 → 1

        double strength = getSuctionStrength();



        List<EntityLivingBase> list = world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                getEntityBoundingBox().grow(radius)
        );

        for (EntityLivingBase e : list) {

            if (e == owner) continue;
            if (!e.isEntityAlive()) continue;
            if (TargetBlacklistUtil.isBlacklisted(e)) continue;

            Vec3d toCenter = new Vec3d(
                    posX - e.posX,
                    posY - (e.posY + e.height * 0.5),
                    posZ - e.posZ
            );

            double dist = toCenter.length();
            if (dist < 0.1) continue;


            double factor = 1.0 - dist / radius;
            factor = factor * factor;

            Vec3d pull = toCenter.normalize()
                    .scale(strength * factor);


            e.motionX += pull.x;
            e.motionY += pull.y;
            e.motionZ += pull.z;

            // 防止被原速度“逃逸”
            e.motionX *= 0.85;
            e.motionY *= 0.85;
            e.motionZ *= 0.85;

            e.velocityChanged = true;
        }
    }


    private int getExplodeWarmup() {
        // 低蓄力 3 tick，高蓄力最多 5 tick
        return 2;
    }


    private double getSuctionStrength() {
        // 直接给终极吸力
        return 1.5 + charge * 0.25;
    }


    private void applyPhysics() {
        this.move(MoverType.SELF, motionX, motionY, motionZ);

        motionX *= 0.96;
        motionY *= 0.96;
        motionZ *= 0.96;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }


    private void startExplode() {
        if (exploding) return;

        exploding = true;
        explodeWarmupTicks = 0;

        motionX = motionY = motionZ = 0;
        velocityChanged = true;

        if (!world.isRemote) {
            this.dataManager.set(DATA_EXPLODING, true);
        }

        // 客户端如果本地也触发到（比如命中实体时可能会），也可以立即播
        if (world.isRemote) {
            spawnExplosionParticle();
        }
    }



    @SideOnly(Side.CLIENT)
    private void spawnExplosionParticle() {
        if (explosionParticleSpawned) return;

        if (ModParticles.SUPERNOVA_FRAMES != null) {
            Minecraft.getMinecraft().effectRenderer.addEffect(
                    new ParticleSupernovaExplosion(
                            world,
                            posX,
                            posY,
                            posZ,
                            ModParticles.SUPERNOVA_FRAMES
                    )
            );
        }

        explosionParticleSpawned = true;
    }

}
