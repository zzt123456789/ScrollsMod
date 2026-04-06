package com.zzt.eternal_abyss.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ImageButton extends GuiButton {

    private final ResourceLocation texture;
    private final int u, v, texW, texH;

    public ImageButton(int id, int x, int y, int w, int h,
                       ResourceLocation tex,
                       int u, int v,
                       int texW, int texH)
    {
        super(id, x, y, w, h, "");
        this.texture = tex;
        this.u = u;
        this.v = v;
        this.texW = texW;
        this.texH = texH;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        mc.getTextureManager().bindTexture(texture);

        boolean hover = mouseX >= x && mouseX < x + width &&
                mouseY >= y && mouseY < y + height;

        float alpha = hover ? 1.0F : 0.85F;
        GlStateManager.color(alpha, alpha, alpha, 1F);

        drawScaledCustomSizeModalRect(
                x, y,
                u, v,
                width, height,
                width, height,
                texW, texH
        );
    }
}
