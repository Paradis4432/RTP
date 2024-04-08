package io.github.dailystruggle.rtp.common.config;

import co.smashmc.smashlib.messages.Colors;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
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

    public static void openMainMenu(Player player) {
        openFromID(player, "mainMenu");
    }

    public static void openFromPane(Player player, Pane pane) {
        // TODO cache gui build based on Pane instance

        ChestGui gui = new ChestGui(pane.getRows(), Colors.colorAll(pane.getTitle()));
        gui.setOnGlobalClick(e -> e.setCancelled(true));

        StaticPane mainPane = pane.buildStaticPane();
        pane.getMoveButtons().addToStaticPane(mainPane);
        gui.addPane(mainPane);

        StaticPane background = new StaticPane(9, pane.getRows());
        pane.getBackgroundItem().addToStaticPane(background);
        gui.addPane(background);

        gui.show(player);
    }
}
