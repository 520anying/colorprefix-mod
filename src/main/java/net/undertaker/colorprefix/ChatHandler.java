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

            // 通过反射获取 UUID（兼容所有映射版本）
            UUID playerUUID = getUUIDReflectively(player);
            if (playerUUID == null) {
                // 如果反射失败，尝试通过玩家名获取（作为最后的备选）
                String playerName = player.getName().getString();
                User user = LuckPermsProvider.get().getUserManager().getUser(playerName);
                if (user == null) return;
                applyPrefix(event, player, user);
                return;
            }

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
        } catch (Exception e) {
            // 忽略
        }
    }

    private UUID getUUIDReflectively(ServerPlayer player) {
        // 尝试多种方式获取 UUID
        try {
            // 方式1：getUUID()
            return player.getUUID();
        } catch (NoSuchMethodError e1) {
            // 方式2：通过字段反射
            try {
                Field field = player.getClass().getSuperclass().getSuperclass().getDeclaredField("uuid");
                field.setAccessible(true);
                return (UUID) field.get(player);
            } catch (Exception e2) {
                try {
                    // 方式3：通过 GameProfile 字段
                    Object profile = player.getClass().getMethod("getGameProfile").invoke(player);
                    if (profile != null) {
                        Field idField = profile.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        return (UUID) idField.get(profile);
                    }
                } catch (Exception e3) {
                    // 所有方式都失败
                    return null;
                }
            }
        }
        return null;
    }
}
