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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChat(ServerChatEvent event) {
        if (event.isCanceled()) return;

        try {
            ServerPlayer player = event.getPlayer();
            if (player == null) return;

            // 尝试多种方式获取 UUID（兼容不同映射版本）
            UUID playerUUID = null;
            try {
                // 方式1：直接 getUUID()（标准）
                playerUUID = player.getUUID();
            } catch (NoSuchMethodError e1) {
                try {
                    // 方式2：getUniqueID()（旧版本）
                    playerUUID = player.getUniqueID();
                } catch (NoSuchMethodError e2) {
                    try {
                        // 方式3：通过 GameProfile
                        playerUUID = player.getGameProfile().getId();
                    } catch (NoSuchMethodError e3) {
                        // 所有方式都失败，则通过玩家名查询（不推荐但作为最后的备选）
                        // 这里我们直接返回，避免聊天崩溃
                        return;
                    }
                }
            }

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
}
