package com.hfstudio.doabarrelroll.coremod;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.hfstudio.doabarrelroll.config.ModConfig;
import com.hfstudio.doabarrelroll.mixins.Mixins;
import com.hfstudio.elytrahud.ElytraHudConfig;
import com.hfstudio.flightassistant.FAConfig;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class DoABarrelRollCoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    static {
        try {
            ModConfig.registerConfig();
            ElytraHudConfig.registerConfig();
            FAConfig.registerConfig();
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    public DoABarrelRollCoreMod() {}

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.doabarrelroll.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return IMixins.getEarlyMixins(Mixins.class, loadedCoreMods);
    }
}
