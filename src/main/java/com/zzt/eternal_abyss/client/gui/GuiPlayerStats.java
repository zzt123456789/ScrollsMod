package com.zzt.eternal_abyss.client.gui;

import com.tmtravlr.potioncore.PotionCoreAttributes;
import com.zzt.eternal_abyss.container.ContainerPlayerStats;
import com.zzt.eternal_abyss.init.ModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiPlayerStats extends GuiContainer {
    private static final int COLOR_TEXT = 0xD8D6CE;
    private static final ResourceLocation BG =
            new ResourceLocation("eternal_abyss", "textures/gui/stats.png");

    public GuiPlayerStats() {
        super(new ContainerPlayerStats());
        this.xSize = 176;
        this.ySize = 180;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(BG);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        EntityPlayer p = Minecraft.getMinecraft().player;

        int baseX = 12;
        int baseY = 14;
        int gap = 12;

        int row = 0;

        String playerName = p.getDisplayNameString();
        this.fontRenderer.drawString("§e▶ 玩家属性 --- §l" + playerName, baseX, baseY, 0x000000);
        row++;

        /* ----------- 左侧属性 ----------- */
        String[] left = new String[] {
                "生命值：" + fmt(p.getMaxHealth()),
                "幸运值：" + fmt(p.getLuck()),
                "攻击力：" + fmt(p.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()),
                "攻击速度：" + fmt(p.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()),
                "暴击率：" + fmt(p.getEntityAttribute(ModAttributes.CRIT_CHANCE).getAttributeValue() * 100) + "%",
                "暴击伤害：" + fmt(p.getEntityAttribute(ModAttributes.CRIT_DAMAGE).getAttributeValue() * 100) + "%"
        };

        for (String line : left) {
            int y = baseY + row * gap;
            this.fontRenderer.drawString(line, baseX, y, COLOR_TEXT);
            row++;
        }
        try {
            IAttributeInstance proj = p.getEntityAttribute(PotionCoreAttributes.PROJECTILE_DAMAGE);
            if (proj != null) {
                int y = baseY + row * gap;
                this.fontRenderer.drawString("箭矢伤害：" + fmt(proj.getAttributeValue() * 100) + "%", baseX, y, COLOR_TEXT);
                row++;
            }
        } catch (Throwable ignored) {}

        try {
            IAttributeInstance mg = p.getEntityAttribute(PotionCoreAttributes.MAGIC_DAMAGE);
            if (mg != null) {
                int y = baseY + row * gap;
                this.fontRenderer.drawString("魔法伤害：" + fmt(mg.getAttributeValue() * 100) + "%", baseX, y, COLOR_TEXT);
                row++;
            }
        } catch (Throwable ignored) {}

        int rightX = baseX + 90;
        row = 1;

        String[] right = new String[] {
                "护甲值：" + fmt(p.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue()),
                "护甲韧性：" + fmt(p.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()),
                "触及距离：" + fmt(Minecraft.getMinecraft().playerController.getBlockReachDistance()),
                "移动速度：" + fmt(p.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()),
                "维度：" + p.world.provider.getDimensionType().getName()
        };

        for (String line : right) {
            int y = baseY + row * gap;
            this.fontRenderer.drawString(line, rightX, y, COLOR_TEXT);
            row++;
        }

        try {
            IAttributeInstance mdef = p.getEntityAttribute(PotionCoreAttributes.MAGIC_SHIELDING);
            if (mdef != null) {
                int y = baseY + row * gap;
                this.fontRenderer.drawString("魔法抗性：" + fmt(mdef.getAttributeValue()), rightX, y, COLOR_TEXT);
                row++;
            }
        } catch (Throwable ignored) {}


//        try {
//            IAttributeInstance resist = p.getEntityAttribute(PotionCoreAttributes.DAMAGE_RESISTANCE);
//            if (resist != null) {
//                double raw = resist.getAttributeValue();
//                double percent = raw * 100.0;
//
//                percent = Math.min(percent, 100);
//                percent = Math.max(percent, -100);
//
//                int y = baseY + row * gap;
//                this.fontRenderer.drawString("总体抗性：" + fmt(percent) + "%", rightX, y, 0x000000);
//                row++;
//            }
//        } catch (Throwable ignored) {}

    }


    private String fmt(double v) {
        return String.format("%.2f", v);
    }

}
