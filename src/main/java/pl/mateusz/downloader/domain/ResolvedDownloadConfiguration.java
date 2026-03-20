package pl.mateusz.downloader.domain;

public record ResolvedDownloadConfiguration(
        int chunkSizeBytes,
        int parallelism
) {
    public ResolvedDownloadConfiguration {
        if (chunkSizeBytes <= 0) {
            throw new IllegalArgumentException("chunkSizeBytes must be greater than 0");
        }
        if (parallelism <= 0) {
            throw new IllegalArgumentException("parallelism must be greater than 0");
        }
    }
}