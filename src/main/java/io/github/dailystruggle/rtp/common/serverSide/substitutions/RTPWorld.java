package io.github.dailystruggle.rtp.common.serverSide.substitutions;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RTPWorld {
    String name();

    UUID id();

    CompletableFuture<RTPChunk> getChunkAt( int chunkX, int chunkZ );

    void keepChunkAt( int chunkX, int chunkZ );

    void forgetChunkAt( int chunkX, int chunkZ );

    void forgetChunks();

    String getBiome( int x, int y, int z );

    void platform( RTPLocation location );

    boolean isInactive();
    default boolean isActive()
    {
        return !isInactive();
    }

    boolean isForceLoaded( int cx, int cz );

    void save();

    int getMaxHeight();
    int getMinHeight();
}
