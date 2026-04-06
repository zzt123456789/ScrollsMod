package com.zzt.eternal_abyss.client.particle;

import com.zzt.eternal_abyss.init.ModParticles;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleMiracleBlight extends Particle {

    public ParticleMiracleBlight(World world,
                                 double x, double y, double z,
                                 double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);

        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;

        this.particleScale = 0.45F + world.rand.nextFloat() * 0.6F;
        this.particleMaxAge = 5;          // 非常短的寿命
        this.canCollide = false;
        this.particleGravity = 0.0F;
        this.particleAlpha = 1.0F;

        this.setParticleTexture(ModParticles.MIRACLE_BLIGHT);
    }

    @Override
    public void onUpdate() {

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        // 位置更新
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;


        // 渐隐
        this.particleAlpha = 1.0F - (float)this.particleAge / (float)this.particleMaxAge;

        // 微缩
        this.particleScale *= 0.96F;

        if (++this.particleAge >= this.particleMaxAge) {
            this.setExpired();
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }
}