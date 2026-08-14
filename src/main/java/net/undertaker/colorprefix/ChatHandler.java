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

            UUID playerUUID = getUUIDReflectively(player);
            if (playerUUID == null) {
                // 备选：通过玩家名查询
                String playerName = player.getName().getString();
                User user = LuckPermsProvider.get().getUserManager().getUser(playerName);
                if (user != null) applyPrefix(event, player, user);
                return;
            }

            User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
            if (user != null) applyPrefix(event, player, user);
        } catch (Exception e) {
            // 静默失败
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

    private UUID getUUIDReflectively(ServerPlayer player) {
        try {
            return player.getUUID();
        } catch (NoSuchMethodError e1) {
            try {
                Field field = player.getClass().getSuperclass().getSuperclass().getDeclaredField("uuid");
                field.setAccessible(true);
                return (UUID) field.get(player);
            } catch (Exception e2) {
                try {
                    Object profile = player.getClass().getMethod("getGameProfile").invoke(player);
                    if (profile != null) {
                        Field idField = profile.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        return (UUID) idField.get(profile);
                    }
                } catch (Exception ignored) {}
                return null;
            }
        }
    }
}
