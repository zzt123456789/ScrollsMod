package com.zzt.eternal_abyss.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

public class ParticleSupernovaExplosion extends Particle {

    private static final int FRAME_COUNT = 20;
    private final TextureAtlasSprite[] frames;

    public ParticleSupernovaExplosion(
            World world,
            double x, double y, double z,
            TextureAtlasSprite[] frames
    ) {
        super(world, x, y, z);

        this.frames = frames;

        this.particleMaxAge = FRAME_COUNT;
        this.particleAge = 0;

        this.motionX = this.motionY = this.motionZ = 0;

        this.particleScale = 100F;
        this.canCollide = false;
    }

    @Override
    public void onUpdate() {
        if (particleAge >= particleMaxAge) {
            setExpired();
            return;
        }

        this.setParticleTexture(frames[particleAge]);

        particleAge++;
    }

    @Override
    public int getFXLayer() {
        // 使用 texture atlas
        return 1;
    }
}
