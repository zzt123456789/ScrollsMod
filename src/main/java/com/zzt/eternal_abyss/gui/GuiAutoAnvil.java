package com.zzt.eternal_abyss.gui;

import com.zzt.eternal_abyss.container.ContainerAutoAnvil;
import com.zzt.eternal_abyss.tileentity.TileEntityAutoAnvil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.translation.I18n;
import com.zzt.eternal_abyss.util.AnvilHelper;

import java.util.List;

public class GuiAutoAnvil extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("eternal_abyss:textures/gui/auto_anvil.png");
    private final InventoryPlayer playerInventory;
    private final TileEntityAutoAnvil tileAutoAnvil;

    public GuiAutoAnvil(InventoryPlayer playerInventory, TileEntityAutoAnvil tileEntity) {
        super(new ContainerAutoAnvil(playerInventory, tileEntity));
        this.playerInventory = playerInventory;
        this.tileAutoAnvil = tileEntity;
        this.xSize = 176;
        this.ySize = 166;
    }
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18n.translateToLocal("container.auto_anvil");
        this.fontRenderer.drawString(name, 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

        int cost = tileAutoAnvil.getField(0);
        if (cost > 0) {
            int color = 0x80FF20; // 绿色

            EntityPlayer owner = tileAutoAnvil.getBoundPlayer();
            if (owner == null || owner.experienceLevel < cost) {
                color = 0xFF4040; // 红色
            }

            String costText = I18n.translateToLocalFormatted("container.repair.cost", cost);

            // ⭐关闭灯光和深度测试（非常重要，否则显示异常）
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();

            // ⭐让文字居中（xSize是整个GUI宽度）
            int stringWidth = this.fontRenderer.getStringWidth(costText);
            int xPos = (this.xSize / 2) - (stringWidth / 2);
            this.fontRenderer.drawString(costText, xPos, 20, color);

            // ⭐恢复灯光和深度测试
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
        }
    }



    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 先绘制 GUI 背景和槽位
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);


        // 渲染悬浮提示
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
