package net.undertaker.colorprefix;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(TimeOfSacrifice.MODID)
public class TimeOfSacrifice {
    public static final String MODID = "colorprefix";

    public TimeOfSacrifice() {
        MinecraftForge.EVENT_BUS.register(new ChatHandler());
        System.out.println("✅ 彩色称号模组已加载！");
    }
}
