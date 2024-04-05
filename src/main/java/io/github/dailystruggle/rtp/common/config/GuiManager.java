package io.github.dailystruggle.rtp.common.config;

import io.github.dailystruggle.rtp.bukkit.RTPBukkitPlugin;
import io.github.dailystruggle.rtp.common.config.files.Pane;
import org.bukkit.entity.Player;

public class GuiManager {
    public static void openFromID(Player player, String id) {
        RTPBukkitPlugin.configManager.getFromID(id).ifPresentOrElse(pane -> {
            openFromPane(player, pane);
        }, () -> {
            player.sendMessage("Pane not found: " + id);
            throw new IllegalArgumentException("Pane not found: " + id);
        });
    }

    public static void openFromPane(Player player, Pane pane) {
        // TODO implement
        // TODO build gui from Pane and show to player
        // TODO cache gui build based on Pane instance
        // TODO add pages support

    }
}
