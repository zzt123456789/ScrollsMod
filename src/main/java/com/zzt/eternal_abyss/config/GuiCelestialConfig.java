package com.zzt.eternal_abyss.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class GuiCelestialConfig extends GuiConfig {

    public GuiCelestialConfig(GuiScreen parent) {
        super(
                parent,
                getAllCategories(),
                "eternal_abyss", // modid
                false,
                false,
                "Celestial Artifacts 配置"
        );
    }

    private static List<IConfigElement> getAllCategories() {
        List<IConfigElement> elements = new ArrayList<>();

        Configuration cfg = ModConfig.getConfig();

        // 通用
        elements.add(new ConfigElement(cfg.getCategory(Configuration.CATEGORY_GENERAL)));

        // 各诅咒分类
        elements.add(new ConfigElement(cfg.getCategory("DamageSpeed")));
        elements.add(new ConfigElement(cfg.getCategory("ExperienceDrop")));
        elements.add(new ConfigElement(cfg.getCategory("HauntedShadows")));
        elements.add(new ConfigElement(cfg.getCategory("VoidResistance")));
        elements.add(new ConfigElement(cfg.getCategory("Lucky")));
        elements.add(new ConfigElement(cfg.getCategory("Final")));
        elements.add(new ConfigElement(cfg.getCategory("record_bag")));
        elements.add(new ConfigElement(cfg.getCategory("crit")));

        return elements;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (ModConfig.getConfig().hasChanged()) {
            ModConfig.getConfig().save();
        }
    }
}
