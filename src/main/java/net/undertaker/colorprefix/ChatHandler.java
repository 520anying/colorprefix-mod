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

    @SubscribeEvent(priority = EventPriority.HIGHEST)  // 最高优先级，确保能处理
    public void onServerChat(ServerChatEvent event) {
        // 打印日志，确认事件被触发
        System.out.println("🔔 [colorprefix] 聊天事件被触发了！");
        
        if (event.isCanceled()) {
            System.out.println("⛔ [colorprefix] 事件已被取消，跳过处理");
            return;
        }

        try {
            ServerPlayer player = event.getPlayer();
            if (player == null) {
                System.out.println("❌ [colorprefix] player 为 null");
                return;
            }

            System.out.println("✅ [colorprefix] 玩家: " + player.getName().getString());

            // 通过反射获取 UUID
            UUID playerUUID = getPlayerUUID(player);
            if (playerUUID == null) {
                System.out.println("❌ [colorprefix] 无法获取 UUID");
                return;
            }
            System.out.println("✅ [colorprefix] UUID: " + playerUUID);

            User user = LuckPermsProvider.get().getUserManager().getUser(playerUUID);
            if (user == null) {
                System.out.println("❌ [colorprefix] 未找到 LuckPerms 用户");
                return;
            }

            String prefix = user.getCachedData().getMetaData().getPrefix();
            System.out.println("✅ [colorprefix] 原始前缀: " + prefix);
            if (prefix == null || prefix.isEmpty()) {
                System.out.println("ℹ️ [colorprefix] 前缀为空，不修改消息");
                return;
            }

            String coloredPrefix = prefix.replace('&', '§');
            MutableComponent finalMsg = Component.literal(coloredPrefix + " ")
                    .append(player.getDisplayName().copy())
                    .append(Component.literal(": "))
                    .append(event.getMessage().copy());

            event.setMessage(finalMsg);
            System.out.println("✅ [colorprefix] 消息已修改！");
        } catch (Exception e) {
            System.err.println("❌ [colorprefix] 处理聊天事件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UUID getPlayerUUID(ServerPlayer player) {
        // 尝试多种方法获取 UUID
        try {
            // 方法1：getUUID()
            return player.getUUID();
        } catch (NoSuchMethodError e1) {
            try {
                // 方法2：getGameProfile().getId()
                Object profile = player.getClass().getMethod("getGameProfile").invoke(player);
                if (profile != null) {
                    return (UUID) profile.getClass().getMethod("getId").invoke(profile);
                }
            } catch (Exception e2) {
                try {
                    // 方法3：反射获取 field
                    java.lang.reflect.Field field = player.getClass().getSuperclass().getDeclaredField("uuid");
                    field.setAccessible(true);
                    return (UUID) field.get(player);
                } catch (Exception e3) {
                    // 所有方法都失败
                    return null;
                }
            }
        }
        return null;
    }
}
