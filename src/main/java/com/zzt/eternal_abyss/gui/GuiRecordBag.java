package com.zzt.eternal_abyss.gui;

import com.zzt.eternal_abyss.container.ContainerRecordBag;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class GuiRecordBag extends GuiContainer {

    private static final ResourceLocation TEX =
            new ResourceLocation("eternal_abyss", "textures/gui/record_bag.png");

    public GuiRecordBag(ContainerRecordBag container) {
        super(container);
        this.xSize = 176;
        this.ySize = 222;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY); // 关键
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(TEX);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString("无余霁楽的唱片包", 8, 6, 0x404040);
        this.fontRenderer.drawString("背包", 8, this.ySize - 96 + 2, 0x404040);
    }
}