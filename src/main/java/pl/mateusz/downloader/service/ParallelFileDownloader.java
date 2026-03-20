package pl.mateusz.downloader.service;

import pl.mateusz.downloader.api.DownloadRequest;
import pl.mateusz.downloader.api.DownloadResult;
import pl.mateusz.downloader.api.FileDownloader;
import pl.mateusz.downloader.domain.ByteRange;
import pl.mateusz.downloader.domain.ChunkData;
import pl.mateusz.downloader.domain.RemoteFileMetadata;
import pl.mateusz.downloader.domain.ResolvedDownloadConfiguration;
import pl.mateusz.downloader.exception.DownloadException;
import pl.mateusz.downloader.ports.ChunkPlanner;
import pl.mateusz.downloader.ports.DownloadAssembler;
import pl.mateusz.downloader.ports.DownloadConfigurationResolver;
import pl.mateusz.downloader.ports.RemoteFileClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelFileDownloader implements FileDownloader {

    private final RemoteFileClient remoteFileClient;
    private final ChunkPlanner chunkPlanner;
    private final DownloadAssembler downloadAssembler;
    private final DownloadConfigurationResolver configurationResolver;

    public ParallelFileDownloader(
            RemoteFileClient remoteFileClient,
            ChunkPlanner chunkPlanner,
            DownloadAssembler downloadAssembler,
            DownloadConfigurationResolver configurationResolver
    ) {
        this.remoteFileClient = Objects.requireNonNull(remoteFileClient, "remoteFileClient cannot be null");
        this.chunkPlanner = Objects.requireNonNull(chunkPlanner, "chunkPlanner cannot be null");
        this.downloadAssembler = Objects.requireNonNull(downloadAssembler, "downloadAssembler cannot be null");
        this.configurationResolver = Objects.requireNonNull(configurationResolver, "configurationResolver cannot be null");
    }

    @Override
    public DownloadResult download(DownloadRequest request) {
        Objects.requireNonNull(request, "request cannot be null");

        RemoteFileMetadata metadata = remoteFileClient.fetchMetadata(request.source());

        if (!metadata.acceptsRanges()) {
            throw new DownloadException("Server does not support byte range requests");
        }

        ResolvedDownloadConfiguration configuration = configurationResolver.resolve(metadata.contentLength());

        List<ByteRange> ranges = chunkPlanner.planChunks(
                metadata.contentLength(),
                configuration.chunkSizeBytes()
        );

        List<ChunkData> chunks = downloadChunksInParallel(
                request,
                ranges,
                configuration.parallelism()
        );

        downloadAssembler.assemble(chunks, request.target());

        return new DownloadResult(
                request.target(),
                metadata.contentLength(),
                ranges.size()
        );
    }

    private List<ChunkData> downloadChunksInParallel(
            DownloadRequest request,
            List<ByteRange> ranges,
            int parallelism
    ) {
        if (ranges.isEmpty()) {
            return List.of();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);

        try {
            List<Callable<ChunkData>> tasks = ranges.stream()
                    .<Callable<ChunkData>>map(range -> () -> remoteFileClient.downloadRange(request.source(), range))
                    .toList();

            List<Future<ChunkData>> futures = executorService.invokeAll(tasks);
            List<ChunkData> chunks = new ArrayList<>(futures.size());

            for (Future<ChunkData> future : futures) {
                try {
                    chunks.add(future.get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    throw new DownloadException("Failed to download one of the file chunks", cause);
                }
            }

            return chunks;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DownloadException("Download was interrupted", e);
        } finally {
            executorService.shutdownNow();
        }
    }
}