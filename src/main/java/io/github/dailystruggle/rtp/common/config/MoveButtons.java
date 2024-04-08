package io.github.dailystruggle.rtp.common.config;

import co.smashmc.smashlib.items.ItemBuilder;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.github.stefvanschie.inventoryframework.pane.util.Slot.fromIndex;

public class MoveButtons implements ObjectSerializer<MoveButtons> {
    private ItemStack next = new ItemBuilder(Material.ARROW).name("Next");
    private Integer nextSlot = 20;
    private ItemStack back = new ItemBuilder(Material.ARROW).name("Previous");
    private Integer backSlot = 18;

    public MoveButtons(ItemStack next, Integer nextSlot, ItemStack back, Integer backSlot) {
        this.next = next == null ? this.next : next;
        this.nextSlot = nextSlot == null ? this.nextSlot : nextSlot;
        this.back = back == null ? this.back : back;
        this.backSlot = backSlot == null ? this.backSlot : backSlot;
    }

    public MoveButtons() {
    }

    public ItemStack getNext() {
        return next;
    }

    public @NotNull Slot getNextSlot() {
        return fromIndex(nextSlot);
    }

    public ItemStack getBack() {
        return back;
    }

    public @NotNull Slot getBackSlot() {
        return fromIndex(backSlot);
    }

    public void addToStaticPane(StaticPane staticPane) {
        // TODO implement
    }

    @Override
    public boolean supports(@NotNull Class<? super MoveButtons> type) {
        return MoveButtons.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull MoveButtons object, @NotNull SerializationData data, @NotNull GenericsDeclaration generics) {
        data.add("next", object.next);
        data.add("nextSlot", object.nextSlot);
        data.add("back", object.back);
        data.add("backSlot", object.backSlot);
    }

    @Override
    public MoveButtons deserialize(@NotNull DeserializationData data, @NotNull GenericsDeclaration generics) {
        return new MoveButtons(
                data.get("next", ItemStack.class),
                data.get("nextSlot", Integer.class),
                data.get("back", ItemStack.class),
                data.get("backSlot", Integer.class)
        );
    }
}
