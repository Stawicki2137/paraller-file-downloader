package pl.mateusz.downloader.api;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

public record DownloadRequest(
        URI source,
        Path target
) {
    public DownloadRequest {
        Objects.requireNonNull(source, "source cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
    }
}