package pl.mateusz.downloader.domain;

public record RemoteFileMetadata(
        long contentLength,
        boolean acceptsRanges
) {
    public RemoteFileMetadata {
        if (contentLength < 0) {
            throw new IllegalArgumentException("contentLength cannot be negative");
        }
    }
}