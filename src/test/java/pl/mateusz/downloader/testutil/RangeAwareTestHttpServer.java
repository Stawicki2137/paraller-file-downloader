package pl.mateusz.downloader.testutil;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class RangeAwareTestHttpServer implements AutoCloseable {

    private final HttpServer server;
    private final byte[] content;
    private final boolean acceptRanges;
    private final boolean includeContentLength;

    public RangeAwareTestHttpServer(byte[] content, boolean acceptRanges, boolean includeContentLength) throws IOException {
        this.content = content;
        this.acceptRanges = acceptRanges;
        this.includeContentLength = includeContentLength;

        this.server = HttpServer.create(new InetSocketAddress(0), 0);
        this.server.createContext("/file", this::handleRequest);
        this.server.start();
    }

    public String getFileUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/file";
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        Headers responseHeaders = exchange.getResponseHeaders();

        if (acceptRanges) {
            responseHeaders.add("Accept-Ranges", "bytes");
        }

        if (includeContentLength) {
            responseHeaders.add("Content-Length", String.valueOf(content.length));
        }

        if ("HEAD".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }

        if ("GET".equalsIgnoreCase(method)) {
            String rangeHeader = exchange.getRequestHeaders().getFirst("Range");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] parts = rangeHeader.substring("bytes=".length()).split("-");
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);

                byte[] slice = new byte[end - start + 1];
                System.arraycopy(content, start, slice, 0, slice.length);

                responseHeaders.add("Content-Range", "bytes " + start + "-" + end + "/" + content.length);
                exchange.sendResponseHeaders(206, slice.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(slice);
                }
                return;
            }

            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
            return;
        }

        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}