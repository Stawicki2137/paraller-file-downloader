package pl.mateusz.downloader.service;

import pl.mateusz.downloader.domain.ResolvedDownloadConfiguration;
import pl.mateusz.downloader.ports.DownloadConfigurationResolver;

public class AdaptiveDownloadConfigurationResolver implements DownloadConfigurationResolver {

    private static final int ONE_MB = 1024 * 1024;

    @Override
    public ResolvedDownloadConfiguration resolve(long contentLength) {
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength cannot be negative");
        }

        int chunkSizeBytes;

        if (contentLength <= 1L * ONE_MB) {
            chunkSizeBytes = 64 * 1024;          // 64 KB
        } else if (contentLength <= 10L * ONE_MB) {
            chunkSizeBytes = 256 * 1024;         // 256 KB
        } else if (contentLength <= 100L * ONE_MB) {
            chunkSizeBytes = 1 * ONE_MB;         // 1 MB
        } else if (contentLength <= 1024L * ONE_MB) {
            chunkSizeBytes = 4 * ONE_MB;         // 4 MB
        } else {
            chunkSizeBytes = 8 * ONE_MB;         // 8 MB
        }

        long chunkCountLong = contentLength == 0 ? 0 : (contentLength + chunkSizeBytes - 1) / chunkSizeBytes;
        int chunkCount = (int) Math.max(1, chunkCountLong);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int preferredParallelism = Math.max(2, availableProcessors * 2);

        int parallelism = Math.min(preferredParallelism, chunkCount);
        parallelism = Math.max(1, parallelism);

        return new ResolvedDownloadConfiguration(chunkSizeBytes, parallelism);
    }
}