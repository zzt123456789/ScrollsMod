package com.zzt.eternal_abyss.client;

import com.zzt.eternal_abyss.client.render.layer.LayerBackImage;
import com.zzt.eternal_abyss.client.render.layer.LayerHeadCrown;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Mod.EventBusSubscriber(modid = "eternal_abyss", value = Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderPlayer(RenderPlayerEvent.Specials.Post event) {
        RenderPlayer renderer = event.getRenderer();

        try {
            @SuppressWarnings("unchecked")
            List<LayerRenderer<?>> layers = (List<LayerRenderer<?>>)
                    ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class, renderer, "layerRenderers");

            boolean hasBack = false;
            boolean hasCrown = false;

            for (LayerRenderer<?> l : layers) {
                if (l instanceof LayerBackImage) {
                    hasBack = true;
                } else if (l instanceof LayerHeadCrown) {
                    hasCrown = true;
                }
            }

            if (!hasBack) {
                renderer.addLayer(new LayerBackImage());
            }
            if (!hasCrown) {
                renderer.addLayer(new LayerHeadCrown());
            }

        } catch (Exception e) {
            // 如果反射失败则直接添加两层，避免渲染缺失
            renderer.addLayer(new LayerBackImage());
            renderer.addLayer(new LayerHeadCrown());
        }
    }
}
