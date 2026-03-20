package pl.mateusz.downloader.ports;

import pl.mateusz.downloader.domain.ResolvedDownloadConfiguration;

public interface DownloadConfigurationResolver {
    ResolvedDownloadConfiguration resolve(long contentLength);
}