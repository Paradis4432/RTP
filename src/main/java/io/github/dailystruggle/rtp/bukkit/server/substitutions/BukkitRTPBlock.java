package io.github.dailystruggle.rtp.bukkit.server.substitutions;

import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.configuration.ConfigParser;
import io.github.dailystruggle.rtp.common.configuration.enums.SafetyKeys;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPBlock;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPLocation;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPWorld;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public final class BukkitRTPBlock implements RTPBlock {
    private static Set<String> airBlocks = new ConcurrentSkipListSet<>();
    private static long lastUpdate = 0;

    static {
        airBlocks.add("AIR");
    }

    private final Block block;

    public BukkitRTPBlock(Block block) {
        this.block = block;
    }

    @Override
    public RTPLocation getLocation() {
        return new RTPLocation(RTP.serverAccessor.getRTPWorld(block.getWorld().getUID()), block.getX(), block.getY(), block.getZ());
    }

    @Override
    public boolean isAir() {
        long t = System.currentTimeMillis();
        long dt = t - lastUpdate;
        if (dt > 5000 || dt < 0) {
            ConfigParser<SafetyKeys> safety = (ConfigParser<SafetyKeys>) RTP.configs.getParser(SafetyKeys.class);
            Object o = safety.getConfigValue(SafetyKeys.airBlocks, new ArrayList<>());
            airBlocks = ((o instanceof Collection) ? (Collection<?>) o : new ArrayList<>())
                    .stream().map(o1 -> o1.toString().toUpperCase()).collect(Collectors.toSet());
            if (airBlocks.size() < 1) airBlocks.add("AIR");
            lastUpdate = t;
        }

        String material = getMaterial();

        return airBlocks.contains(material);
    }

    @Override
    public int x() {
        return block.getX();
    }

    @Override
    public int y() {
        return block.getY();
    }

    @Override
    public int z() {
        return block.getZ();
    }

    @Override
    public RTPWorld world() {
        return RTP.serverAccessor.getRTPWorld(block.getWorld().getUID());
    }

    @Override
    public int skyLight() {
        return block.getLightFromSky();
    }

    @Override
    public String getMaterial() {
        return block.getType().name().toUpperCase();
    }

    public Block block() {
        return block;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BukkitRTPBlock that = (BukkitRTPBlock) obj;
        return Objects.equals(this.block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block);
    }

    @Override
    public String toString() {
        return "BukkitRTPBlock[" +
                "block=" + block + ']';
    }

}
