package pl.mateusz.downloader.ports;

import pl.mateusz.downloader.domain.ByteRange;

import java.util.List;

public interface ChunkPlanner {
    List<ByteRange> planChunks(long contentLength, int chunkSizeBytes);
}