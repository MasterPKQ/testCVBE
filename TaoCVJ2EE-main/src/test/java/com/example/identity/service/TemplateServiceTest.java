package com.example.identity.service;

import com.example.identity.dto.request.TemplateRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.entity.Template;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.TemplateMapper;
import com.example.identity.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateMapper templateMapper;


    @InjectMocks
    private TemplateService templateService;

    private Template template;
    private TemplateResponse templateResponse;
    private TemplateRequest templateRequest;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode config = objectMapper.createObjectNode().put("font", "Arial");

        template = Template.builder()
                .id(1L)
                .name("Modern CV")
                .category("IT")
                .templateConfig(config)
                .createdAt(LocalDateTime.now())
                .build();

        templateResponse = TemplateResponse.builder()
                .id(1L)
                .name("Modern CV")
                .category("IT")
                .templateConfig(config)
                .build();

        templateRequest = TemplateRequest.builder()
                .name("Modern CV")
                .category("IT")
                .templateConfig(config)
                .build();
    }

    @Test
    @DisplayName("Test findAll - Trả về danh sách template")
    void findAll_success() {
        // Given
        // Ra lệnh: Khi repo.findAll() được gọi -> trả về 1 list chứa template mẫu
        when(templateRepository.findAll()).thenReturn(List.of(template));
        // Ra lệnh: Khi mapper.toDtoRes() được gọi với template -> trả về templateResponse
        when(templateMapper.toDtoRes(template)).thenReturn(templateResponse);

        // When
        List<TemplateResponse> actualList = templateService.findAll();

        // Then
        assertThat(actualList).isNotNull();
        assertThat(actualList).hasSize(1);
        assertThat(actualList.get(0).getName()).isEqualTo("Modern CV");
    }

    @Test
    @DisplayName("Test findById - Trường hợp thành công")
    void findById_success() {
        // Given
        Long templateId = 1L;
        // Ra lệnh: Khi repo.findById(1L) -> trả về Optional chứa template
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        // Ra lệnh: Khi mapper.toDtoRes(template) -> trả về templateResponse
        when(templateMapper.toDtoRes(template)).thenReturn(templateResponse);

        // When
        TemplateResponse actualResponse = templateService.findById(templateId);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(templateId);
    }

    @Test
    @DisplayName("Test findById - Lỗi TEMPLATE_NOT_FOUND")
    void findById_notFound() {
        // Given
        Long nonExistentId = 99L;
        // Ra lệnh: Khi repo.findById(99L) -> trả về Optional rỗng
        when(templateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When Then
        assertThatThrownBy(() -> templateService.findById(nonExistentId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEMPLATE_NOT_FOUND);
    }

    @Test
    @DisplayName("Test createTemplate - Trường hợp thành công")
    void createTemplate_success() {
        // Given
        // Ra lệnh: Khi mapper.toEntity(request) -> trả về template (chưa có ID)
        when(templateMapper.toEntity(any(TemplateRequest.class))).thenReturn(template);
        // Ra lệnh: Khi repo.save(template) -> trả về template (đã có ID)
        when(templateRepository.save(any(Template.class))).thenReturn(template);
        // Ra lệnh: Khi mapper.toDtoRes(template đã lưu) -> trả về response
        when(templateMapper.toDtoRes(any(Template.class))).thenReturn(templateResponse);

        // When
        TemplateResponse actualResponse = templateService.createTemplate(templateRequest);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isEqualTo(1L);
        assertThat(actualResponse.getName()).isEqualTo("Modern CV");
    }

    @Test
    @DisplayName("Test getTemplateById - Trường hợp thành công (hàm helper)")
    void getTemplateById_success() {
        // Given
        Long templateId = 1L;
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // When
        Template actualTemplate = templateService.getTemplateById(templateId);

        // Then
        assertThat(actualTemplate).isNotNull();
        assertThat(actualTemplate.getId()).isEqualTo(templateId);
    }

    @Test
    @DisplayName("Test getTemplateById - Lỗi TEMPLATE_NOT_FOUND (hàm helper)")
    void getTemplateById_notFound() {
        // Given
        Long nonExistentId = 99L;
        when(templateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When  Then
        assertThatThrownBy(() -> templateService.getTemplateById(nonExistentId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEMPLATE_NOT_FOUND);
    }
}