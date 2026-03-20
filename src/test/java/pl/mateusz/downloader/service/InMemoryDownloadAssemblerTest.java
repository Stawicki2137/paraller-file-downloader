package pl.mateusz.downloader.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.mateusz.downloader.domain.ByteRange;
import pl.mateusz.downloader.domain.ChunkData;
import pl.mateusz.downloader.exception.DownloadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDownloadAssemblerTest {

    private final InMemoryDownloadAssembler assembler = new InMemoryDownloadAssembler();

    @TempDir
    Path tempDir;

    @Test
    void shouldAssembleChunksInCorrectOrderEvenIfInputIsUnsorted() throws IOException {
        Path target = tempDir.resolve("result.txt");

        ChunkData second = new ChunkData(new ByteRange(4, 7), "1234".getBytes());
        ChunkData first = new ChunkData(new ByteRange(0, 3), "test".getBytes());

        assembler.assemble(List.of(second, first), target);

        byte[] result = Files.readAllBytes(target);
        assertArrayEquals("test1234".getBytes(), result);
    }

    @Test
    void shouldWriteEmptyFileWhenChunksListIsEmpty() throws IOException {
        Path target = tempDir.resolve("empty.bin");

        assembler.assemble(List.of(), target);

        assertTrue(Files.exists(target));
        assertEquals(0, Files.size(target));
    }

    @Test
    void shouldThrowWhenChunksAreNotContiguous() {
        Path target = tempDir.resolve("broken.bin");

        ChunkData first = new ChunkData(new ByteRange(0, 3), "test".getBytes());
        ChunkData third = new ChunkData(new ByteRange(8, 11), "abcd".getBytes());

        assertThrows(DownloadException.class, () ->
                assembler.assemble(List.of(first, third), target)
        );
    }
}