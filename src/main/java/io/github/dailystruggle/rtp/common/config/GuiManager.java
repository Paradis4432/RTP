package io.github.dailystruggle.rtp.common.config;

import io.github.dailystruggle.rtp.bukkit.RTPBukkitPlugin;
import io.github.dailystruggle.rtp.common.config.files.Page;
import org.bukkit.entity.Player;

public class GuiManager {
    public static void openFromID(Player player, String id) {
        RTPBukkitPlugin.configManager.getFromID(id).ifPresentOrElse(page -> {
            openFromPage(player, page);
        }, () -> {
            player.sendMessage("Page not found: " + id);
            throw new IllegalArgumentException("Page not found: " + id);
        });
    }

    public static void openFromPage(Player player, Page page) {
        // TODO implement
        // TODO build gui from page and show to playe
        // TODO cache gui build based on page instance

    }
}
