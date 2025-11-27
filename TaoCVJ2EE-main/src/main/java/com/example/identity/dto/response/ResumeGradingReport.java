package com.example.identity.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeGradingReport {
    private int score;
    private List<String> pros;
    private List<String> cons;
    private List<String> improvements;
}

