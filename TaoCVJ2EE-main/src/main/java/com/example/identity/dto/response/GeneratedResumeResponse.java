package com.example.identity.dto.response;

import com.example.identity.dto.request.GenerateResumeRequest;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedResumeResponse {
    private GenerateResumeRequest.JobInfo job;
    private ResumeData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumeData {
        private String name;
        private List<String> highlights;
        private List<GenerateResumeRequest.Education> education;
        private List<GenerateResumeRequest.ProfileExperience> experiences;
        private List<GenerateResumeRequest.ProfileProject> projects;
        private List<GenerateResumeRequest.ProfileExperience> extraCurriculars;
        private List<GenerateResumeRequest.Skill> skills;
        private List<GenerateResumeRequest.Award> awards;
        private List<String> hobbies;
        private GenerateResumeRequest.ContactInfo contactInfo;
    }
}

