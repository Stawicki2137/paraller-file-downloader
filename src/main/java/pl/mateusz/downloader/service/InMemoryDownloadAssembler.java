package pl.mateusz.downloader.service;

import pl.mateusz.downloader.domain.ChunkData;
import pl.mateusz.downloader.exception.DownloadException;
import pl.mateusz.downloader.ports.DownloadAssembler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class InMemoryDownloadAssembler implements DownloadAssembler {

    @Override
    public void assemble(List<ChunkData> chunks, Path target) {
        Objects.requireNonNull(chunks, "chunks cannot be null");
        Objects.requireNonNull(target, "target cannot be null");

        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (chunks.isEmpty()) {
                Files.write(target, new byte[0]);
                return;
            }

            List<ChunkData> sortedChunks = new ArrayList<>(chunks);
            sortedChunks.sort(Comparator.comparingLong(chunk -> chunk.range().startInclusive()));

            long totalSize = sortedChunks.get(sortedChunks.size() - 1).range().endInclusive() + 1;
            if (totalSize > Integer.MAX_VALUE) {
                throw new DownloadException("In-memory assembly does not support files larger than 2 GB");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) totalSize);
            long expectedStart = 0;

            for (ChunkData chunk : sortedChunks) {
                long actualStart = chunk.range().startInclusive();
                if (actualStart != expectedStart) {
                    throw new DownloadException(
                            "Chunks are not contiguous. Expected next chunk to start at "
                                    + expectedStart + ", but got " + actualStart
                    );
                }

                outputStream.write(chunk.bytes());
                expectedStart = chunk.range().endInclusive() + 1;
            }

            Files.write(target, outputStream.toByteArray());
        } catch (IOException e) {
            throw new DownloadException("Failed to assemble downloaded chunks into target file", e);
        }
    }
}