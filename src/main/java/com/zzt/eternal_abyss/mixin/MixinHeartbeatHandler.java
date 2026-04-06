package com.zzt.eternal_abyss.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "team.creative.enhancedvisuals.common.handler.HeartbeatHandler", remap = false)
public abstract class MixinHeartbeatHandler {

    @Unique private static Boolean ca$baublesLoaded = null;
    @Unique private static Method ca$getBaublesHandler = null;
    @Unique private static Method ca$getSlots = null;
    @Unique private static Method ca$getStackInSlot = null;

    @Unique private static Item ca$twistedFateItem = null;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void ca$disableHeartbeat(EntityPlayer player, CallbackInfo ci) {
        if (player == null) return;

        // 目标饰品（用注册名取，避免直接引用 ModItems）
        if (ca$twistedFateItem == null) {
            ca$twistedFateItem = Item.getByNameOrId("eternal_abyss:the_twisted_fate");
            if (ca$twistedFateItem == null) return;
        }

        // 运行期检测 Baubles 是否存在，并反射读取饰品栏
        Object baublesHandler = ca$getBaubles(player);
        if (baublesHandler == null) return;

        try {
            int slots = (Integer) ca$getSlots.invoke(baublesHandler);
            for (int i = 0; i < slots; i++) {
                ItemStack stack = (ItemStack) ca$getStackInSlot.invoke(baublesHandler, i);
                if (stack == null || stack.isEmpty()) continue;

                if (stack.getItem() == ca$twistedFateItem) {
                    ci.cancel();
                    return;
                }
            }
        } catch (Throwable ignored) {
            // 任何反射失败都不影响游戏启动
        }
    }

    @Unique
    private static Object ca$getBaubles(EntityPlayer player) {
        try {
            if (ca$baublesLoaded == null) {
                try {
                    Class.forName("baubles.api.BaublesApi");
                    ca$baublesLoaded = true;
                } catch (Throwable t) {
                    ca$baublesLoaded = false;
                }
            }
            if (!ca$baublesLoaded) return null;

            if (ca$getBaublesHandler == null) {
                Class<?> baublesApi = Class.forName("baubles.api.BaublesApi");
                ca$getBaublesHandler = baublesApi.getMethod("getBaublesHandler", EntityPlayer.class);
            }

            Object handler = ca$getBaublesHandler.invoke(null, player);
            if (handler == null) return null;

            if (ca$getSlots == null || ca$getStackInSlot == null) {
                // handler 通常是 IBaublesItemHandler，实现了 getSlots/getStackInSlot
                ca$getSlots = handler.getClass().getMethod("getSlots");
                ca$getStackInSlot = handler.getClass().getMethod("getStackInSlot", int.class);
            }

            return handler;
        } catch (Throwable t) {
            return null;
        }
    }
}
