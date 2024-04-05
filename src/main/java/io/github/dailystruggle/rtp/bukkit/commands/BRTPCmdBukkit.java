package io.github.dailystruggle.rtp.bukkit.commands;

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
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;


        return false;
    }
}
