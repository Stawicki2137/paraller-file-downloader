package pl.mateusz.downloader.ports;

import pl.mateusz.downloader.domain.ChunkData;

import java.nio.file.Path;
import java.util.List;

public interface DownloadAssembler {
    void assemble(List<ChunkData> chunks, Path target);
}