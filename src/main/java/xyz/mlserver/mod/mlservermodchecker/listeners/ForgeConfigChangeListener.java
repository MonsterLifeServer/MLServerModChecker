package xyz.mlserver.mod.mlservermodchecker.listeners;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.mlserver.mod.mlservermodchecker.MLServerModChecker;
import xyz.mlserver.mod.mlservermodchecker.MLServerModCheckerConfigCore;

public class ForgeConfigChangeListener {
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        // コンフィグが変更された時に呼ばれる。
        if (event.getModID().equals(MLServerModChecker.MODID)) {
            MLServerModCheckerConfigCore.syncConfig();
        }
    }
}
