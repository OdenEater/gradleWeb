package org.example.service;

import org.example.service.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImageService {
    /**
     * 画像アップロード（置換時は replaceImageId を指定）
     * - ユーザの画像は最大2枚
     * - 許可拡張子／MIME、ファイル名ポリシー、所有者チェックは本メソッドで実施
     * - replaceImageId 未指定かつ slot 指定あり: 指定スロットが空なら保存、占有中なら 400
     */
    UploadResult upload(String username,
                        MultipartFile file,
                        Optional<Long> replaceImageId,
                        Optional<Integer> slot);
}