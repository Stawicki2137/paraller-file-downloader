package pl.mateusz.downloader.domain;

public record ByteRange(long startInclusive, long endInclusive) {

    public ByteRange {
        if (startInclusive < 0) {
            throw new IllegalArgumentException("startInclusive cannot be negative");
        }

        if (endInclusive < startInclusive) {
            throw new IllegalArgumentException("endInclusive cannot be smaller than startInclusive");
        }
    }

    public long length() {
        return endInclusive - startInclusive + 1;
    }

    public String toRangeHeader() {
        return "bytes=" + startInclusive + "-" + endInclusive;
    }
}