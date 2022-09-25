package io.github.dailystruggle.rtp.common.commands.reload;

import io.github.dailystruggle.commandsapi.common.CommandsAPI;
import io.github.dailystruggle.commandsapi.common.CommandsAPICommand;
import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.commands.BaseRTPCmdImpl;
import io.github.dailystruggle.rtp.common.configuration.ConfigParser;
import io.github.dailystruggle.rtp.common.configuration.Configs;
import io.github.dailystruggle.rtp.common.configuration.MultiConfigParser;
import io.github.dailystruggle.rtp.common.configuration.enums.LangKeys;
import io.github.dailystruggle.rtp.common.tasks.RTPRunnable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReloadCmd extends BaseRTPCmdImpl {

    public ReloadCmd(@Nullable CommandsAPICommand parent) {
        super(parent);

        RTP.getInstance().miscAsyncTasks.add(new RTPRunnable(this::addCommands,20));
    }

    public void addCommands() {
        final Configs configs = RTP.getInstance().configs;
        for (ConfigParser<?> value : configs.configParserMap.values()) {
            String name = value.name.replace(".yml","");
            if(getCommandLookup().containsKey(name)) continue;
            addSubCommand(new SubReloadCmd<>(this,value.name,"rtp.reload","reload only " + value.name, value.myClass));
        }

        for (Map.Entry<Class<?>, MultiConfigParser<?>> e : configs.multiConfigParserMap.entrySet()) {
            MultiConfigParser<? extends Enum<?>> value = e.getValue();
            if(getCommandLookup().containsKey(value.name)) continue;
            addSubCommand(new SubReloadCmd<>(this,value.name,"rtp.reload","reload only " + value.name, value.myClass));
        }
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String permission() {
        return "rtp.reload";
    }

    @Override
    public String description() {
        return "reload config files";
    }

    @Override
    public boolean onCommand(UUID senderId, Map<String, List<String>> parameterValues, CommandsAPICommand nextCommand) {
        addCommands();

        RTP.stop();
        RTP.serverAccessor.stop();
        RTP.serverAccessor.start();

        if(nextCommand!=null) return true;

        ConfigParser<LangKeys> lang = (ConfigParser<LangKeys>) RTP.getInstance().configs.getParser(LangKeys.class);
        if(lang != null) {
            String msg = String.valueOf(lang.getConfigValue(LangKeys.reloading,""));
            if(msg!=null) msg = StringUtils.replace(msg,"[filename]", "configs");
            RTP.serverAccessor.sendMessage(CommandsAPI.serverId, senderId,msg);
        }

        boolean b = RTP.getInstance().configs.reload();
        if(!b) throw new IllegalStateException("reload failed");

        RTP.serverAccessor.start();

        return true;
    }
}
