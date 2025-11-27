package com.example.identity.controller;

import com.example.identity.dto.request.ApiResponse;
import com.example.identity.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadFile(file);
        return ApiResponse.<Map<String, String>>builder()
                .message("Successfully uploaded")
                .result(Map.of("url", url))
                .build();
    }
}
