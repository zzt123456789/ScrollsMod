package com.zzt.eternal_abyss;


import com.zzt.eternal_abyss.client.CritDisplay;
import com.zzt.eternal_abyss.client.KeyBindings;
import com.zzt.eternal_abyss.client.KeyInputHandler;
import com.zzt.eternal_abyss.client.render.LayerRegister;
import com.zzt.eternal_abyss.command.CommandDepthRingDebug;
import com.zzt.eternal_abyss.config.ModConfig;
import com.zzt.eternal_abyss.entity.EntitySupernovaBall;
import com.zzt.eternal_abyss.entity.EntitySwordQi;
import com.zzt.eternal_abyss.event.*;
import com.zzt.eternal_abyss.font.ModFontRenderers;
import com.zzt.eternal_abyss.init.ModBlocks;
import com.zzt.eternal_abyss.init.ModEntities;
import com.zzt.eternal_abyss.init.ModRecipes;
import com.zzt.eternal_abyss.items.DepthRing;
import com.zzt.eternal_abyss.network.PacketToggleItemCollect;
import com.zzt.eternal_abyss.packages.PacketCritMessage;
import com.zzt.eternal_abyss.packages.PacketSyncBaubleNBT;
import com.zzt.eternal_abyss.recordbag.RecordBagEffectRegistry;
import com.zzt.eternal_abyss.registry.GuiHandler;
import com.zzt.eternal_abyss.render.RenderSupernovaBall;
import com.zzt.eternal_abyss.render.RenderSwordQi;
import com.zzt.eternal_abyss.tileentity.TileEntityAutoAnvil;
import com.zzt.eternal_abyss.tileentity.TileEntityXPDrain;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
@Mod(
        modid = EternalAbyss.MOD_ID,
        name = EternalAbyss.NAME,
        version = EternalAbyss.VERSION,
        guiFactory = "com.zzt.eternal_abyss.config.ModGuiFactory"

)
public class EternalAbyss {
    public static final String MOD_ID = "eternal_abyss";
    public static final String NAME = "Eternal Abyss";
    public static final String VERSION = "3.3.3.5";
    public static SimpleNetworkWrapper NETWORK;


//    static {
//        org.spongepowered.asm.launch.MixinBootstrap.init();
//        org.spongepowered.asm.mixin.Mixins.addConfiguration(
//                "mixins.eternal_abyss.json"
//        );
//    }

    @Mod.Instance(MOD_ID)
    public static EternalAbyss instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        /* ===================== 基础注册 ===================== */

        MinecraftForge.EVENT_BUS.register(com.zzt.eternal_abyss.event.GlobalEvents.class);
        MinecraftForge.EVENT_BUS.register(ModBlocks.class);

        ModEntities.registerEntities();

        GameRegistry.registerTileEntity(
                TileEntityAutoAnvil.class,
                new ResourceLocation(MOD_ID, "auto_anvil")
        );

        GameRegistry.registerTileEntity(
                TileEntityXPDrain.class,
                new ResourceLocation(MOD_ID, "xp_drain")
        );

        ModConfig.init(new File(event.getModConfigurationDirectory(), "Eternal Abyss.cfg"));
        RecordBagEffectRegistry.reload();
        NetworkRegistry.INSTANCE.registerGuiHandler(
                EternalAbyss.instance,
                new GuiHandler()
        );

        MinecraftForge.EVENT_BUS.register(DepthRing.class);
        MinecraftForge.EVENT_BUS.register(new DropHandler());
        MinecraftForge.EVENT_BUS.register(new VoidArtifactHandler());
        MinecraftForge.EVENT_BUS.register(new TheTwistedFateHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerAttributeHandler());
        MinecraftForge.EVENT_BUS.register(new TheDiminishedShadeHandler());

        /* ===================== 客户端专用 ===================== */

        if (event.getSide().isClient()) {
            RenderingRegistry.registerEntityRenderingHandler(
                    EntitySwordQi.class,
                    RenderSwordQi::new
            );
            RenderingRegistry.registerEntityRenderingHandler(
                    EntitySupernovaBall.class,
                    RenderSupernovaBall::new
            );

            MinecraftForge.EVENT_BUS.register(CritDisplay.class);
            MinecraftForge.EVENT_BUS.register(new LayerRegister());

            KeyBindings.init();
            MinecraftForge.EVENT_BUS.register(new KeyInputHandler());

            net.minecraftforge.client.model.obj.OBJLoader.INSTANCE
                    .addDomain(MOD_ID);

        }

        /* ===================== 网络初始化（唯一入口） ===================== */

        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("ca");

        // C → S：切换吸取模式
        NETWORK.registerMessage(
                PacketToggleItemCollect.Handler.class,
                PacketToggleItemCollect.class,
                0,
                Side.SERVER
        );

        // S → C：同步饰品 NBT
        NETWORK.registerMessage(
                PacketSyncBaubleNBT.Handler.class,
                PacketSyncBaubleNBT.class,
                1,
                Side.CLIENT
        );

        // S → C：暴击飘字
        NETWORK.registerMessage(
                PacketCritMessage.Handler.class,
                PacketCritMessage.class,
                2,
                Side.CLIENT
        );
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        if(event.getSide().isClient()){
            ModFontRenderers.init();
        }
        ModRecipes.registerRecipes();
    }

    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        System.out.println("[CelestialArtifacts] serverStarting 已被调用！");
        event.registerServerCommand(new com.zzt.eternal_abyss.command.CommandSetLuckModifier());
        event.registerServerCommand(new CommandDepthRingDebug());
    }


    // ClientProxy.java
    @SideOnly(Side.CLIENT)
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntitySwordQi.class, RenderSwordQi::new);

    }



}