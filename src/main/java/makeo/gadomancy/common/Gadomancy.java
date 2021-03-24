package makeo.gadomancy.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import makeo.gadomancy.api.GadomancyApi;
import makeo.gadomancy.common.api.DefaultApiHandler;
import makeo.gadomancy.common.data.config.ModConfig;
import makeo.gadomancy.common.data.config.ModData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.common.Thaumcraft;

/**
 * This class is part of the Gadomancy Mod
 * Gadomancy is Open Source and distributed under the
 * GNU LESSER GENERAL PUBLIC LICENSE
 * for more read the LICENSE file
 *
 * Created by makeo @ 29.11.2014 14:07
 */
@Mod(modid = Gadomancy.MODID, name = Gadomancy.NAME, version = Gadomancy.VERSION, dependencies="required-after:Thaumcraft@[4.1.1.11,);after:Waila;after:Automagy")
public class Gadomancy {
    public static final String MODID = "gadomancy";
    public static final String NAME = "Gadomancy";

    public static final String VERSION = "1.0.7.4";

    private static final String PROXY_CLIENT = "makeo.gadomancy.client.ClientProxy";
    private static final String PROXY_SERVER = "makeo.gadomancy.common.CommonProxy";

    @Mod.Instance(Gadomancy.MODID)
    public static Gadomancy instance;

    @SidedProxy(clientSide = Gadomancy.PROXY_CLIENT, serverSide = Gadomancy.PROXY_SERVER)
    public static CommonProxy proxy;

    public static Logger log = LogManager.getLogger("Gadomancy");
    private static ModData modData;

    public static ModData getModData() {
        return Gadomancy.modData;
    }

    public static void loadModData() {
        Gadomancy.modData = new ModData("data");
        Gadomancy.modData.load();
    }

    public static void unloadModData() {
        if(Gadomancy.modData != null) {
            Gadomancy.modData.save();
            Gadomancy.modData = null;
        }
    }

    @Mod.EventHandler
    public void onConstruct(FMLConstructionEvent event) {
        GadomancyApi.setInstance(new DefaultApiHandler());
        Gadomancy.proxy.onConstruct();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = Gadomancy.VERSION;
        ModConfig.init(event.getSuggestedConfigurationFile());
        Gadomancy.proxy.preInitalize();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Gadomancy.proxy.initalize();

        FMLInterModComms.sendMessage(Thaumcraft.MODID, "dimensionBlacklist", ModConfig.dimOuterId + ":0");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Gadomancy.proxy.postInitalize();
    }
}
