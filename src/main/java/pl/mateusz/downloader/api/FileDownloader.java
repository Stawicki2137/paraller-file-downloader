package pl.mateusz.downloader.api;

public interface FileDownloader {
    DownloadResult download(DownloadRequest request);
}