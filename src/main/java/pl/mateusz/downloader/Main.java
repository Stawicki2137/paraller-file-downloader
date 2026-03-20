package pl.mateusz.downloader;

import pl.mateusz.downloader.api.DownloadRequest;
import pl.mateusz.downloader.infrastructure.http.JavaHttpRemoteFileClient;
import pl.mateusz.downloader.service.AdaptiveDownloadConfigurationResolver;
import pl.mateusz.downloader.service.FixedSizeChunkPlanner;
import pl.mateusz.downloader.service.InMemoryDownloadAssembler;
import pl.mateusz.downloader.service.ParallelFileDownloader;

import java.net.URI;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <file-url>");
            System.exit(1);
        }

        URI source = URI.create(args[0]);
        String originalFileName = extractFileName(source);
        Path target = Path.of("downloaded-" + originalFileName);

        var downloader = new ParallelFileDownloader(
                new JavaHttpRemoteFileClient(),
                new FixedSizeChunkPlanner(),
                new InMemoryDownloadAssembler(),
                new AdaptiveDownloadConfigurationResolver()
        );

        var request = new DownloadRequest(source, target);
        var result = downloader.download(request);

        System.out.println("Downloaded file: " + result.target());
        System.out.println("Bytes downloaded: " + result.bytesDownloaded());
        System.out.println("Chunk count: " + result.chunkCount());
    }

    private static String extractFileName(URI source) {
        String path = source.getPath();

        if (path == null || path.isBlank() || path.endsWith("/")) {
            throw new IllegalArgumentException("URL must point to a file");
        }

        int lastSlashIndex = path.lastIndexOf('/');
        return path.substring(lastSlashIndex + 1);
    }
}