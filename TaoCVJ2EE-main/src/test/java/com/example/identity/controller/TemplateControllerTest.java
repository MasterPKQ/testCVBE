package com.example.identity.controller;

import com.example.identity.dto.request.TemplateRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TemplateService templateService;

    private TemplateResponse templateResponse;
    private TemplateRequest templateRequest;

    @BeforeEach
    void setUp() {

        ObjectNode config = objectMapper.createObjectNode();
        config.put("font", "Arial");
        config.put("color", "blue");


        templateResponse = TemplateResponse.builder()
                .id(1L)
                .name("Modern CV")
                .category("IT")
                .style("modern")
                .isPremium(false)
                .templateConfig(config)
                .createdAt(LocalDateTime.now())
                .build();


        templateRequest = TemplateRequest.builder()
                .name("Modern CV")
                .category("IT")
                .style("modern")
                .isPremium(false)
                .templateConfig(config)
                .build();
    }

    @Test
    @DisplayName("Should get all templates successfully")
    void findAll_whenTemplatesExist() throws Exception {
        // Given

        when(templateService.findAll()).thenReturn(List.of(templateResponse));

        // When Then
        mockMvc.perform(get("/templates"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result[0].id").value(1L))
                .andExpect(jsonPath("$.result[0].name").value("Modern CV"));
    }

    @Test
    @DisplayName("Should get template by ID successfully")
    void findById_whenTemplateExists() throws Exception {
        // Given
        Long templateId = 1L;

        when(templateService.findById(templateId)).thenReturn(templateResponse);

        // When Then
        mockMvc.perform(get("/templates/{id}", templateId))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(templateId))
                .andExpect(jsonPath("$.result.name").value("Modern CV"));
    }

    @Test
    @DisplayName("Should return 404 when template not found")
    void findById_whenTemplateNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        ErrorCode errorCode = ErrorCode.TEMPLATE_NOT_FOUND;


        when(templateService.findById(nonExistentId))
                .thenThrow(new AppException(errorCode));

        // When Then
        mockMvc.perform(get("/templates/{id}", nonExistentId))

                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
    }

    @Test
    @DisplayName("Should create template successfully")
    void createTemplate_success() throws Exception {
        // Given

        when(templateService.createTemplate(any(TemplateRequest.class)))
                .thenReturn(templateResponse);

        // When Then
        mockMvc.perform(post("/templates")
                        .contentType(MediaType.APPLICATION_JSON)

                        .content(objectMapper.writeValueAsString(templateRequest)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("Modern CV"));
    }
}