package leafcraft.rtp.customEvents;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RandomSelectQueueEvent extends Event implements Cancellable {
    private final Location to;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;

    public RandomSelectQueueEvent(Location to) {

        this.to = to;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Location getTo() {
        return to;
    }
}
