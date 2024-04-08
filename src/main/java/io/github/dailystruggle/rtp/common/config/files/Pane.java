package io.github.dailystruggle.rtp.common.config.files;

import co.smashmc.smashlib.items.BackgroundItem;
import co.smashmc.smashlib.messages.Colors;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import io.github.dailystruggle.rtp.common.config.ConfigItem;

import java.util.Arrays;
import java.util.List;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class Pane extends OkaeriConfig {
    /**
     * a container of items, no need for id
     * its the file loaded for each name found in the config
     * automatically created by the ConfigManager
     */

    @Comment("use 'redirect: <pane_name>' to open a new gui")
    private List<ConfigItem> items = Arrays.asList(new ConfigItem());

    private BackgroundItem backgroundItem = new BackgroundItem();

    private String title = "Title";

    private int rows = 6;



    public List<ConfigItem> getItems() {
        return items;
    }

    public BackgroundItem getBackgroundItem() {
        return backgroundItem;
    }

    public String getTitle() {
        return Colors.colorAll(title);
    }

    public int getRows() {
        return rows;
    }

    public StaticPane buildStaticPane() {
        StaticPane staticPane = new StaticPane(9, getRows());
        items.forEach(item -> item.getGuiItems().forEach((slot, guiItem) -> staticPane.addItem(guiItem, Slot.fromIndex(slot))));
        return staticPane;
    }
}
