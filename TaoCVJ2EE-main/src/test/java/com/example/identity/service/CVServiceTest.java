package com.example.identity.service;

import com.example.identity.dto.request.CVRequest;
import com.example.identity.dto.response.CVResponse;
import com.example.identity.entity.CV;
import com.example.identity.entity.Template;
import com.example.identity.entity.User;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.CVMapper;
import com.example.identity.repository.CVRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CVServiceTest {

    @Mock
    private CVRepository cvRepository;
    @Mock
    private CVMapper cvMapper;
    @Mock
    private UserService userService;
    @Mock
    private TemplateService templateService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;


    @InjectMocks
    private CVService cvService;

    // Dữ liệu mẫu
    private final String testUsername = "testuser";
    private final Long cvId = 1L;
    private final Long templateId = 10L;
    private User user;
    private Template template;
    private CV cv;
    private CVRequest cvRequest;
    private CVResponse cvResponse;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode cvData = objectMapper.createObjectNode().put("title", "Developer");

        user = User.builder().id("user-123").username(testUsername).build();

        template = Template.builder().id(templateId).name("Test Template").build();

        cvRequest = CVRequest.builder()
                .name("Test CV")
                .templateId(templateId)
                .cvData(cvData)
                .build();

        cv = CV.builder()
                .id(cvId)
                .user(user)
                .template(template)
                .name("Test CV")
                .cvData(cvData)
                .createdAt(LocalDateTime.now())
                .build();


        cvResponse = CVResponse.builder()
                .id(cvId)
                .name("Test CV")
                .templateId(templateId)
                .cvData(cvData)
                .build();

    }


    @Test
    @DisplayName("Test getAllMyCV - Lấy tất cả CV thành công")
    void getAllMyCV_success() {

        try (MockedStatic<SecurityContextHolder> mockedContext = Mockito.mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUsername);

            // Ra lệnh: Khi repo tìm CV theo username -> trả về 1 list
            when(cvRepository.findAllByUserUsername(testUsername)).thenReturn(List.of(cv));
            // Ra lệnh: Khi mapper chuyển đổi -> trả về response
            when(cvMapper.toCVResponse(cv)).thenReturn(cvResponse);

            // When
            List<CVResponse> result = cvService.getAllMyCV();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(cvId);
        }
    }

    @Test
    @DisplayName("Test getCVById - Lấy CV thành công")
    void getCVById_success() {

        try (MockedStatic<SecurityContextHolder> mockedContext = Mockito.mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUsername);

            when(cvRepository.findAllByUserUsername(testUsername)).thenReturn(List.of(cv));
            when(cvMapper.toCVResponse(cv)).thenReturn(cvResponse);

            // When
            CVResponse result = cvService.getCVById(cvId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(cvId);
        }
    }

    @Test
    @DisplayName("Test getCVById - Lỗi CV_NOT_FOUND")
    void getCVById_notFound() {

        try (MockedStatic<SecurityContextHolder> mockedContext = Mockito.mockStatic(SecurityContextHolder.class)) {
            // Given
            Long nonExistentId = 99L;
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUsername);

            when(cvRepository.findAllByUserUsername(testUsername)).thenReturn(List.of(cv));
            when(cvMapper.toCVResponse(cv)).thenReturn(cvResponse);

            // When & Then
            assertThatThrownBy(() -> cvService.getCVById(nonExistentId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CV_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("Test createCV - Tạo CV thành công")
    void createCV_success() {

        try (MockedStatic<SecurityContextHolder> mockedContext = Mockito.mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUsername);

            when(cvMapper.toCV(cvRequest)).thenReturn(cv);
            when(userService.getUserByUsername(testUsername)).thenReturn(user);
            when(templateService.getTemplateById(templateId)).thenReturn(template);
            when(cvRepository.save(any(CV.class))).thenReturn(cv);
            when(cvMapper.toCVResponse(cv)).thenReturn(cvResponse);

            // When
            CVResponse result = cvService.createCV(cvRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(cvId);

            ArgumentCaptor<CV> cvCaptor = ArgumentCaptor.forClass(CV.class);
            verify(cvRepository).save(cvCaptor.capture());

            CV savedCV = cvCaptor.getValue();
            assertThat(savedCV.getUser().getUsername()).isEqualTo(testUsername);
            assertThat(savedCV.getTemplate().getId()).isEqualTo(templateId);
        }
    }

    @Test
    @DisplayName("Test updateCV - Cập nhật CV thành công")
    void updateCV_success() {

        // Given
        when(cvRepository.findById(cvId)).thenReturn(Optional.of(cv));
        when(cvRepository.save(any(CV.class))).thenReturn(cv);
        when(cvMapper.toCVResponse(cv)).thenReturn(cvResponse);

        // When
        CVResponse result = cvService.updateCV(cvId, cvRequest);

        // Then
        assertThat(result).isNotNull();
        verify(cvMapper).updateCV(cv, cvRequest);
    }

    @Test
    @DisplayName("Test deleteCV - Xóa CV thành công")
    void deleteCV_success() {
        // Given

        // When
        Boolean result = cvService.deleteCV(cvId); //

        // Then
        assertThat(result).isTrue();
        verify(cvRepository).deleteById(cvId);
    }

    @Test
    @DisplayName("Test duplicateCV - Nhân bản CV thành công")
    void duplicateCV_success() {
        // Given
        CV newCV = new CV();
        CVResponse newCVResponse = CVResponse.builder().id(2L).name("Test CV").build();

        when(cvRepository.findById(cvId)).thenReturn(Optional.of(cv));
        when(cvRepository.save(any(CV.class))).thenReturn(newCV);
        when(cvMapper.toCVResponse(newCV)).thenReturn(newCVResponse);

        // When
        CVResponse result = cvService.duplicateCV(cvId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        verify(cvMapper).duplicateCV(any(CV.class), eq(cv));
    }
}