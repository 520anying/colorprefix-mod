package net.undertaker.colorprefix;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("colorprefix")
public class TimeOfSacrifice {
    public static final String MODID = "colorprefix";

    public TimeOfSacrifice() {
        // 不在这里注册任何事件
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftForge.EVENT_BUS.register(new ChatHandler());
    }
}
