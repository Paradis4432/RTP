package io.github.dailystruggle.rtp.common.config;

import co.smashmc.smashlib.actions.PlayerActionHolder;
import co.smashmc.smashlib.items.ItemBuilder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import io.github.dailystruggle.rtp.bukkit.RTPBukkitPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigItem implements ObjectSerializer<ConfigItem> {
    private Integer page = 0;
    private List<Integer> slots = Arrays.asList(0);
    private ItemStack itemStack = new ItemBuilder(Material.PAPER).nbt("CustomModelData", "1000");
    private PlayerActionHolder actions = new PlayerActionHolder();

    public ConfigItem() {
    }

    public ConfigItem(List<Integer> slots, ItemStack itemStack, PlayerActionHolder actions, Integer page) {
        this.slots = slots == null ? this.slots : slots;
        this.itemStack = itemStack == null ? this.itemStack : itemStack;
        this.actions = actions == null ? this.actions : actions;
        this.page = page == null ? this.page : page;
        this.actions.registerPlayerExecutor("redirect", ((player, payload) ->
                RTPBukkitPlugin.configManager.getFromID(payload).ifPresentOrElse((pane) ->
                        GuiManager.openFromPane(player, pane), () -> {
                    player.sendMessage("page not found");
                    throw new IllegalArgumentException("pane not found");
                })));
    }

    public Map<Integer, GuiItem> getGuiItems() {
        Map<Integer, GuiItem> items = new HashMap<>();
        slots.forEach((s) -> {
            items.put(s, new GuiItem(itemStack));
        });
        return items;
    }

    public Map<Integer, ItemStack> getItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        slots.forEach((s) -> items.put(s, itemStack));
        return items;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean supports(@NotNull Class<? super ConfigItem> type) {
        return ConfigItem.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull ConfigItem object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.add("page", object.page, Integer.class);
        data.addCollection("slots", object.slots, Integer.class);
        data.add("itemStack", object.itemStack, ItemStack.class);
        data.add("actionsForItems", object.actions, PlayerActionHolder.class);
    }

    @Override
    public ConfigItem deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        return new ConfigItem(
                data.getAsList("slots", Integer.class),
                data.get("itemStack", ItemStack.class),
                data.get("actionsForItems", PlayerActionHolder.class),
                data.get("page", Integer.class)
        );
    }
}
