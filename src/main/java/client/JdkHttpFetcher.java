package client;

import exception.LoanLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class JdkHttpFetcher implements HttpFetcher {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_BODY_BYTES = 1_000_000;

    private final HttpClient client;
    private final Duration requestTimeout;

    public JdkHttpFetcher() {
        this(DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
    }

    public JdkHttpFetcher(Duration connectTimeout, Duration requestTimeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.requestTimeout = requestTimeout;
    }

    @Override
    public String get(String url) {
        URI uri = toUri(url);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();

        if (scheme.equals("file")) {
            return readLocalFile(uri);
        }
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new LoanLoadException("URL harus diawali http://, https://, atau file://, bukan: " + url);
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw new LoanLoadException(
                            "Web service membalas HTTP " + response.statusCode() + " (diharapkan 200)");
                }
                return readLimited(body);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoanLoadException("Permintaan ke web service dibatalkan");
        } catch (HttpTimeoutException e) {
            throw new LoanLoadException("Web service tidak merespons (timeout)");
        } catch (IOException e) {
            throw new LoanLoadException("Gagal menghubungi web service: " + describe(e));
        }
    }

    private static String describe(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t.getMessage() != null && !t.getMessage().isBlank()) {
                return t.getMessage();
            }
        }
        return error.getClass().getSimpleName();
    }

    private static URI toUri(String url) {
        if (url == null || url.isBlank()) {
            throw new LoanLoadException("URL web service tidak boleh kosong");
        }
        try {
            return URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            throw new LoanLoadException("URL tidak valid: " + url);
        }
    }

    private static String readLocalFile(URI uri) {
        try {
            Path path = Path.of(uri);
            try (InputStream in = Files.newInputStream(path)) {
                return readLimited(in);
            }
        } catch (IllegalArgumentException | FileSystemNotFoundException e) {
            throw new LoanLoadException("URL file:// tidak valid (pakai path absolut): " + uri);
        } catch (IOException e) {
            throw new LoanLoadException("Gagal membaca file " + uri + ": " + describe(e));
        }
    }

    private static String readLimited(InputStream in) throws IOException {
        byte[] bytes = in.readNBytes(MAX_BODY_BYTES + 1);
        if (bytes.length > MAX_BODY_BYTES) {
            throw new LoanLoadException("Response terlalu besar (maksimal 1 MB)");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}