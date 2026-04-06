package com.zzt.eternal_abyss.init;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModParticles {

    public static TextureAtlasSprite[] SUPERNOVA_FRAMES;
    public static TextureAtlasSprite MIRACLE_BLIGHT;

    @SubscribeEvent
    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        for (int i = 1; i <= 20; i++) {
            event.getMap().registerSprite(
                    new ResourceLocation(
                            "eternal_abyss",
                            "particles/biomech_reactor_particle_" + i
                    )
            );
        }

        event.getMap().registerSprite(
                new ResourceLocation(
                        "eternal_abyss",
                        "particles/slash"
                )
        );
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        SUPERNOVA_FRAMES = new TextureAtlasSprite[20];
        for (int i = 0; i < 20; i++) {
            SUPERNOVA_FRAMES[i] = event.getMap().getAtlasSprite(
                    "eternal_abyss:particles/biomech_reactor_particle_" + (i + 1)
            );
        }

        MIRACLE_BLIGHT = event.getMap().getAtlasSprite(
                "eternal_abyss:particles/slash"
        );
    }
}
