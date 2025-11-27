package com.example.identity.controller;

import com.example.identity.dto.request.CVRequest;
import com.example.identity.dto.response.CVResponse;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.service.CVService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CVController.class)

@AutoConfigureMockMvc(addFilters = false)
public class CVControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private CVService cvService;

    private CVRequest cvRequest;
    private CVResponse cvResponse;
    private final Long cvId = 1L;

    @BeforeEach
    void setUp() {
        JsonNode dummyCvData = objectMapper.createObjectNode().put("title", "Software Engineer");

        cvRequest = CVRequest.builder()
                .name("My First CV")
                .templateId(1L)
                .cvData(dummyCvData)
                .build();

        cvResponse = CVResponse.builder()
                .id(cvId)
                .name("My First CV")
                .templateId(1L)
                .cvData(dummyCvData)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get all CVs for the user")
    void getAllMyCV_success() throws Exception {

        when(cvService.getAllMyCV()).thenReturn(List.of(cvResponse));

        // When
        mockMvc.perform(get("/cvs"))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Tải danh sách CV thành công"))
                .andExpect(jsonPath("$.result[0].id").value(cvId));
    }

    @Test
    @DisplayName("Should create a new CV successfully")
    void createCV_success() throws Exception {
        // Given
        when(cvService.createCV(any(CVRequest.class))).thenReturn(cvResponse);

        // When/Then
        mockMvc.perform(post("/cvs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cvRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Tạo CV thành công"))
                .andExpect(jsonPath("$.result.id").value(cvId));
    }

    @Test
    @DisplayName("Should get CV by ID successfully")
    void getById_success() throws Exception {
        // Given
        when(cvService.getCVById(cvId)).thenReturn(cvResponse);

        // When/Then
        mockMvc.perform(get("/cvs/{id}", cvId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Tải CV thành công"))
                .andExpect(jsonPath("$.result.id").value(cvId));
    }

    @Test
    @DisplayName("Should return 404 when CV not found")
    void getById_notFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        ErrorCode errorCode = ErrorCode.CV_NOT_FOUND;


        when(cvService.getCVById(nonExistentId))
                .thenThrow(new AppException(errorCode));

        // When/Then
        mockMvc.perform(get("/cvs/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
    }

    @Test
    @DisplayName("Should update CV successfully")
    void updateCV_success() throws Exception {
        // Given
        when(cvService.updateCV(eq(cvId), any(CVRequest.class))).thenReturn(cvResponse);

        // When/Then

        mockMvc.perform(post("/cvs/{id}", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cvRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Cập nhật CV thành công"))
                .andExpect(jsonPath("$.result.id").value(cvId));
    }

    @Test
    @DisplayName("Should delete CV successfully")
    void deleteCV_success() throws Exception {
        // Given
        when(cvService.deleteCV(cvId)).thenReturn(true);

        // When/Then
        mockMvc.perform(delete("/cvs/{id}", cvId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Xóa CV thành công"))
                .andExpect(jsonPath("$.result.deleted").value(true));
    }

    @Test
    @DisplayName("Should duplicate CV successfully")
    void duplicateCV_success() throws Exception {
        // Given

        CVResponse duplicatedResponse = CVResponse.builder()
                .id(2L)
                .name(cvResponse.getName())
                .templateId(cvResponse.getTemplateId())
                .build();
        
        when(cvService.duplicateCV(cvId)).thenReturn(duplicatedResponse);

        // When/Then
        mockMvc.perform(post("/cvs/{id}/duplicate", cvId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Nhân đôi CV thành công"))
                .andExpect(jsonPath("$.result.id").value(2L));
    }
}