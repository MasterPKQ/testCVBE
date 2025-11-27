package com.example.identity.service;

import com.example.identity.dto.request.CVSectionRequestReorder;
import com.example.identity.entity.CV;
import com.example.identity.entity.CVSection;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.mapper.CVSectionMapper;
import com.example.identity.repository.CVRepository;
import com.example.identity.repository.CVSectionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CVSectionServiceTest {

    @Mock
    private CVSectionRepository cvSectionRepository;
    @Mock
    private CVSectionMapper cvSectionMapper;
    @Mock
    private CVRepository cvRepository;


    @InjectMocks
    private CVSectionService cvSectionService;

    // Dữ liệu mẫu
    private final Long cvId = 1L;
    private final Long section1Id = 10L;
    private final Long section2Id = 11L;
    private CV cv;
    private CVSection section1;
    private CVSection section2;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode section1Data = objectMapper.createObjectNode().put("name", "John Doe");
        JsonNode section2Data = objectMapper.createObjectNode().put("title", "Engineer");

        cv = new CV();
        cv.setId(cvId);

        section1 = CVSection.builder()
                .id(section1Id)
                .cv(cv)
                .sectionType("personal_info")
                .sectionData(section1Data)
                .orderIndex(1)
                .isVisible(true)
                .build();

        section2 = CVSection.builder()
                .id(section2Id)
                .cv(cv)
                .sectionType("experience")
                .sectionData(section2Data)
                .orderIndex(2)
                .isVisible(true)
                .build();

        cv.setSections(new ArrayList<>(List.of(section1, section2)));
    }

    @Test
    @DisplayName("Test getListSectionByCVIdSorted - Lấy danh sách section đã sắp xếp")
    void getListSectionByCVIdSorted_success() {
        // Given
        // Ra lệnh: Khi repo.findAll... được gọi -> trả về list (ĐÃ SỬA THÀNH MUTABLE)
        when(cvSectionRepository.findAllByCvId(cvId))
                .thenReturn(new ArrayList<>(List.of(section2, section1)));

        // When
        List<CVSection> result = cvSectionService.getListSectionByCVIdSorted(cvId); //

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(section1Id);
        assertThat(result.get(1).getId()).isEqualTo(section2Id);
    }

    @Test
    @DisplayName("Test reorderCVSection - Sắp xếp lại thành công")
    void reorderCVSection_success() {
        // Given
        // Ra lệnh: Khi repo.findById(cvId) -> trả về CV
        when(cvRepository.findById(cvId)).thenReturn(Optional.of(cv));

        // Yêu cầu sắp xếp: section 1 -> index 2, section 2 -> index 1
        List<CVSectionRequestReorder> reorderRequest = List.of(
                new CVSectionRequestReorder(section1Id, 2),
                new CVSectionRequestReorder(section2Id, 1)
        );

        // When
        List<CVSection> result = cvSectionService.reorderCVSection(cvId, reorderRequest);

        // Then
        // Kiểm tra xem hàm saveAll đã được gọi
        verify(cvSectionRepository).saveAll(any(List.class));

        // Kiểm tra xem index của các object thật đã bị thay đổi
        assertThat(section1.getOrderIndex()).isEqualTo(2);
        assertThat(section2.getOrderIndex()).isEqualTo(1);

        // Kiểm tra xem list trả về đã được sắp xếp
        assertThat(result.get(0).getId()).isEqualTo(section2Id);
        assertThat(result.get(1).getId()).isEqualTo(section1Id);
    }

    @Test
    @DisplayName("Test reorderCVSection - Lỗi CV_NOT_FOUND")
    void reorderCVSection_cvNotFound() {
        // Given
        when(cvRepository.findById(cvId)).thenReturn(Optional.empty());
        List<CVSectionRequestReorder> reorderRequest = List.of();

        // When  Then
        assertThatThrownBy(() -> cvSectionService.reorderCVSection(cvId, reorderRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CV_NOT_FOUND);
    }

    @Test
    @DisplayName("Test createCVSection - Tạo section thành công")
    void createCVSection_success() {
        // Given
        when(cvRepository.findById(cvId)).thenReturn(Optional.of(cv));

        CVSection newSection = CVSection.builder().sectionType("education").build();
        CVSection savedSection = CVSection.builder().id(12L).sectionType("education").cv(cv).build();

        // Ra lệnh: Khi repo.save -> trả về section đã có ID
        when(cvSectionRepository.save(any(CVSection.class))).thenReturn(savedSection);

        // When
        CVSection result = cvSectionService.createCVSection(cvId, newSection);

        // Then
        assertThat(result.getId()).isEqualTo(12L);

        ArgumentCaptor<CVSection> captor = ArgumentCaptor.forClass(CVSection.class);
        verify(cvSectionRepository).save(captor.capture());

        assertThat(captor.getValue().getCv().getId()).isEqualTo(cvId);
    }

    @Test
    @DisplayName("Test updateCVSection - Cập nhật thành công")
    void updateCVSection_success() {
        // Given
        when(cvSectionRepository.findById(section1Id)).thenReturn(Optional.of(section1));
        when(cvSectionRepository.save(any(CVSection.class))).thenReturn(section1);

        JsonNode updatedData = new ObjectMapper().createObjectNode().put("name", "Jane Doe");
        CVSection updateRequest = CVSection.builder()
                .sectionData(updatedData)
                .isVisible(false)
                .build();

        // When
        CVSection result = cvSectionService.updateCVSection(section1Id, updateRequest);

        // Then
        verify(cvSectionMapper).updateSection(section1, updateRequest);
        verify(cvSectionRepository).save(section1);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Test updateCVSection - Lỗi SECTION_NOT_FOUND")
    void updateCVSection_notFound() {
        // Given
        Long nonExistentId = 99L;
        CVSection updateRequest = CVSection.builder().build();

        when(cvSectionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When  Then
        assertThatThrownBy(() -> cvSectionService.updateCVSection(nonExistentId, updateRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SECTION_NOT_FOUND);
    }

    @Test
    @DisplayName("Test deleteCVSection - Xóa thành công")
    void deleteCVSection_success() {
        // Given
        doNothing().when(cvSectionRepository).deleteById(section1Id);

        // When
        Boolean result = cvSectionService.deleteCVSection(section1Id);

        // Then
        assertThat(result).isTrue();
        verify(cvSectionRepository).deleteById(section1Id);
    }
}