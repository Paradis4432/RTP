package io.github.dailystruggle.rtp.common.selection.region;

import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPChunk;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class ChunkSet {
    public final List<CompletableFuture<RTPChunk>> chunks;
    public final CompletableFuture<Boolean> complete;

    public ChunkSet(List<CompletableFuture<RTPChunk>> chunks, CompletableFuture<Boolean> complete) {
        this.chunks = chunks;
        this.complete = complete;

        AtomicLong count = new AtomicLong();
        Semaphore countAccess = new Semaphore(1);
        chunks.forEach(rtpChunkCompletableFuture -> rtpChunkCompletableFuture.thenAccept(rtpChunk -> {
            try {
                countAccess.acquire();
                long i = count.incrementAndGet();
                if (i == chunks.size()) {
                    this.complete.complete(true);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                countAccess.release();
            }
        }));
    }

    public void keep(boolean keep) {
        chunks.forEach(chunk -> {
            if (chunk.isDone()) {
                try {
                    RTPChunk rtpChunk = chunk.get();
                    rtpChunk.keep(keep);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                chunk.thenAccept(chunk1 -> chunk1.keep(keep));
            }
        });
    }

    public void whenComplete(Consumer<Boolean> consumer) {
        if (complete.isDone()) {
            try {
                consumer.accept(complete.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return;
        }

        complete.thenAccept(consumer);
    }
}
