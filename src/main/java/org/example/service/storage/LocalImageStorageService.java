package org.example.service.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Profile("dev")
public class LocalImageStorageService implements ImageStorageService {

    private final Path baseDir;
    private final String publicBaseUrl;

    public LocalImageStorageService(
            @Value("${app.storage.local.base-dir:./uploads}") String baseDir,
            @Value("${app.storage.local.public-base-url:}") String publicBaseUrl
    ) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "保存先ディレクトリを作成できませんでした", e);
        }
    }

    @Override
    public StoredFile save(MultipartFile file, String saveFileName) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "ファイルが指定されていません");
        }
        String cleanName = sanitize(saveFileName);
        Path target = baseDir.resolve(cleanName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new ResponseStatusException(BAD_REQUEST, "無効なファイルパスです");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ファイル保存に失敗しました", e);
        }

        String url = buildPublicUrl(cleanName, target);
        return new StoredFile(cleanName, url, file.getSize());
    }

    private String sanitize(String name) {
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "不正なファイル名です");
        }
        return base;
    }

    private String buildPublicUrl(String fileName, Path absolutePath) {
        if (publicBaseUrl.isEmpty()) {
            return absolutePath.toUri().toString();
        }
        return publicBaseUrl.endsWith("/") ? publicBaseUrl + fileName : publicBaseUrl + "/" + fileName;
    }
}