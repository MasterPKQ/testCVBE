package com.example.identity.controller;

import com.example.identity.dto.response.StatsResponse;
import com.example.identity.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public StatsResponse getStats() {
        return statsService.getStats();
    }
}
