package com.example.identity.controller;

import com.example.identity.dto.request.CVSectionRequestReorder;
import com.example.identity.entity.CVSection;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.service.CVSectionService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CVSectionController.class)

@AutoConfigureMockMvc(addFilters = false)
public class CVSectionControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private CVSectionService cvSectionService;


    private final Long cvId = 1L;
    private final Long section1Id = 10L;
    private final Long section2Id = 11L;
    private CVSection section1;
    private CVSection section2;
    private List<CVSection> sectionList;

    @BeforeEach
    void setUp() {

        JsonNode section1Data = objectMapper.createObjectNode().put("name", "John Doe");
        JsonNode section2Data = objectMapper.createObjectNode().put("title", "Engineer");

        section1 = CVSection.builder()
                .id(section1Id)
                .cv(null)
                .sectionType("personal_info")
                .sectionData(section1Data)
                .orderIndex(1)
                .isVisible(true)
                .build();

        section2 = CVSection.builder()
                .id(section2Id)
                .cv(null)
                .sectionType("experience")
                .sectionData(section2Data)
                .orderIndex(2)
                .isVisible(true)
                .build();

        sectionList = List.of(section1, section2);
    }

    @Test
    @DisplayName("Should get list of sections by CV ID")
    void getSection_success() throws Exception {
        // Given

        when(cvSectionService.getListSectionByCVIdSorted(cvId)).thenReturn(sectionList);

        // When
        mockMvc.perform(get("/cvs/sections/{cvId}", cvId))
                //Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(section1Id));
    }

    @Test
    @DisplayName("Should create a new section")
    void createCVSection_success() throws Exception {
        // Given

        CVSection newSectionRequest = CVSection.builder()
                .sectionType("education")
                .orderIndex(3)
                .build();


        CVSection createdSection = CVSection.builder()
                .id(13L)
                .sectionType("education")
                .orderIndex(3)
                .build();

        when(cvSectionService.createCVSection(eq(cvId), any(CVSection.class)))
                .thenReturn(createdSection);

        // When/Then
        mockMvc.perform(post("/cvs/sections/{cvId}", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.result.id").value(13L))
                .andExpect(jsonPath("$.result.sectionType").value("education"));
    }

    @Test
    @DisplayName("Should update an existing section")
    void updateCVSection_success() throws Exception {
        // Given

        JsonNode updatedData = objectMapper.createObjectNode().put("name", "Jane Doe");
        CVSection updateRequest = CVSection.builder()
                .sectionData(updatedData)
                .isVisible(false)
                .build();


        section1.setSectionData(updatedData);
        section1.setIsVisible(false);

        when(cvSectionService.updateCVSection(eq(section1Id), any(CVSection.class)))
                .thenReturn(section1);

        // When/Then
        mockMvc.perform(put("/cvs/sections/{sectionId}", section1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.result.id").value(section1Id))
                .andExpect(jsonPath("$.result.sectionData.name").value("Jane Doe"))
                .andExpect(jsonPath("$.result.isVisible").value(false));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent section")
    void updateCVSection_notFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        CVSection updateRequest = CVSection.builder().sectionType("test").build();
        ErrorCode errorCode = ErrorCode.SECTION_NOT_FOUND;

        when(cvSectionService.updateCVSection(eq(nonExistentId), any(CVSection.class)))
                .thenThrow(new AppException(errorCode));

        // When/Then
        mockMvc.perform(put("/cvs/sections/{sectionId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound()) // Mong đợi 404
                .andExpect(jsonPath("$.code").value(errorCode.getCode())) // 1011
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
    }

    @Test
    @DisplayName("Should reorder sections successfully")
    void reorderSection_success() throws Exception {
        // Given

        List<CVSectionRequestReorder> reorderList = List.of(
                new CVSectionRequestReorder(section1Id, 2),
                new CVSectionRequestReorder(section2Id, 1)
        );


        section1.setOrderIndex(2);
        section2.setOrderIndex(1);
        List<CVSection> reorderedList = List.of(section2, section1);

        when(cvSectionService.reorderCVSection(eq(cvId), any(List.class)))
                .thenReturn(reorderedList);

        // When/Then
        mockMvc.perform(put("/cvs/sections/reorder/{cvId}", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(section2Id))
                .andExpect(jsonPath("$.result[0].orderIndex").value(1));
    }

    @Test
    @DisplayName("Should delete section successfully")
    void deleteCVSection_success() throws Exception {
        // Given
        when(cvSectionService.deleteCVSection(section1Id)).thenReturn(true);

        // When/Then
        mockMvc.perform(delete("/cvs/sections/{id}", section1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.result").value(true));
    }
}