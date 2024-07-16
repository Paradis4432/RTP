package io.github.dailystruggle.rtp.bukkit.commands;

import io.github.dailystruggle.rtp.bukkit.RTPBukkitPlugin;
import io.github.dailystruggle.rtp.common.config.GuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BRTPCmdBukkit implements CommandExecutor {

    /**
     * TODO missing reload and maybe some admin commands
     * TODO missing reload, note, clear GuiManager cache
     */

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        //if (player.hasPermission("")) // TODO perm set

        if (args.length > 0) {
            // TODO missing reload and maybe some admin commands
            if (args[0].equalsIgnoreCase("reload")) {
                commandSender.sendMessage("reloading");
                RTPBukkitPlugin.configManager.reload();
                return true;
            }
            commandSender.sendMessage("unknown command");
            return true;
        }

        if (!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;

        GuiManager.openMainMenu(player);

        return true;
    }
}
