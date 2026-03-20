package pl.mateusz.downloader.api;

import java.nio.file.Path;
import java.util.Objects;

public record DownloadResult(
        Path target,
        long bytesDownloaded,
        int chunkCount
) {
    public DownloadResult {
        Objects.requireNonNull(target, "target cannot be null");

        if (bytesDownloaded < 0) {
            throw new IllegalArgumentException("bytesDownloaded cannot be negative");
        }

        if (chunkCount < 0) {
            throw new IllegalArgumentException("chunkCount cannot be negative");
        }
    }
}