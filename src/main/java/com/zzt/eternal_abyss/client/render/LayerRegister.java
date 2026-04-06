package com.zzt.eternal_abyss.client.render;

import com.zzt.eternal_abyss.client.render.layer.LayerBackImage;
import com.zzt.eternal_abyss.client.render.layer.LayerHeadCrown;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class LayerRegister {

    private final Set<RenderPlayer> injected = new HashSet<>();

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        RenderPlayer render = event.getRenderer();

        // 确保每个 RenderPlayer 只注入一次
        if (!injected.contains(render)) {

            render.addLayer(new LayerBackImage());
            render.addLayer(new LayerHeadCrown());


            injected.add(render);

            System.out.println("[CelestialArtifacts] Layers injected into RenderPlayer: " + render);
        }
    }
}
