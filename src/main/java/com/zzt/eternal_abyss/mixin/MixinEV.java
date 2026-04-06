package com.zzt.eternal_abyss.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "team.creative.enhancedvisuals.client.render.EVRenderer", remap = false)
public abstract class MixinEV {

    @Unique private static Item ca$twistedFate;
    @Unique private static Boolean ca$baublesLoaded;
    @Unique private static Method ca$getBaublesHandler;
    @Unique private static Method ca$getSlots;
    @Unique private static Method ca$getStackInSlot;

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void ca$skipOverlay(TickEvent.RenderTickEvent event, CallbackInfo ci) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        if (ca$twistedFate == null) {
            ca$twistedFate = Item.getByNameOrId("eternal_abyss:the_twisted_fate");
            if (ca$twistedFate == null) return;
        }

        Object baubles = ca$getBaubles(player);
        if (baubles == null) return;

        try {
            int slots = (Integer) ca$getSlots.invoke(baubles);
            for (int i = 0; i < slots; i++) {
                ItemStack stack = (ItemStack) ca$getStackInSlot.invoke(baubles, i);
                if (stack != null && !stack.isEmpty() && stack.getItem() == ca$twistedFate) {
                    ci.cancel();
                    return;
                }
            }
        } catch (Throwable ignored) {}
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
                Class<?> api = Class.forName("baubles.api.BaublesApi");
                ca$getBaublesHandler =
                        api.getMethod("getBaublesHandler", EntityPlayer.class);
            }

            Object handler = ca$getBaublesHandler.invoke(null, player);
            if (handler == null) return null;

            if (ca$getSlots == null) {
                ca$getSlots = handler.getClass().getMethod("getSlots");
                ca$getStackInSlot =
                        handler.getClass().getMethod("getStackInSlot", int.class);
            }

            return handler;
        } catch (Throwable t) {
            return null;
        }
    }
}
