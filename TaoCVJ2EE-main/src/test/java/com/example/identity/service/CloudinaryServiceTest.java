package com.example.identity.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() throws IOException {
        mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );


        // Ra lệnh: Khi service gọi cloudinary.uploader() -> trả về uploader (giả)
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    @DisplayName("Test uploadFile - Upload thành công")
    void uploadFile_success() throws IOException {
        // Given
        String expectedUrl = "http://example.com/image.jpg";


        Map<String, Object> uploadResult = Map.of("secure_url", expectedUrl);

        // Ra lệnh: Khi uploader.upload() được gọi -> trả về kết quả giả
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);
        // When
        String actualUrl = cloudinaryService.uploadFile(mockFile);

        // Then
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("Test uploadFile - Lỗi khi Cloudinary văng ra IOException")
    void uploadFile_throwsIOException() throws IOException {
        // Given
        // Ra lệnh: Khi uploader.upload() được gọi -> VĂNG RA LỖI
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Test IO error"));

        // When Then
        assertThatThrownBy(() -> cloudinaryService.uploadFile(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload thất bại");
    }
}