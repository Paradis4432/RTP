package leafcraft.rtp;

import io.papermc.lib.PaperLib;
import leafcraft.rtp.commands.*;
import leafcraft.rtp.customEventListeners.OnRandomTeleport;
import leafcraft.rtp.customEventListeners.OnTeleportCancel;
import leafcraft.rtp.spigotEventListeners.*;
import leafcraft.rtp.tools.Cache;
import leafcraft.rtp.tools.Configuration.Configs;
import leafcraft.rtp.tools.TPS;
import leafcraft.rtp.tools.selection.RandomSelectParams;
import leafcraft.rtp.tools.selection.TeleportRegion;
import leafcraft.rtp.tools.softdepends.PAPI_expansion;
import leafcraft.rtp.tools.softdepends.VaultChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RTP extends JavaPlugin {
    private Configs configs;
    private Cache cache;

//    private OnChunkLoad onChunkLoad;

    private Metrics metrics;

    public RTP()
    {
        super();
    }

    protected RTP(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file)
    {
        super(loader, description, dataFolder, file);
    }


    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        this.metrics = new Metrics(this, 12277);
        this.configs = new Configs(this);
        this.cache = new Cache(this,configs);

        RTPCmd rtpCmd = new RTPCmd(this,configs,cache);
        Help help = new Help(configs);
        Reload reload = new Reload(configs, cache);
        SetRegion setRegion = new SetRegion(this,configs, cache);
        SetWorld setWorld = new SetWorld(this,configs, cache);
        Fill fill = new Fill(this,configs, cache);

        try {
            Objects.requireNonNull(getCommand("wild")).setExecutor(rtpCmd);
            Objects.requireNonNull(getCommand("rtp")).setExecutor(rtpCmd);
        }
        catch (NullPointerException ignored) { }

        try {
            Objects.requireNonNull(getCommand("rtp")).setTabCompleter(new TabComplete(this.configs));
            Objects.requireNonNull(getCommand("wild")).setTabCompleter(new TabComplete(this.configs));
        }
        catch (NullPointerException ignored) { }

        rtpCmd.addCommandHandle("help", "rtp.help", help);
        rtpCmd.addCommandHandle("reload", "rtp.reload", reload);
        rtpCmd.addCommandHandle("setRegion", "rtp.setRegion", setRegion);
        rtpCmd.addCommandHandle("setWorld", "rtp.setWorld", setWorld);
        rtpCmd.addCommandHandle("fill", "rtp.fill", fill);

//        try {
//            Objects.requireNonNull(getCommand("rtp help")).setExecutor(help);
//            Objects.requireNonNull(getCommand("rtp reload")).setExecutor(reload);
//            Objects.requireNonNull(getCommand("rtp setRegion")).setExecutor(setRegion);
//            Objects.requireNonNull(getCommand("rtp setWorld")).setExecutor(setWorld);
//            Objects.requireNonNull(getCommand("rtp fill")).setExecutor(fill);
//        } catch (NullPointerException ignored) { }

        getServer().getPluginManager().registerEvents(new OnPlayerMove(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnPlayerTeleport(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnPlayerDeath(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnPlayerRespawn(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnPlayerJoin(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnPlayerChangeWorld(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnPlayerQuit(cache),this);
        getServer().getPluginManager().registerEvents(new OnRandomTeleport(this,configs,cache),this);
        getServer().getPluginManager().registerEvents(new OnTeleportCancel(this,configs,cache),this);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TPS(), 100L, 1L);

        VaultChecker.setupChat();
        VaultChecker.setupEconomy();
        VaultChecker.setupPermissions();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPI_expansion(this,configs,cache).register();
        }
    }

    @Override
    public void onDisable() {
//        onChunkLoad.shutdown();
        if(this.cache != null) {
            this.cache.shutdown();
        }

        super.onDisable();
    }

    /*
     * getRegion
     *
     * returns TeleportRegion by name
     * returns null if region does not exist or if its world is bad
     */
    public TeleportRegion getRegion(String regionName) {
        Map<String,String> params = new HashMap<>();
        params.put("region",regionName);

        String worldName = (String) configs.regions.getRegionSetting(regionName,"world","");
        if (worldName == null || worldName == "" || !configs.worlds.checkWorldExists(worldName)) {
            return null;
        }

        RandomSelectParams randomSelectParams = new RandomSelectParams(Bukkit.getWorld(worldName),params,configs);
        if(!cache.permRegions.containsKey(randomSelectParams)) return null;
        return cache.permRegions.get(randomSelectParams);
    }
}
