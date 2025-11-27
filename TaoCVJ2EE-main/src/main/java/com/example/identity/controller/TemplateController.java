package com.example.identity.controller;

import com.example.identity.dto.TemplateFilterDTO;
import com.example.identity.dto.request.ApiResponse;
import com.example.identity.dto.request.TemplateRequest;
import com.example.identity.dto.response.TemplateResponse;
import com.example.identity.entity.Template;
import com.example.identity.service.TemplateService;
import com.example.identity.service.TemplateRenderingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemplateController {
    TemplateService templateService;
    TemplateRenderingService templateRenderingService;

//    GET    /api/templates                    // Lấy danh sách templates
    @GetMapping()
    public ApiResponse<List<TemplateResponse>> findAll() {
        List<TemplateResponse> list = templateService.findAll();
        return ApiResponse.<List<TemplateResponse>>builder()
                .message("Success")
                .result(list)
                .build();
    }
//    GET    /api/templates/{id}               // Chi tiết template
    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> findById(@PathVariable Long id) {
        TemplateResponse templateResponse = templateService.findById(id);
        return ApiResponse.<TemplateResponse>builder()
                .message("Success")
                .result(templateResponse)
                .build();
    }
//    POST    /api/templates      // Create
    @PostMapping
    public ApiResponse<TemplateResponse> createTemplate(@RequestBody TemplateRequest templateRequest) {
        //bên FE cần gửi ảnh vào API /upload để lấy url truyền vào "thumbnailUrl"
        TemplateResponse templateResponse = templateService.createTemplate(templateRequest);
        return ApiResponse.<TemplateResponse>builder()
                .message("Success")
                .result(templateResponse)
                .build();
    }

    @PostMapping("/{id}/edit")
    public ApiResponse<TemplateResponse> editTemplate(@PathVariable Long id, @RequestBody TemplateRequest templateRequest) {
        TemplateResponse templateResponse = templateService.editTemplate(id, templateRequest);
        return ApiResponse.<TemplateResponse>builder()
                .message("Success")
                .result(templateResponse)
                .build();
    }
//    GET    /api/templates/filter?category=IT&style=modern&...        // Filter
    @GetMapping("/filter")
    public ApiResponse<List<TemplateResponse>> filter(TemplateFilterDTO filter) {
        List<TemplateResponse> templates = templateService.filter(filter);
        return ApiResponse.<List<TemplateResponse>>builder()
                .message("Success")
                .result(templates)
                .build();
    }

    /**
     * Preview template với sample data
     * GET /api/templates/{id}/preview
     * Returns HTML preview
     */
    @GetMapping(value = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewTemplate(@PathVariable Long id) {
        log.info("Previewing template: {}", id);
        Template template = templateService.getTemplateById(id);
        String html = templateRenderingService.renderTemplatePreview(template);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Clear cache for a specific template (Admin endpoint)
     * DELETE /api/templates/{id}/cache
     */
    @DeleteMapping("/{id}/cache")
    public ApiResponse<String> clearTemplateCache(@PathVariable Long id) {
        log.info("Clearing cache for template: {}", id);
        templateService.clearCache(id);
        return ApiResponse.<String>builder()
                .message("Cache cleared successfully for template: " + id)
                .build();
    }

    /**
     * Clear all templates cache (Admin endpoint)
     * DELETE /api/templates/cache/all
     */
    @DeleteMapping("/cache/all")
    public ApiResponse<String> clearAllTemplatesCache() {
        log.info("Clearing all templates cache");
        templateService.clearAllCache();
        return ApiResponse.<String>builder()
                .message("All templates cache cleared successfully")
                .build();
    }
}
