package org.example.controller.restapi;

import org.example.controller.restapi.dto.ErrorResponse;
import org.example.controller.restapi.dto.UploadResponse;
import org.example.service.ImageService;
import org.example.service.dto.UploadResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024; // 20MB

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "imageId", required = false) Long imageId,
            @RequestParam(value = "slot", required = false) Integer slot,
            Principal principal
    ) {
        try {
            if (principal == null) {
                throw new ResponseStatusException(UNAUTHORIZED, "認証が必要");
            }
            if (file != null && file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new ResponseStatusException(PAYLOAD_TOO_LARGE, "ファイルサイズは20MB以下にしてください");
            }
            if (slot != null && (slot < 1 || slot > 2)) {
                throw new ResponseStatusException(BAD_REQUEST, "slotは1または2を指定してください");
            }

            String username = principal.getName();
            UploadResult result = imageService.upload(
                    username,
                    file,
                    Optional.ofNullable(imageId),
                    Optional.ofNullable(slot)
            );
            UploadResponse body = new UploadResponse("OK", result.imageId(), result.fileName());
            return ResponseEntity.ok(body);
        } catch (ResponseStatusException ex) {
            String message = ex.getReason() != null ? ex.getReason() : "エラーが発生しました";
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(new ErrorResponse("ERROR", message));
        }
    }
}