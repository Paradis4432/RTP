package leafcraft.rtp.common.selection.region.selectors.memory.shapes;

import leafcraft.rtp.common.RTP;
import leafcraft.rtp.common.configuration.ConfigParser;
import leafcraft.rtp.common.configuration.enums.PerformanceKeys;
import leafcraft.rtp.common.selection.region.selectors.memory.shapes.enums.GenericMemoryShapeParams;
import leafcraft.rtp.common.selection.region.selectors.shapes.Shape;
import leafcraft.rtp.common.substitutions.RTPChunk;
import leafcraft.rtp.common.substitutions.RTPWorld;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @param <E> enum for configuration values
 */
public abstract class MemoryShape<E extends Enum<E>> extends Shape<E> {
    /**
     * @param name - unique name of shape
     */
    public MemoryShape(Class<E> eClass, String name, EnumMap<E,Object> data) throws IllegalArgumentException {
        super(eClass,name,data);
    }

    public final ConcurrentSkipListMap<Long,Long> badLocations = new ConcurrentSkipListMap<>();
    public final AtomicLong badLocationSum = new AtomicLong(0L);
    public final ConcurrentHashMap<Biome, ConcurrentSkipListMap<Long,Long>> biomeLocations = new ConcurrentHashMap<>();

    public abstract double getRange();
    public abstract double xzToLocation(long x, long z);
    public abstract int[] locationToXZ(long location);

    public boolean isKnownBad(int x, int z) {
        return isKnownBad((long)xzToLocation(x,z));
    }

    public boolean isKnownBad(long location) {
        Map.Entry<Long,Long> lower = badLocations.floorEntry(location);
        if(lower!=null) {
            return (location < (lower.getKey() + lower.getValue()));
        }
        return false;
    }
}
