// java
package org.example.controller.restapi;

import org.example.service.ImageService;
import org.example.service.dto.UploadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ImageService imageService;

    @Test
    @DisplayName("未認証でアップロード -> 401 JSON")
    void upload_unauthorized_401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "x".getBytes());

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("認証が必要"));

        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("ファイルサイズ超過 -> 413 JSON（サービス未呼び出し）")
    void upload_sizeExceeded_413() throws Exception {
        byte[] big = new byte[(int)(20L * 1024 * 1024) + 1]; // 20MB+1
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", big);

        mockMvc.perform(multipart("/api/images/upload").file(file).principal(() -> "alice"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("ファイルサイズは20MB以下にしてください"));

        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("許可されていない拡張子 -> 400 JSON")
    void upload_invalidExtension_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "x".getBytes());
        when(imageService.upload(eq("alice"), any(), any(), any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "許可されていないファイル形式です"));

        mockMvc.perform(multipart("/api/images/upload").file(file).principal(() -> "alice"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("許可されていないファイル形式です"));

        verify(imageService, times(1)).upload(eq("alice"), any(), any(), any());
    }

    @Test
    @DisplayName("許可されていないMIMEタイプ -> 400 JSON")
    void upload_invalidMime_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "application/pdf", "x".getBytes());
        when(imageService.upload(eq("alice"), any(), any(), any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "許可されていないMIMEタイプです"));

        mockMvc.perform(multipart("/api/images/upload").file(file).principal(() -> "alice"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("許可されていないMIMEタイプです"));

        verify(imageService, times(1)).upload(eq("alice"), any(), any(), any());
    }

    @Test
    @DisplayName("拡張子とMIME不一致 -> 400 JSON")
    void upload_mismatchExtAndMime_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/png", "x".getBytes());
        when(imageService.upload(eq("alice"), any(), any(), any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "許可されていないファイル形式です"));

        mockMvc.perform(multipart("/api/images/upload").file(file).principal(() -> "alice"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("許可されていないファイル形式です"));

        verify(imageService, times(1)).upload(eq("alice"), any(), any(), any());
    }

    @Test
    @DisplayName("画像置換: 他ユーザ -> 403 JSON")
    void upload_replace_forbidden_403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", "x".getBytes());
        when(imageService.upload(eq("bob"), any(), eq(Optional.of(99L)), any()))
                .thenThrow(new ResponseStatusException(FORBIDDEN, "権限がありません"));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .param("imageId", "99")
                        .principal(() -> "bob"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("権限がありません"));

        verify(imageService, times(1)).upload(eq("bob"), any(), eq(Optional.of(99L)), any());
    }

    @Test
    @DisplayName("画像置換: 正常 -> 200 JSON")
    void upload_replace_ok_200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", "x".getBytes());
        when(imageService.upload(eq("bob"), any(), eq(Optional.of(42L)), any()))
                .thenReturn(new UploadResult(42L, "x_bob_1.png"));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .param("imageId", "42")
                        .principal(() -> "bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.imageId").value(42))
                .andExpect(jsonPath("$.fileName").value("x_bob_1.png"));

        verify(imageService, times(1)).upload(eq("bob"), any(), eq(Optional.of(42L)), any());
    }

    @Test
    @DisplayName("新規アップロード: 正常 -> 200 JSON")
    void upload_new_ok_200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "p.jpg", "image/jpeg", "x".getBytes());
        when(imageService.upload(eq("alice"), any(), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(new UploadResult(100L, "p_alice_1.jpg"));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .principal(() -> "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.imageId").value(100))
                .andExpect(jsonPath("$.fileName").value("p_alice_1.jpg"));

        verify(imageService, times(1)).upload(eq("alice"), any(), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    @DisplayName("新規アップロード: スロット満杯 -> 400 JSON")
    void upload_fullSlots_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "p.jpg", "image/jpeg", "x".getBytes());
        when(imageService.upload(eq("alice"), any(), eq(Optional.empty()), eq(Optional.empty())))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "画像は最大2枚までです"));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .principal(() -> "alice"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("画像は最大2枚までです"));

        verify(imageService, times(1)).upload(eq("alice"), any(), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    @DisplayName("slotが1|2以外 -> 400 JSON（サービス未呼び出し）")
    void upload_invalidSlotParam_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "p.jpg", "image/jpeg", "x".getBytes());

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .param("slot", "3")
                        .principal(() -> "alice"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERROR"))
                .andExpect(jsonPath("$.message").value("slotは1または2を指定してください"));

        verifyNoInteractions(imageService);
    }
}