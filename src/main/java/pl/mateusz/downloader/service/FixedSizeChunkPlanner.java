package pl.mateusz.downloader.service;

import pl.mateusz.downloader.domain.ByteRange;
import pl.mateusz.downloader.ports.ChunkPlanner;

import java.util.ArrayList;
import java.util.List;

public class FixedSizeChunkPlanner implements ChunkPlanner {

    @Override
    public List<ByteRange> planChunks(long contentLength, int chunkSizeBytes) {
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength cannot be negative");
        }

        if (chunkSizeBytes <= 0) {
            throw new IllegalArgumentException("chunkSizeBytes must be greater than 0");
        }

        if (contentLength == 0) {
            return List.of();
        }

        List<ByteRange> ranges = new ArrayList<>();
        long start = 0;

        while (start < contentLength) {
            long end = Math.min(start + chunkSizeBytes - 1, contentLength - 1);
            ranges.add(new ByteRange(start, end));
            start = end + 1;
        }

        return ranges;
    }
}