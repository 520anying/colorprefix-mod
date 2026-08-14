package net.undertaker.colorprefix;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.UUID;

public class ChatHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChat(ServerChatEvent event) {
        if (event.isCanceled()) return;

        try {
            ServerPlayer player = event.getPlayer();
            if (player == null) return;

            // 通过反射获取 UUID（兼容所有映射版本）
            UUID playerUUID = getPlayerUUID(player);
            if (playerUUID == null) return;

            User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
            if (user == null) return;

            String prefix = user.getCachedData().getMetaData().getPrefix();
            if (prefix == null || prefix.isEmpty()) return;

            String coloredPrefix = prefix.replace('&', '§');
            MutableComponent finalMsg = Component.literal(coloredPrefix + " ")
                    .append(player.getDisplayName().copy())
                    .append(Component.literal(": "))
                    .append(event.getMessage().copy());

            event.setMessage(finalMsg);
        } catch (Exception e) {
            // 静默失败，确保聊天不崩溃
        }
    }

    /**
     * 通过反射获取 ServerPlayer 的 UUID
     * 兼容所有映射版本（包括混淆后的方法名）
     */
    private UUID getPlayerUUID(ServerPlayer player) {
        try {
            // 尝试所有可能的方法名
            String[] methodNames = {"getUUID", "getUniqueID", "func_110124_au", "b_", "getId"};

            for (String name : methodNames) {
                try {
                    Method method = ServerPlayer.class.getMethod(name);
                    Object result = method.invoke(player);
                    if (result instanceof UUID) {
                        return (UUID) result;
                    }
                } catch (NoSuchMethodException ignored) {
                    // 继续尝试下一个方法名
                }
            }

            // 如果上述方法都失败，尝试通过 getGameProfile 获取
            try {
                Method getGameProfile = ServerPlayer.class.getMethod("getGameProfile");
                Object profile = getGameProfile.invoke(player);
                if (profile != null) {
                    Method getId = profile.getClass().getMethod("getId");
                    Object id = getId.invoke(profile);
                    if (id instanceof UUID) {
                        return (UUID) id;
                    }
                }
            } catch (Exception ignored) {
                // 忽略
            }

            // 所有方法都失败
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
