package pl.mateusz.downloader.infrastructure.http;

import pl.mateusz.downloader.domain.ByteRange;
import pl.mateusz.downloader.domain.ChunkData;
import pl.mateusz.downloader.domain.RemoteFileMetadata;
import pl.mateusz.downloader.exception.DownloadException;
import pl.mateusz.downloader.ports.RemoteFileClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.OptionalLong;

public class JavaHttpRemoteFileClient implements RemoteFileClient {

    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public JavaHttpRemoteFileClient() {
        this(HttpClient.newHttpClient(), Duration.ofSeconds(30));
    }

    public JavaHttpRemoteFileClient(HttpClient httpClient, Duration requestTimeout) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout cannot be null");
    }

    @Override
    public RemoteFileMetadata fetchMetadata(URI source) {
        HttpRequest request = baseRequestBuilder(source)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response = send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() != 200) {
            throw new DownloadException("HEAD request failed with status code " + response.statusCode());
        }

        OptionalLong contentLength = response.headers().firstValueAsLong("Content-Length");
        if (contentLength.isEmpty()) {
            throw new DownloadException("Missing Content-Length header in HEAD response");
        }

        boolean acceptsRanges = response.headers()
                .firstValue("Accept-Ranges")
                .map("bytes"::equalsIgnoreCase)
                .orElse(false);

        return new RemoteFileMetadata(contentLength.getAsLong(), acceptsRanges);
    }

    @Override
    public ChunkData downloadRange(URI source, ByteRange range) {
        HttpRequest request = baseRequestBuilder(source)
                .header("Range", range.toRangeHeader())
                .GET()
                .build();

        HttpResponse<byte[]> response = send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 206) {
            throw new DownloadException(
                    "Range GET request for " + range.toRangeHeader()
                            + " failed with status code " + response.statusCode()
            );
        }

        byte[] body = response.body();
        if (body.length != range.length()) {
            throw new DownloadException(
                    "Downloaded chunk size does not match expected range length for "
                            + range.toRangeHeader()
            );
        }

        return new ChunkData(range, body);
    }

    private HttpRequest.Builder baseRequestBuilder(URI source) {
        Objects.requireNonNull(source, "source cannot be null");
        return HttpRequest.newBuilder(source)
                .timeout(requestTimeout);
    }

    private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        try {
            return httpClient.send(request, bodyHandler);
        } catch (IOException e) {
            throw new DownloadException("HTTP communication failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DownloadException("HTTP request was interrupted", e);
        }
    }
}