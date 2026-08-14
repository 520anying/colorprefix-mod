package net.undertaker.colorprefix;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class ChatHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerChat(ServerChatEvent event) {
        if (event.isCanceled()) return;

        try {
            ServerPlayer player = event.getPlayer();
            if (player == null) return;

            // 获取 UUID
            UUID playerUUID = getPlayerUUID(player);
            if (playerUUID == null) return;

            User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
            if (user == null) return;

            String prefix = user.getCachedData().getMetaData().getPrefix();
            if (prefix == null || prefix.isEmpty()) return;

            String coloredPrefix = prefix.replace('&', '§');
            
            // 使用 getDisplayName() 而不是 getName()
            MutableComponent finalMsg = Component.literal(coloredPrefix + " ")
                    .append(player.getDisplayName().copy())
                    .append(Component.literal(": "))
                    .append(event.getMessage().copy());

            event.setMessage(finalMsg);
        } catch (Exception e) {
            System.err.println("❌ [colorprefix] 处理聊天事件时出错: " + e.getMessage());
        }
    }

    private UUID getPlayerUUID(ServerPlayer player) {
        try {
            return player.getUUID();
        } catch (NoSuchMethodError e1) {
            try {
                Object profile = player.getClass().getMethod("getGameProfile").invoke(player);
                if (profile != null) {
                    return (UUID) profile.getClass().getMethod("getId").invoke(profile);
                }
            } catch (Exception e2) {
                try {
                    java.lang.reflect.Field field = player.getClass().getSuperclass().getDeclaredField("uuid");
                    field.setAccessible(true);
                    return (UUID) field.get(player);
                } catch (Exception e3) {
                    return null;
                }
            }
        }
        return null;
    }
}
