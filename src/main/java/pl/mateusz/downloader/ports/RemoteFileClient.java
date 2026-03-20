package pl.mateusz.downloader.ports;

import pl.mateusz.downloader.domain.ByteRange;
import pl.mateusz.downloader.domain.ChunkData;
import pl.mateusz.downloader.domain.RemoteFileMetadata;

import java.net.URI;

public interface RemoteFileClient {
    RemoteFileMetadata fetchMetadata(URI source);

    ChunkData downloadRange(URI source, ByteRange range);
}