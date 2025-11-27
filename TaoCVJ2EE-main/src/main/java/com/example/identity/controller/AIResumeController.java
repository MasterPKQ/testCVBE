package com.example.identity.controller;


import com.example.identity.dto.request.ApiResponse;
import com.example.identity.dto.request.GenerateResumeRequest;
import com.example.identity.dto.request.ScoreResumeRequest;
import com.example.identity.dto.response.GeneratedResumeResponse;
import com.example.identity.dto.response.ResumeGradingReport;
import com.example.identity.service.AIResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai-resume")
@RequiredArgsConstructor
@Slf4j
public class AIResumeController {

    private final AIResumeService aiResumeService;

    /**
     * Endpoint để chấm điểm resume
     * POST /api/ai-resume/score
     */
    @PostMapping("/score")
    public Mono<ApiResponse<ResumeGradingReport>> scoreResume(@RequestBody ScoreResumeRequest request) {
        log.info("Received request to score resume for job: {}", request.getJob().getTitle());

        return aiResumeService.scoreResume(request)
                .map(report -> ApiResponse.<ResumeGradingReport>builder()
                        .code(200)
                        .message("Resume scored successfully")
                        .result(report)
                        .build())
                .onErrorResume(error -> {
                    log.error("Error scoring resume", error);
                    return Mono.just(ApiResponse.<ResumeGradingReport>builder()
                            .code(500)
                            .message("Error scoring resume: " + error.getMessage())
                            .build());
                });
    }

    /**
     * Endpoint để tạo resume tự động
     * POST /api/ai-resume/generate
     */
    @PostMapping("/generate")
    public Mono<ApiResponse<GeneratedResumeResponse>> generateResume(@RequestBody GenerateResumeRequest request) {
        log.info("Received request to generate resume for job: {}", request.getJob().getTitle());

        return aiResumeService.generateResume(request)
                .map(resume -> ApiResponse.<GeneratedResumeResponse>builder()
                        .code(200)
                        .message("Resume generated successfully")
                        .result(resume)
                        .build())
                .onErrorResume(error -> {
                    log.error("Error generating resume", error);
                    return Mono.just(ApiResponse.<GeneratedResumeResponse>builder()
                            .code(500)
                            .message("Error generating resume: " + error.getMessage())
                            .build());
                });
    }

    /**
     * Test endpoint để kiểm tra service đang hoạt động
     * GET /api/ai-resume/health
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.<String>builder()
                .code(200)
                .message("AI Resume Service is running")
                .result("OK")
                .build();
    }
}
