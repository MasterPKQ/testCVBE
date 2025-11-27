package com.example.identity.controller;

import com.example.identity.dto.request.ApiResponse;
import com.example.identity.dto.request.CVRequest;
import com.example.identity.dto.response.CVResponse;
import com.example.identity.service.CVService;
import com.example.identity.service.TemplateRenderingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/cvs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CVController {
    CVService cvService;
    TemplateRenderingService templateRenderingService;

    /*----------------------------------------CV Management----------------------------------------------------------------*/
    //GET /api/cvs                          Danh sách CV của user
    @GetMapping()
    public ApiResponse<List<CVResponse>> getAllMyCV() {
        return ApiResponse.<List<CVResponse>>builder()
                .message("Tải danh sách CV thành công")
                .result(cvService.getAllMyCV())
                .build();
    }

    @PostMapping()
    public ApiResponse<CVResponse> create(@RequestBody CVRequest cvRequest) {
        return ApiResponse.<CVResponse>builder()
                .message("Tạo CV thành công")
                .result(cvService.createCV(cvRequest))
                .build();
    }

    // Xem chi tiết CV
    @GetMapping("/{id}")
    public ApiResponse<CVResponse> getById(@PathVariable Long id) {
        return ApiResponse.<CVResponse>builder()
                .message("Tải CV thành công")
                .result(cvService.getCVById(id))
                .build();
    }

    //Cập nhật CV
    @PostMapping("/{id}")
    public ApiResponse<CVResponse> update(@PathVariable Long id, @RequestBody CVRequest cvRequest) {
        return ApiResponse.<CVResponse>builder()
                .message("Cập nhật CV thành công")
                .result(cvService.updateCV(id, cvRequest))
                .build();
    }

    //POST    /api/cvs/{id}                   Xóa CV
    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Boolean>> delete(@PathVariable Long id) {
        return ApiResponse.<Map<String, Boolean>>builder()
                .message("Xóa CV thành công")
                .result(Map.of("deleted", cvService.deleteCV(id)))
                .build();
    }

    //Nhân đôi CV
    @PostMapping("/{id}/duplicate")
    public ApiResponse<CVResponse> duplicate(@PathVariable Long id) {
        return ApiResponse.<CVResponse>builder()
                .message("Nhân đôi CV thành công")
                .result(cvService.duplicateCV(id))
                .build();
    }






    /*-------------------------------------------------Export & Share CV-------------------------------------------------*/

    /**
     * Render CV as HTML
     * GET /api/cvs/{id}/render
     * Returns complete HTML of CV with template applied
     */
    @GetMapping(value = "/{id}/render", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> renderCV(@PathVariable Long id) {
        log.info("Rendering CV: {}", id);
        String html = cvService.renderCVAsHtml(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

//  POST   /api/cvs/{id}/export/pdf         // Generate PDF
    @PostMapping("/{id}/export/pdf")
    public ResponseEntity<File> exportPdf(@PathVariable Long id) {
        return new ResponseEntity<>(new File(id+".pdf"), HttpStatus.OK);
    }


//  POST   /api/cvs/{id}/export/docx        // Generate DOCX
    @PostMapping("/{id}/export/docx")
    public File exportDocx(@PathVariable Long id) {
        return new File("path");
    }


//  POST   /api/cvs/{id}/share              // Tạo share link
    @PostMapping("/{id}/share")
    public String shareLink(@PathVariable Long id) {
        return "cvbuilder.com/share/7847864762";
    }


//  GET    /api/cvs/{id}/qrcode             // Generate QR code
    @GetMapping("/{id}/qrcode")
    public Object shareQrcode(@PathVariable Long id) {
        return "cvbuilder.com/share/7847864762";
    }


//  GET    /public/cv/{shareToken}          // Public CV view
    @GetMapping("/public/cv/{shareToken}")
    public Object publicCVView(@PathVariable UUID shareToken) {
        return "cvbuilder.com/public/cv/" + shareToken;
    }
}
