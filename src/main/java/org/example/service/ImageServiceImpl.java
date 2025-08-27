package org.example.service;

import org.example.service.dto.UploadResult;
import org.example.service.storage.ImageStorageService;
import org.example.service.storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "heic");
    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png", "image/heic", "image/heif");
    private static final Map<String, Set<String>> MIME_BY_EXT = Map.of(
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png", Set.of("image/png"),
            "heic", Set.of("image/heic", "image/heif")
    );

    private final ImageStorageService storage;

    public ImageServiceImpl(ImageStorageService storage) {
        this.storage = storage;
    }

    @Override
    public UploadResult upload(String username,
                               MultipartFile file,
                               Optional<Long> replaceImageId,
                               Optional<Integer> slotOpt) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "ファイルが指定されていません");
        }

        String origName = Objects.requireNonNullElse(file.getOriginalFilename(), "").trim();
        if (origName.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "ファイル名が不正です");
        }
        String ext = getExtension(origName).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ResponseStatusException(BAD_REQUEST, "許可されていないファイル形式です");
        }
        String mime = Objects.requireNonNullElse(file.getContentType(), "");
        if (!ALLOWED_MIME.contains(mime)) {
            throw new ResponseStatusException(BAD_REQUEST, "許可されていないMIMEタイプです");
        }
        if (!MIME_BY_EXT.getOrDefault(ext, Set.of()).contains(mime)) {
            throw new ResponseStatusException(BAD_REQUEST, "許可されていないファイル形式です");
        }

        log.info("upload start user={} name={} size={}", username, origName, file.getSize());

        int slot;
        long imageId;

        if (replaceImageId.isPresent()) {
            long targetId = replaceImageId.get();
            if (!isOwner(username, targetId)) {
                throw new ResponseStatusException(FORBIDDEN, "権限がありません");
            }
            // 置換時は既存のスロットを必ず継承（slotパラメータは無視）
            slot = getSlotByImageId(targetId); // 1 or 2
            imageId = targetId;
        } else {
            if (slotOpt.isPresent()) {
                int requested = slotOpt.get();
                if (requested != 1 && requested != 2) {
                    throw new ResponseStatusException(BAD_REQUEST, "slotは1または2を指定してください");
                }
                if (isSlotOccupied(username, requested)) {
                    throw new ResponseStatusException(BAD_REQUEST, "指定したスロットは使用中です");
                }
                slot = requested;
                imageId = provisionalId();
            } else {
                int current = countImages(username);
                if (current >= 2) {
                    throw new ResponseStatusException(BAD_REQUEST, "画像は最大2枚までです");
                }
                slot = current + 1; // 1 or 2
                imageId = provisionalId(); // TODO: DB採番に置換
            }
        }

        String base = stripExtension(origName);
        String saveName = String.format("%s_%s_%d.%s", base, username, slot, ext);

        StoredFile stored = storage.save(file, saveName);

        log.info("upload done user={} id={} saveName={}", username, imageId, saveName);

        // TODO: DBにメタ情報保存（userId, fileName, fileUrl, extension, size, slot 等）
        return new UploadResult(imageId, stored.fileName());
    }

    private static String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0) ? filename.substring(idx + 1) : "";
    }

    private static String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0) ? filename.substring(0, idx) : filename;
    }

    // --- 以下は後日DB実装に置換 ---
    private int countImages(String username) {
        return 0;
    }

    private boolean isOwner(String username, long imageId) {
        return true;
    }

    private int getSlotByImageId(long imageId) {
        // TODO: DBから対象画像のスロット(1|2)を取得する。スタブは常に 1 を返す。
        return 1;
    }

    private boolean isSlotOccupied(String username, int slot) {
        // TODO: DBでユーザの該当スロットに画像が存在するか判定
        return false;
    }

    private long provisionalId() {
        return System.nanoTime();
    }
}