package org.example.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    /**
     * 物理ストレージへ保存する。
     *
     * @param file         アップロードファイル
     * @param saveFileName 既にポリシーに沿って生成済みの保存ファイル名（例: IMG_0001_userA_1.jpg）
     * @return 保存結果（ファイル名・公開URL・サイズ）
     */
    StoredFile save(MultipartFile file, String saveFileName);
}
