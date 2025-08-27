// java
package org.example.service;

import org.example.service.dto.UploadResult;
import org.example.service.storage.ImageStorageService;
import org.example.service.storage.StoredFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceImplTest {

    private ImageStorageService storage;
    private ImageServiceImpl service;

    @BeforeEach
    void setUp() {
        storage = mock(ImageStorageService.class);
        service = new ImageServiceImpl(storage);
    }

    @Test
    void upload_nullFile_400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", null, Optional.empty(), Optional.empty()));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("ファイルが指定されていません", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_emptyFile_400() {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[0]);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", file, Optional.empty(), Optional.empty()));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("ファイルが指定されていません", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_emptyFilename_400() {
        MockMultipartFile file = new MockMultipartFile("file", null, "image/jpeg", "x".getBytes(StandardCharsets.UTF_8));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", file, Optional.empty(), Optional.empty()));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("ファイル名が不正です", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_invalidExtension_400() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "x".getBytes());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", file, Optional.empty(), Optional.empty()));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("許可されていないファイル形式です", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_invalidMime_400() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "application/pdf", "x".getBytes());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", file, Optional.empty(), Optional.empty()));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("許可されていないMIMEタイプです", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_mismatchExtAndMime_400() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/png", "x".getBytes());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", file, Optional.empty(), Optional.empty()));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("許可されていないファイル形式です", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_new_success_generatesExpectedSaveNameAndLowercasesExt() {
        // JPG(大文字) -> jpg に正規化。スロットはスタブにより 1。
        MockMultipartFile file = new MockMultipartFile("file", "IMG_0001.JPG", "image/jpeg", "content".getBytes());
        ArgumentCaptor<String> saveNameCaptor = ArgumentCaptor.forClass(String.class);

        when(storage.save(eq(file), saveNameCaptor.capture()))
                .thenAnswer(inv -> new StoredFile(saveNameCaptor.getValue(), "http://localhost/f", file.getSize()));

        UploadResult result = service.upload("alice", file, Optional.empty(), Optional.empty());

        String savedName = saveNameCaptor.getValue();
        assertEquals(result.fileName(), savedName);
        assertTrue(savedName.endsWith("_alice_1.jpg"), "保存名は *_alice_1.jpg のはず");
        assertTrue(savedName.startsWith("IMG_0001_"), "元名ベースを含むはず");
        verify(storage, times(1)).save(eq(file), anyString());
    }

    @Test
    void upload_replace_success_returnsGivenImageIdAndKeepsSlotFromStub() {
        MockMultipartFile file = new MockMultipartFile("file", "P1.png", "image/png", "x".getBytes());
        ArgumentCaptor<String> saveNameCaptor = ArgumentCaptor.forClass(String.class);
        when(storage.save(eq(file), saveNameCaptor.capture()))
                .thenReturn(new StoredFile("ignored", "u", file.getSize()));

        long replaceId = 42L;
        UploadResult result = service.upload("bob", file, Optional.of(replaceId), Optional.empty());

        assertEquals(replaceId, result.imageId());
        String savedName = saveNameCaptor.getValue();
        assertTrue(savedName.endsWith("_bob_1.png"), "置換時はスタブのスロット1を継承");
        verify(storage, times(1)).save(eq(file), anyString());
    }

    @Test
    void upload_heic_and_heifMime_success() {
        MockMultipartFile file = new MockMultipartFile("file", "A.heic", "image/heif", "x".getBytes());
        when(storage.save(any(), anyString())).thenReturn(new StoredFile("A_bob_1.heic", "u", file.getSize()));

        UploadResult result = service.upload("bob", file, Optional.empty(), Optional.empty());

        assertNotNull(result);
        verify(storage, times(1)).save(eq(file), anyString());
    }

    // --- 追加: slot 関連 ---

    @Test
    void upload_withSlot2_success_usesSlot2() {
        MockMultipartFile file = new MockMultipartFile("file", "p.png", "image/png", "x".getBytes());
        ArgumentCaptor<String> saveNameCaptor = ArgumentCaptor.forClass(String.class);
        when(storage.save(eq(file), saveNameCaptor.capture()))
                .thenAnswer(inv -> new StoredFile(saveNameCaptor.getValue(), "u", file.getSize()));

        UploadResult result = service.upload("alice", file, Optional.empty(), Optional.of(2));

        String savedName = saveNameCaptor.getValue();
        assertEquals(result.fileName(), savedName);
        assertTrue(savedName.endsWith("_alice_2.png"), "slot=2 指定時は *_alice_2.png");
        verify(storage, times(1)).save(eq(file), anyString());
    }

    @Test
    void upload_withInvalidSlot_400() {
        MockMultipartFile file = new MockMultipartFile("file", "p.png", "image/png", "x".getBytes());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("alice", file, Optional.empty(), Optional.of(3)));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("slotは1または2を指定してください", ex.getReason());
        verifyNoInteractions(storage);
    }

    @Test
    void upload_replace_ignoresSlotParam_keepsExistingSlotFromStub() {
        MockMultipartFile file = new MockMultipartFile("file", "p.png", "image/png", "x".getBytes());
        ArgumentCaptor<String> saveNameCaptor = ArgumentCaptor.forClass(String.class);
        when(storage.save(eq(file), saveNameCaptor.capture()))
                .thenAnswer(inv -> new StoredFile(saveNameCaptor.getValue(), "u", file.getSize()));

        long replaceId = 100L;
        UploadResult result = service.upload("bob", file, Optional.of(replaceId), Optional.of(2)); // slotは無視される

        assertEquals(replaceId, result.imageId());
        String savedName = saveNameCaptor.getValue();
        assertTrue(savedName.endsWith("_bob_1.png"), "置換時は既存スロット(スタブで1)を継承すべき");
        verify(storage, times(1)).save(eq(file), anyString());
    }
}