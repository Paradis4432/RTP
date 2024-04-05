package io.github.dailystruggle.rtp.common.config.files;

import co.smashmc.smashlib.items.BackgroundItem;
import co.smashmc.smashlib.messages.Colors;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import io.github.dailystruggle.rtp.common.config.ConfigItem;
import io.github.dailystruggle.rtp.common.config.MoveButtons;

import java.util.Arrays;
import java.util.List;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class Page extends OkaeriConfig {
    /**
     * a container of items, no need for id
     * its the file loaded for each name found in the config
     * automatically created by the ConfigManager
     */

    @Comment("use 'redirect: <page_name>' to open a new gui")
    private List<ConfigItem> items = Arrays.asList(new ConfigItem());

    private BackgroundItem backgroundItem = new BackgroundItem();

    private String title = "Title";

    private int size = 54;

    private MoveButtons moveButtons = new MoveButtons();


    public List<ConfigItem> getItems() {
        return items;
    }

    public BackgroundItem getBackgroundItem() {
        return backgroundItem;
    }

    public String getTitle() {
        return Colors.colorAll(title);
    }

    public int getSize() {
        return size;
    }

    public MoveButtons getMoveButtons() {
        return moveButtons;
    }
}
