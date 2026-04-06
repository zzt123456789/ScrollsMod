package com.zzt.eternal_abyss.init;

import com.zzt.eternal_abyss.items.*;
import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;

/**
 * 物品实例集中管理，不负责注册，注册逻辑由 RegistryItems 完成
 */
// ModItems.java
public class ModItems {

    public static final Item.ToolMaterial EXOBLADE_MATERIAL = EnumHelper.addToolMaterial(
            "EXOBLADE", 3, 6666, 8.0F, 25.0F, 15);

    public static final Item.ToolMaterial SUPERNOVA_MATERIAL = EnumHelper.addToolMaterial(
            "SUPERNOVA", 3, 6666, 8.0F, -4F, 15);


    public static final Item CATASTROPHE_SCROLL = new CatastropheScroll();
    public static final Item EXPERIENCE_ATTRACTION_SCROLL = new ExperienceAttractionScroll();

    public static final Item DEPTH_RING = new DepthRing();

    public static final Item VILLAGER_UNLOCKER = new VillagerUnlocker("villager_unlocker");

    public static final Item LUCKIFY_SCROLL = new LuckifyScroll();

    public static final Item LUCK_MODIFIER = new LuckModifier("luck_modifier");

    public static final Item ABYSSAL_ESSENCE = new AbyssalEssence("abyssal_essence");


    public static final Item WAILING_CORE = new WailingCore("wailing_core");

    public static final Item INSANE_ABYSS_DICE = new InsaneAbyssDice("insane_abyss_dice");

    public static final Item CLOVER = new Clover();

    public static final Item VOID_ARTIFACT = new VoidArtifact("void_artifact");

    public static final Item EXOBLADE = new Exoblade(EXOBLADE_MATERIAL,"exoblade");
    public static final Item SUPERNOVA = new Supernova(SUPERNOVA_MATERIAL,"supernova");


    public static final Item THE_TWISTED_FATE = new TheTwistedFate("the_twisted_fate");

    public static final Item THE_DIMINISHED_SHADE = new TheDiminishedShade("the_diminished_shade");

    public static final Item THE_ABYSSAL_COGNITION = new TheAbyssalCognition("the_abyssal_cognition");

    public static final Item THE_ARCANE_ANNIHILATION = new TheArcaneAnnihilation("the_arcane_annihilation");

    public static final Item THE_SORROWED_SHRIEK = new TheSorrowedShriek("the_sorrowed_shriek");

    public static final Item YUWAN_RING = new YuwanRing("yuwan_ring");

    public static final Item RECORD_BAG = new RecordBag("record_bag");


}