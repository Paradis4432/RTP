package io.github.dailystruggle.rtp.bukkit;

import io.github.dailystruggle.effectsapi.EffectFactory;
import io.github.dailystruggle.effectsapi.EffectsAPI;
import io.github.dailystruggle.rtp.bukkit.commands.RTPCmdBukkit;
import io.github.dailystruggle.rtp.bukkit.events.*;
import io.github.dailystruggle.rtp.bukkit.server.AsyncTeleportProcessing;
import io.github.dailystruggle.rtp.bukkit.server.BukkitServerAccessor;
import io.github.dailystruggle.rtp.bukkit.server.SyncTeleportProcessing;
import io.github.dailystruggle.rtp.bukkit.server.substitutions.BukkitRTPPlayer;
import io.github.dailystruggle.rtp.bukkit.spigotListeners.*;
import io.github.dailystruggle.rtp.bukkit.tools.SendMessage;
import io.github.dailystruggle.rtp.bukkit.tools.softdepends.VaultChecker;
import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.configuration.ConfigParser;
import io.github.dailystruggle.rtp.common.configuration.Configs;
import io.github.dailystruggle.rtp.common.configuration.MultiConfigParser;
import io.github.dailystruggle.rtp.common.configuration.enums.MessagesKeys;
import io.github.dailystruggle.rtp.common.configuration.enums.PerformanceKeys;
import io.github.dailystruggle.rtp.common.configuration.enums.RegionKeys;
import io.github.dailystruggle.rtp.common.configuration.enums.WorldKeys;
import io.github.dailystruggle.rtp.common.database.options.SQLiteDatabaseAccessor;
import io.github.dailystruggle.rtp.common.database.options.YamlFileDatabase;
import io.github.dailystruggle.rtp.common.factory.FactoryValue;
import io.github.dailystruggle.rtp.common.selection.region.Region;
import io.github.dailystruggle.rtp.common.tasks.*;
import io.github.dailystruggle.rtp.common.tasks.teleport.DoTeleport;
import io.github.dailystruggle.rtp.common.tasks.teleport.LoadChunks;
import io.github.dailystruggle.rtp.common.tasks.teleport.RTPTeleportCancel;
import io.github.dailystruggle.rtp.common.tasks.teleport.SetupTeleport;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Random Teleportation Spigot/Paper plugin, optimized for operators
 */
@SuppressWarnings("unused")
public final class RTPBukkitPlugin extends JavaPlugin {
    private static RTPBukkitPlugin instance = null;
    private static Metrics metrics;
    private static final EffectsAPI effectsAPI = null;

    public static RTPBukkitPlugin getInstance() {
        return instance;
    }

    public BukkitTask commandTimer = null;
    public BukkitTask commandProcessing = null;
    public BukkitTask asyncTimer = null;
    public BukkitTask syncTimer = null;

    @Override
    public void onLoad() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException();
        }

//        instance = this;
//        RTP.serverAccessor = new BukkitServerAccessor();
//
//        RTP rtp = new RTP();//constructor updates API instance
//
//        File databaseDirectory = RTP.configs.pluginDirectory;
//        databaseDirectory = new File(databaseDirectory.getAbsolutePath() + File.separator + "database");
//        databaseDirectory.mkdirs();
//        rtp.databaseAccessor = new SQLiteDatabaseAccessor(
//                "jdbc:sqlite:" + databaseDirectory.getAbsolutePath() + File.separator + "RTP.db");
//        rtp.databaseAccessor.startup();

//        File pluginDirectory = RTP.configs.pluginDirectory;
//        pluginDirectory = new File(pluginDirectory.getAbsolutePath() + File.separator + "database");
//        YamlFileDatabase database = new YamlFileDatabase(pluginDirectory);
//        rtp.databaseAccessor = database;
//        database.startup();
    }

    @Override
    public void onEnable() {
        metrics = new Metrics(this,12277);

        if(instance == null) {
            instance = this;
            RTP.serverAccessor = new BukkitServerAccessor();
            RTP rtp = new RTP();//constructor updates API instance

            File databaseDirectory = RTP.configs.pluginDirectory;
            databaseDirectory = new File(databaseDirectory.getAbsolutePath() + File.separator + "database");
            databaseDirectory.mkdirs();
            rtp.databaseAccessor = new SQLiteDatabaseAccessor(
                    "jdbc:sqlite:" + databaseDirectory.getAbsolutePath() + File.separator + "RTP.db");
            rtp.databaseAccessor.startup();
        }

        RTP.getInstance().startupTasks.execute(Long.MAX_VALUE);

        RTPCmdBukkit mainCommand = new RTPCmdBukkit(this);
        RTP.baseCommand = mainCommand;

        Objects.requireNonNull(getCommand("rtp")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("rtp")).setTabCompleter(mainCommand);
        Objects.requireNonNull(getCommand("wild")).setExecutor(mainCommand);
        Objects.requireNonNull(getCommand("wild")).setTabCompleter(mainCommand);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this,() -> {
            while (RTP.getInstance().startupTasks.size()>0) {
                RTP.getInstance().startupTasks.execute(Long.MAX_VALUE);
            }
        });

        Bukkit.getScheduler().scheduleSyncDelayedTask(this,RTP.serverAccessor::start);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this,this::setupBukkitEvents);
        if(RTP.serverAccessor.getServerIntVersion()>12) Bukkit.getScheduler().runTaskAsynchronously(this,this::setupEffects);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this,this::setupIntegrations);

        Bukkit.getScheduler().runTaskTimer(this, new TPS(),0,1);

        SendMessage.sendMessage(Bukkit.getConsoleSender(),"");

        while (RTP.getInstance().startupTasks.size()>0) {
            RTP.getInstance().startupTasks.execute(Long.MAX_VALUE);
        }

    }

    @Override
    public void onDisable() {
        if(commandTimer!=null) commandTimer.cancel();
        if(commandProcessing!=null) commandProcessing.cancel();

        AsyncTeleportProcessing.kill();
        SyncTeleportProcessing.kill();

        syncTimer.cancel();
        asyncTimer.cancel();



//        onChunkLoad.shutdown();
        metrics = null;

        RTP.stop();

        List<BukkitTask> pendingTasks = Bukkit.getScheduler().getPendingTasks().stream().filter(
                b -> b.getOwner().getName().equalsIgnoreCase("RTP") && !b.isSync() && !b.isCancelled()).collect(Collectors.toList());
        pendingTasks.forEach(BukkitTask::cancel);

        super.onDisable();
    }

    private void setupBukkitEvents() {
        Bukkit.getPluginManager().registerEvents(new OnEventTeleports(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerChangeWorld(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerDamage(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerMove(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerQuit(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerRespawn(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerTeleport(), this);
        if(RTP.serverAccessor.getServerIntVersion()<13) Bukkit.getPluginManager().registerEvents(new OnChunkUnload(), this);

        if(RTP.serverAccessor.getServerIntVersion()>12) EffectsAPI.init(this);
    }

    private void setupEffects() {
        Configs configs = RTP.configs;
        FactoryValue<PerformanceKeys> parser = configs.getParser(PerformanceKeys.class);

        SetupTeleport.preActions.add(task -> {
            PreSetupTeleportEvent event = new PreSetupTeleportEvent(task);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) task.setCancelled(true);
            if(task.player() instanceof BukkitRTPPlayer) {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                Player player = ((BukkitRTPPlayer) task.player()).player();
                RTP.getInstance().miscAsyncTasks.add(() -> {
                    EffectFactory.buildEffects("rtp.effect.presetup", player.getEffectivePermissions()).forEach(effect -> {
                        effect.setTarget(player);
                        effect.run();
                    });
                });
            }
        });

        SetupTeleport.postActions.add((task, aBoolean) -> {
            if(!aBoolean) return;
            PostSetupTeleportEvent event = new PostSetupTeleportEvent(task);
            Bukkit.getPluginManager().callEvent(event);
            if(task.player() instanceof BukkitRTPPlayer) {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                Player player = ((BukkitRTPPlayer) task.player()).player();
                RTP.getInstance().miscAsyncTasks.add(() -> {
                    EffectFactory.buildEffects("rtp.effect.postSetup", player.getEffectivePermissions()).forEach(effect -> {
                        effect.setTarget(player);
                        effect.run();
                    });
                });
            }
        });

        LoadChunks.preActions.add(task -> {
            PreLoadChunksEvent event = new PreLoadChunksEvent(task);
            Bukkit.getPluginManager().callEvent(event);

            if(task.player() instanceof BukkitRTPPlayer) {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                Player player = ((BukkitRTPPlayer) task.player()).player();
                RTP.getInstance().miscAsyncTasks.add(() -> {
                    EffectFactory.buildEffects("rtp.effect.presetup", player.getEffectivePermissions()).forEach(effect -> {
                        effect.setTarget(player);
                        effect.run();
                    });
                });
            }
        });

        LoadChunks.postActions.add(task -> {
            PostLoadChunksEvent event = new PostLoadChunksEvent(task);
            Bukkit.getPluginManager().callEvent(event);

            if(task.player() instanceof BukkitRTPPlayer) {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                Player player = ((BukkitRTPPlayer) task.player()).player();
                RTP.getInstance().miscAsyncTasks.add(() -> {
                    EffectFactory.buildEffects("rtp.effect.postload", player.getEffectivePermissions()).forEach(effect -> {
                        effect.setTarget(player);
                        effect.run();
                    });
                });
            }
        });

        DoTeleport.preActions.add(task -> {
            PreTeleportEvent event = new PreTeleportEvent(task);
            Bukkit.getPluginManager().callEvent(event);

            if(task.player() instanceof BukkitRTPPlayer) {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                Player player = ((BukkitRTPPlayer) task.player()).player();
                RTP.getInstance().miscAsyncTasks.add(() -> {
                    EffectFactory.buildEffects("rtp.effect.preteleport", player.getEffectivePermissions()).forEach(effect -> {
                        effect.setTarget(player);
                        effect.run();
                    });
                });
            }
        });

        DoTeleport.postActions.add(task -> {
            PostTeleportEvent event = new PostTeleportEvent(task);
            Bukkit.getPluginManager().callEvent(event);

            if(task.player() instanceof BukkitRTPPlayer) {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                Player player = ((BukkitRTPPlayer) task.player()).player();
                RTP.getInstance().miscAsyncTasks.add(() -> {
                    EffectFactory.buildEffects("rtp.effect.postteleport", player.getEffectivePermissions()).forEach(effect -> {
                        effect.setTarget(player);
                        effect.run();
                    });
                });
            }
        });

        DoTeleport.postActions.add(task -> {
            ConfigParser<MessagesKeys> lang = (ConfigParser<MessagesKeys>) RTP.configs.getParser(MessagesKeys.class);

            if(task.player() instanceof BukkitRTPPlayer) {
                Player player = ((BukkitRTPPlayer) task.player()).player();
                String title = lang.getConfigValue(MessagesKeys.title, "").toString();
                String subtitle = lang.getConfigValue(MessagesKeys.subtitle, "").toString();

                int fadeIn = lang.getNumber(MessagesKeys.fadeIn,0).intValue();
                int stay = lang.getNumber(MessagesKeys.stay,0).intValue();
                int fadeOut = lang.getNumber(MessagesKeys.fadeOut,0).intValue();

                SendMessage.title(player,title,subtitle,fadeIn,stay,fadeOut);

                String actionbar = lang.getConfigValue(MessagesKeys.actionbar, "").toString();
                SendMessage.actionbar(player,actionbar);
            }
        });

        RTPTeleportCancel.postActions.add(task -> {
            UUID uuid = task.getPlayerId();
            Player player = Bukkit.getPlayer(uuid);

            if(player == null) return;

            TeleportCancelEvent event = new TeleportCancelEvent(uuid);
            Bukkit.getPluginManager().callEvent(event);

            RTP.getInstance().miscAsyncTasks.add(() -> {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                EffectFactory.buildEffects("rtp.effect.cancel", player.getEffectivePermissions()).forEach(effect -> {
                    effect.setTarget(player);
                    effect.run();
                });
            });
        });

        Region.onPlayerQueuePush.add((region, uuid) -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) return;

            PlayerQueuePushEvent event = new PlayerQueuePushEvent(region,uuid);
            Bukkit.getPluginManager().callEvent(event);

            RTP.getInstance().miscAsyncTasks.add(() -> {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                EffectFactory.buildEffects("rtp.effect.queuepush", player.getEffectivePermissions()).forEach(effect -> {
                    effect.setTarget(player);
                    effect.run();
                });
            });
        });

        Region.onPlayerQueuePop.add((region, uuid) -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) return;

            PlayerQueuePopEvent event = new PlayerQueuePopEvent(region,uuid);
            Bukkit.getPluginManager().callEvent(event);

            RTP.getInstance().miscAsyncTasks.add(() -> {
                if(!Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) return;
                EffectFactory.buildEffects("rtp.effect.queuepop", player.getEffectivePermissions()).forEach(effect -> {
                    effect.setTarget(player);
                    effect.run();
                });
            });
        });

        if(Boolean.parseBoolean(parser.getData().getOrDefault(PerformanceKeys.effectParsing, false).toString())) {
            EffectFactory.addPermissions("rtp.effect.preSetup");
            EffectFactory.addPermissions("rtp.effect.postSetup");
            EffectFactory.addPermissions("rtp.effect.preLoad");
            EffectFactory.addPermissions("rtp.effect.postLoad");
            EffectFactory.addPermissions("rtp.effect.preTeleport");
            EffectFactory.addPermissions("rtp.effect.postTeleport");
            EffectFactory.addPermissions("rtp.effect.cancel");
            EffectFactory.addPermissions("rtp.effect.queuePush");
        }
    }

    public void setupIntegrations() {
        if(RTP.economy == null && Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            VaultChecker.setupEconomy();
            VaultChecker.setupPermissions();
            if(VaultChecker.getEconomy()!=null) RTP.economy = new VaultChecker();
            else RTP.economy = null;
        }
    }

    public static Region getRegion(Player player) {
        //get region from world name, check for overrides
        Set<String> worldsAttempted = new HashSet<>();
        Set<String> regionsAttempted = new HashSet<>();

        String worldName = player.getWorld().getName();
        MultiConfigParser<WorldKeys> worldParsers = (MultiConfigParser<WorldKeys>) RTP.configs.multiConfigParserMap.get(WorldKeys.class);
        ConfigParser<WorldKeys> worldParser = worldParsers.getParser(worldName);
        boolean requirePermission = Boolean.parseBoolean(worldParser.getConfigValue(WorldKeys.requirePermission,false).toString());

        while(requirePermission && !player.hasPermission("rtp.worlds."+worldName)) {
            if(worldsAttempted.contains(worldName)) throw new IllegalStateException("infinite override loop detected at world - " + worldName);
            worldsAttempted.add(worldName);

            worldName = String.valueOf(worldParser.getConfigValue(WorldKeys.override,"default"));
            worldParser = worldParsers.getParser(worldName);
            requirePermission = Boolean.parseBoolean(worldParser.getConfigValue(WorldKeys.requirePermission,false).toString());
        }

        String regionName = String.valueOf(worldParser.getConfigValue(WorldKeys.region, "default"));
        MultiConfigParser<RegionKeys> regionParsers = (MultiConfigParser<RegionKeys>) RTP.configs.multiConfigParserMap.get(RegionKeys.class);
        ConfigParser<RegionKeys> regionParser = regionParsers.getParser(regionName);
        requirePermission = Boolean.parseBoolean(regionParser.getConfigValue(RegionKeys.requirePermission,false).toString());

        while(requirePermission && !player.hasPermission("rtp.regions."+regionName)) {
            if(regionsAttempted.contains(regionName)) throw new IllegalStateException("infinite override loop detected at region - " + regionName);
            regionsAttempted.add(regionName);

            regionName = String.valueOf(regionParser.getConfigValue(RegionKeys.override,"default"));
            regionParser = regionParsers.getParser(regionName);
            requirePermission = Boolean.parseBoolean(regionParser.getConfigValue(RegionKeys.requirePermission,false).toString());
        }
        return RTP.getInstance().selectionAPI.permRegionLookup.get(regionName);
    }
}
