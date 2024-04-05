package io.github.dailystruggle.rtp.common.config;

import co.smashmc.smashlib.okaeri.AbstractConfigManager;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;
import io.github.dailystruggle.rtp.common.config.files.Config;
import io.github.dailystruggle.rtp.common.config.files.Page;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigManager extends AbstractConfigManager {
    private static Config config;
    private static Map<String, Page> pages = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        super(plugin, new CustomSerdesPack());
    }

    @Override
    public void load() {
        config = loadFile(Config.class, "config");
        config.getGuis().forEach(gui -> {
            System.out.println("loading gui: " + gui);
            pages.put(gui, loadFile(Page.class, gui));
        });
    }

    @Override
    public void reload() {
        super.reload(config);
        pages.forEach((id, page) -> {
            System.out.println("reloading page: " + id);
            page.load(true);
        });
    }

    public Optional<Page> getFromID(String id) {
        if (!pages.containsKey(id)) return Optional.empty();
        return Optional.of(pages.get(id));
    }

    private static class CustomSerdesPack implements OkaeriSerdesPack {

        @Override
        public void register(@NotNull SerdesRegistry registry) {
            registry.register(new ConfigItem());
            registry.register(new MoveButtons());
            registry.register(new MoveButtons());
        }
    }
}


