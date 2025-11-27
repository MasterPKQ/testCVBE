package com.example.identity.controller;

import com.example.identity.exception.ErrorCode;
import com.example.identity.service.CloudinaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UploadController.class)

@AutoConfigureMockMvc(addFilters = false)
public class UploadControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private CloudinaryService cloudinaryService;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should upload file successfully and return URL")
    void uploadFile_success() throws Exception {
        // Given

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );


        String expectedUrl = "http://cloudinary.com/image/123.jpg";


        when(cloudinaryService.uploadFile(any(MultipartFile.class)))
                .thenReturn(expectedUrl);

        // When
        mockMvc.perform(multipart("/upload").file(mockFile))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Successfully uploaded"))
                .andExpect(jsonPath("$.result.url").value(expectedUrl));
    }

    @Test
    @DisplayName("Should return 400 when upload fails")
    void uploadFile_serviceFailure() throws Exception {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        String errorMessage = "Upload thất bại: test error";



        when(cloudinaryService.uploadFile(any(MultipartFile.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // When
        mockMvc.perform(multipart("/upload").file(mockFile))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(errorCode.getCode())) // 9999
                .andExpect(jsonPath("$.message").value(errorCode.getMessage())); // "Uncategorized error"
    }
}