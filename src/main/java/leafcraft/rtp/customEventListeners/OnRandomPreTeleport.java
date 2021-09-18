package leafcraft.rtp.customEventListeners;

import leafcraft.rtp.RTP;
import leafcraft.rtp.customEvents.RandomPreTeleportEvent;
import leafcraft.rtp.tools.Cache;
import leafcraft.rtp.tools.configuration.Configs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

public final class OnRandomPreTeleport implements Listener {
    private final RTP plugin;
    private final Configs configs;
    private final Cache cache;

    public OnRandomPreTeleport(RTP plugin, Configs configs, Cache cache) {
        this.plugin = plugin;
        this.configs = configs;
        this.cache = cache;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRandomPreTeleport(RandomPreTeleportEvent event) {
        Player player = event.getPlayer();
        if(configs.config.blindnessDuration>0)
            player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(configs.config.blindnessDuration,100),false);
    }
}