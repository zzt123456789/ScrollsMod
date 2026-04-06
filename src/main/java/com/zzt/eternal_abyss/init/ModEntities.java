package com.zzt.eternal_abyss.init;

import com.zzt.eternal_abyss.EternalAbyss;
import com.zzt.eternal_abyss.entity.EntitySupernovaBall;
import com.zzt.eternal_abyss.entity.EntitySwordQi;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {

    private static int id = 0; // 用于递增实体ID

    public static void registerEntities() {
        // 第一个参数：实体类
        // 第二个参数：实体名称（注册名）
        // 第三个参数：唯一ID（每个mod内部唯一即可）
        // 第四个参数：mod实例
        // 第五个参数：追踪距离（客户端渲染跟随距离）
        // 第六个参数：更新频率（tick）
        // 第七个参数：是否发送 velocity 数据
        EntityRegistry.registerModEntity(
                new ResourceLocation(EternalAbyss.MOD_ID, "sword_qi"),
                EntitySwordQi.class,
                "sword_qi",
                id++,
                EternalAbyss.instance,
                64,   // 追踪范围
                1,   // 更新频率
                true  // 是否发送速度信息
        );
        EntityRegistry.registerModEntity(
                new ResourceLocation(EternalAbyss.MOD_ID, "supernova"),
                EntitySupernovaBall.class,
                "supernova_ball",
                id++,
                EternalAbyss.instance,
                64,   // 追踪范围
                1,    // 更新频率（飞行实体建议 1）
                true  // 同步 motion（非常重要）
        );

    }
    
}
