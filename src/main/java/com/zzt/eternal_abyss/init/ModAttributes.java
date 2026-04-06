package com.zzt.eternal_abyss.init;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public class ModAttributes {

    // 暴击率（0.0 = 0%，1.0 = 100%）
    public static final IAttribute CRIT_CHANCE = new RangedAttribute(
            null,
            "eternal_abyss.crit_chance",
            0.05D,
            0.0D,
            1024.0D
    ).setDescription("Critical Chance").setShouldWatch(true);

    // 暴击伤害（1.5 = 150%，2.0 = 200%）
    public static final IAttribute CRIT_DAMAGE = new RangedAttribute(
            null,
            "eternal_abyss.crit_damage",
            1.5D,
            1.0D,
            1024.0D
    ).setDescription("Critical Damage").setShouldWatch(true);

}
