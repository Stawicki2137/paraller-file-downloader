# Parallel File Downloader

Java application for downloading files in parallel using HTTP range requests.

## Requirements

- Java 21
- Maven 3+
- Docker (optional, for local manual testing)

## Build

```bash
mvn compile
```

## Run tests

```bash
mvn test
```

## Run from command line

```bash
java -cp target/classes pl.mateusz.downloader.Main http://localhost:8080/plik.txt
```

The downloaded file will be saved as:

```text
downloaded-plik.txt
```

## Local test server with Docker

Create a directory with files to serve, for example:

```bash
mkdir -p ~/test-downloads
echo "hello downloader test" > ~/test-downloads/plik.txt
dd if=/dev/urandom of=~/test-downloads/bigfile.bin bs=1M count=50
```

Start the server:

```bash
docker run --rm -p 8080:80 -v ~/test-downloads:/usr/local/apache2/htdocs/ httpd:latest
```

Example URLs:

```text
http://localhost:8080/plik.txt
http://localhost:8080/bigfile.bin
```

## Verify server headers

```bash
curl -I http://localhost:8080/plik.txt
curl -i -H "Range: bytes=0-3" http://localhost:8080/plik.txt
```

## Verify downloaded file

```bash
cmp ~/test-downloads/bigfile.bin downloaded-bigfile.bin
```

or

```bash
sha256sum ~/test-downloads/bigfile.bin downloaded-bigfile.bin
```

## Notes

The downloader:
- sends a `HEAD` request to read `Content-Length` and `Accept-Ranges`
- splits the file into byte ranges
- downloads chunks in parallel
- assembles the final file locally

Current limitation:
- file assembly is done in memory
