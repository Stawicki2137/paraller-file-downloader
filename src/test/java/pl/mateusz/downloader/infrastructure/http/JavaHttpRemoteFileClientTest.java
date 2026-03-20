package pl.mateusz.downloader.infrastructure.http;

import org.junit.jupiter.api.Test;
import pl.mateusz.downloader.domain.ByteRange;
import pl.mateusz.downloader.domain.ChunkData;
import pl.mateusz.downloader.domain.RemoteFileMetadata;
import pl.mateusz.downloader.exception.DownloadException;
import pl.mateusz.downloader.testutil.RangeAwareTestHttpServer;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class JavaHttpRemoteFileClientTest {

    private final JavaHttpRemoteFileClient client = new JavaHttpRemoteFileClient();

    @Test
    void shouldFetchMetadata() throws Exception {
        byte[] content = "test1234".getBytes();

        try (RangeAwareTestHttpServer server = new RangeAwareTestHttpServer(content, true, true)) {
            RemoteFileMetadata metadata = client.fetchMetadata(URI.create(server.getFileUrl()));

            assertEquals(content.length, metadata.contentLength());
            assertTrue(metadata.acceptsRanges());
        }
    }

    @Test
    void shouldDownloadRange() throws Exception {
        byte[] content = "test1234".getBytes();

        try (RangeAwareTestHttpServer server = new RangeAwareTestHttpServer(content, true, true)) {
            ChunkData chunk = client.downloadRange(
                    URI.create(server.getFileUrl()),
                    new ByteRange(0, 3)
            );

            assertEquals(new ByteRange(0, 3), chunk.range());
            assertArrayEquals("test".getBytes(), chunk.bytes());
        }
    }

    @Test
    void shouldThrowWhenContentLengthIsMissing() throws Exception {
        byte[] content = "test1234".getBytes();

        try (RangeAwareTestHttpServer server = new RangeAwareTestHttpServer(content, true, false)) {
            assertThrows(DownloadException.class, () ->
                    client.fetchMetadata(URI.create(server.getFileUrl()))
            );
        }
    }

    @Test
    void shouldThrowWhenServerIsUnavailable() {
        assertThrows(DownloadException.class, () ->
                client.downloadRange(URI.create("http://localhost:1/file"), new ByteRange(0, 3))
        );
    }
}