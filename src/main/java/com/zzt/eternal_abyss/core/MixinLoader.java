package com.zzt.eternal_abyss.core;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("CelestialArtifactsMixinCore")
@IFMLLoadingPlugin.SortingIndex(1000)
@IFMLLoadingPlugin.TransformerExclusions({"com.zzt.eternal_abyss.core"})
public class MixinLoader implements IFMLLoadingPlugin {

    static {
        try {
            // EnhancedVisuals / client-only
            FermiumRegistryAPI.enqueueMixin(true, "mixins.eternal_abyss.json");

            System.out.println("[CelestialArtifacts] Mixins queued.");
        } catch (Throwable t) {
            System.err.println("[CelestialArtifacts] Failed to queue mixins");
            t.printStackTrace();
        }
    }

    @Override public String[] getASMTransformerClass() {
        return new String[]{ EmptyTransformer.class.getName() };
    }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(java.util.Map<String, Object> data) {}
    @Override public String getAccessTransformerClass() { return null; }
}
