package pl.mateusz.downloader.domain;

import java.util.Objects;

public record ChunkData(
        ByteRange range,
        byte[] bytes
) {
    public ChunkData {
        Objects.requireNonNull(range, "range cannot be null");
        Objects.requireNonNull(bytes, "bytes cannot be null");

        if (bytes.length != range.length()) {
            throw new IllegalArgumentException(
                    "Chunk byte array length does not match declared range length"
            );
        }
    }
}