package io.github.dailystruggle.rtp.common.config.files;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

import java.util.Arrays;
import java.util.List;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class Config extends OkaeriConfig {
    @Comment("The list of guis that are available, case sensitive")
    @Comment("default to open is always 'mainMenu' ")
    private List<String> guis = Arrays.asList("mainMenu");

    public List<String> getGuis() {
        return guis;
    }
}
