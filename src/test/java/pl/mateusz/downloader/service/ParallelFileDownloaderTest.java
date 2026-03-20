package pl.mateusz.downloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.mateusz.downloader.api.DownloadRequest;
import pl.mateusz.downloader.api.DownloadResult;
import pl.mateusz.downloader.infrastructure.http.JavaHttpRemoteFileClient;
import pl.mateusz.downloader.testutil.RangeAwareTestHttpServer;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ParallelFileDownloaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadAndAssembleWholeFile() throws Exception {
        byte[] content = "This is a test file for downloader.".getBytes();

        try (RangeAwareTestHttpServer server = new RangeAwareTestHttpServer(content, true, true)) {
            ParallelFileDownloader downloader = new ParallelFileDownloader(
                    new JavaHttpRemoteFileClient(),
                    new FixedSizeChunkPlanner(),
                    new InMemoryDownloadAssembler(),
                    new AdaptiveDownloadConfigurationResolver()
            );

            Path target = tempDir.resolve("downloaded-file.txt");
            DownloadRequest request = new DownloadRequest(URI.create(server.getFileUrl()), target);

            DownloadResult result = downloader.download(request);

            assertEquals(target, result.target());
            assertEquals(content.length, result.bytesDownloaded());
            assertTrue(result.chunkCount() >= 1);
            assertArrayEquals(content, Files.readAllBytes(target));
        }
    }
}