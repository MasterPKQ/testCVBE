package com.example.identity.controller;

import com.example.identity.dto.request.ApiResponse;
import com.example.identity.dto.request.AdminTemplateUploadRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.service.AdminTemplateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminTemplateController {
    
    AdminTemplateService adminTemplateService;

    /**
     * Admin tạo template mới từ HTML builder
     * POST /api/admin/templates/create
     * 
     * Body: {
     *   "name": "Modern IT Resume",
     *   "category": "IT",
     *   "style": "modern",
     *   "thumbnailUrl": "https://...",
     *   "baseHtml": "<html>...</html>",
     *   "sectionsDefinition": {...},
     *   "templateConfig": {...},
     *   "isPremium": false
     * }
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TemplateResponse> createTemplate(
            @RequestBody AdminTemplateUploadRequest request) {
        
        log.info("Admin creating new template: {}", request.getName());
        
        TemplateResponse response = adminTemplateService.createTemplateFromHtml(request);
        
        return ApiResponse.<TemplateResponse>builder()
                .message("Template created successfully")
                .result(response)
                .build();
    }

    /**
     * Admin update template HTML
     * PUT /api/admin/templates/{id}/update-html
     */
    @PutMapping("/{id}/update-html")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TemplateResponse> updateTemplateHtml(
            @PathVariable Long id,
            @RequestBody AdminTemplateUploadRequest request) {
        
        log.info("Admin updating template HTML: {}", id);
        
        TemplateResponse response = adminTemplateService.updateTemplateHtml(id, request);
        
        return ApiResponse.<TemplateResponse>builder()
                .message("Template updated successfully")
                .result(response)
                .build();
    }

    /**
     * Admin test compile template
     * POST /api/admin/templates/test-compile
     */
    @PostMapping("/test-compile")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> testCompile(@RequestBody String html) {
        log.info("Testing HTML to Thymeleaf compilation");
        
        String thymeleafHtml = adminTemplateService.testHtmlToThymeleaf(html);
        
        return ApiResponse.<String>builder()
                .message("Compilation successful")
                .result(thymeleafHtml)
                .build();
    }

    /**
     * Admin xóa template
     * DELETE /api/admin/templates/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        log.info("Admin deleting template: {}", id);
        
        adminTemplateService.deleteTemplate(id);
        
        return ApiResponse.<Void>builder()
                .message("Template deleted successfully")
                .build();
    }

    /**
     * Admin toggle template active status
     * PATCH /api/admin/templates/{id}/toggle-active
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TemplateResponse> toggleActive(@PathVariable Long id) {
        log.info("Admin toggling template active status: {}", id);
        
        TemplateResponse response = adminTemplateService.toggleActiveStatus(id);
        
        return ApiResponse.<TemplateResponse>builder()
                .message("Template status updated")
                .result(response)
                .build();
    }
}
