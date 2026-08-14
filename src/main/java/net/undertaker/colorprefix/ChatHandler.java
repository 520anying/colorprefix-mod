package net.undertaker.colorprefix;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.UUID;

public class ChatHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChat(ServerChatEvent event) {
        if (event.isCanceled()) return;

        try {
            ServerPlayer player = event.getPlayer();
            if (player == null) return;

            // 方法1：尝试直接用 player.getUUID()
            UUID playerUUID = null;
            try {
                playerUUID = player.getUUID();
            } catch (NoSuchMethodError e) {
                // 方法2：通过反射获取 uuid 字段
                try {
                    Field field = player.getClass().getSuperclass().getSuperclass().getDeclaredField("uuid");
                    field.setAccessible(true);
                    playerUUID = (UUID) field.get(player);
                } catch (Exception ex) {
                    // 方法3：通过玩家名查询（最后的备选）
                    String playerName = player.getName().getString();
                    User user = LuckPermsProvider.get().getUserManager().getUser(playerName);
                    if (user != null) {
                        applyPrefix(event, player, user);
                    }
                    return;
                }
            }

            if (playerUUID == null) return;
            User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
            if (user == null) return;

            applyPrefix(event, player, user);
        } catch (Exception e) {
            // 静默失败，确保聊天不崩溃
        }
    }

    private void applyPrefix(ServerChatEvent event, ServerPlayer player, User user) {
        try {
            String prefix = user.getCachedData().getMetaData().getPrefix();
            if (prefix == null || prefix.isEmpty()) return;

            String coloredPrefix = prefix.replace('&', '§');
            MutableComponent finalMsg = Component.literal(coloredPrefix + " ")
                    .append(player.getDisplayName().copy())
                    .append(Component.literal(": "))
                    .append(event.getMessage().copy());

            event.setMessage(finalMsg);
        } catch (Exception ignored) {}
    }
}
