package io.github.dailystruggle.rtp.common;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import io.github.dailystruggle.rtp.bukkit.commands.RTPCmdBukkit;
import io.github.dailystruggle.rtp.common.commands.BaseRTPCmd;
import io.github.dailystruggle.rtp.common.commands.RTPCmd;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public abstract class MessageListener extends RTPCmdBukkit implements PluginMessageListener {

    public MessageListener(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, @NotNull byte[] bytes) {
        if (!s.equals("smash:switchchannel"))return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String command = in.readUTF();
        if (!command.equals("RtpServer")) return;
        String playerName = in.readUTF();
        String args = in.readUTF();
        String[] cmdArgs = deserialize(args);

        this.onCommand(player, null, null, cmdArgs);
    }

    private String[] deserialize(String args){
        return args.split(",");
    }
}
