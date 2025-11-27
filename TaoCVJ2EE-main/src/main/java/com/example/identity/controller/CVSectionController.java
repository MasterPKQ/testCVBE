package com.example.identity.controller;

import com.example.identity.dto.request.ApiResponse;
import com.example.identity.dto.request.CVSectionRequestReorder;
import com.example.identity.entity.CVSection;
import com.example.identity.service.CVSectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("cvs/sections")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CVSectionController {
    CVSectionService cvSectionService;

// Lấy danh sách section
    @GetMapping("/{cvId}")
    public ApiResponse<List<CVSection>> getSection(@PathVariable Long cvId) {
        return ApiResponse.<List<CVSection>>builder()
                .result(cvSectionService.getListSectionByCVIdSorted(cvId))
                .message("Thành công")
                .build();
    }
// Thay đổi thứ tự sections
    @PutMapping("/reorder/{cvId}")
    public ApiResponse<List<CVSection>> reorderSection(@PathVariable Long cvId,@RequestBody List<CVSectionRequestReorder> cvSectionRequestReorders) {
        return ApiResponse.<List<CVSection>>builder()
                .result(cvSectionService.reorderCVSection(cvId, cvSectionRequestReorders))
                .message("Thành công")
                .build();
    }

// Update section
    @PutMapping("/{sectionId}")
    public ApiResponse<CVSection> updateCVSection(@PathVariable Long sectionId, @RequestBody CVSection cvSection) {
        return ApiResponse.<CVSection>builder()
                .result(cvSectionService.updateCVSection(sectionId, cvSection))
                .message("Thành công")
                .build();
    }
// Thêm section
    @PostMapping("/{cvId}")
    public ApiResponse<CVSection> createCVSection(@PathVariable Long cvId, @RequestBody CVSection cvSection) {
        return ApiResponse.<CVSection>builder()
                .result(cvSectionService.createCVSection(cvId, cvSection))
                .message("Thành công")
                .build();
    }
// Xóa section
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteCVSection(@PathVariable Long id) {
        return ApiResponse.<Boolean>builder()
                .result(cvSectionService.deleteCVSection(id))
                .message("Thành công")
                .build();
    }
}
