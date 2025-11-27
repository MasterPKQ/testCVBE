package com.example.identity.service;

import com.example.identity.dto.request.GenerateResumeRequest;
import com.example.identity.dto.request.ScoreResumeRequest;
import com.example.identity.dto.response.GeneratedResumeResponse;
import com.example.identity.dto.response.ResumeGradingReport;
// Import các entity classes - điều chỉnh package path nếu cần
import com.example.identity.entity.Profile;
import com.example.identity.entity.ContactInfo;
import com.example.identity.entity.Location;
import com.example.identity.entity.MediaProfile;
import com.example.identity.entity.Education;
import com.example.identity.entity.Experience;
import com.example.identity.entity.Project;
import com.example.identity.entity.Skill;
import com.example.identity.entity.Award;
import com.example.identity.entity.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIResumeService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ml.worker.base-url:http://localhost:8000}")
    private String mlWorkerBaseUrl;

    /**
     * Gọi endpoint /score_resume/ để chấm điểm resume
     */
    public Mono<ResumeGradingReport> scoreResume(ScoreResumeRequest request) {
        log.info("Calling ML worker to score resume for job: {}", request.getJob().getTitle());

        WebClient webClient = webClientBuilder.baseUrl(mlWorkerBaseUrl).build();

        return webClient.post()
                .uri("/score_resume/")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ResumeGradingReport.class)
                .doOnSuccess(report -> log.info("Resume scored successfully: score={}", report.getScore()))
                .doOnError(error -> log.error("Error scoring resume", error));
    }

    /**
     * Gọi endpoint /generate_resume/ để tạo resume tự động
     */
    public Mono<GeneratedResumeResponse> generateResume(GenerateResumeRequest request) {
        log.info("Calling ML worker to generate resume for job: {}", request.getJob().getTitle());

        WebClient webClient = webClientBuilder.baseUrl(mlWorkerBaseUrl).build();

        return webClient.post()
                .uri("/generate_resume/")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeneratedResumeResponse.class)
                .doOnSuccess(resume -> log.info("Resume generated successfully"))
                .doOnError(error -> log.error("Error generating resume", error));
    }

    /**
     * Helper method để convert Profile entity sang format phù hợp cho ML worker
     */
    public GenerateResumeRequest.ProfileData convertProfileToProfileData(Profile profile) {
        if (profile == null) {
            return null;
        }

        return GenerateResumeRequest.ProfileData.builder()
                .name(profile.getName())
                .contactInfo(convertContactInfo(profile.getContactInfo()))
                .education(convertEducations(profile.getEducations()))
                .experiences(convertExperiences(profile.getExperiences()))
                .projects(convertProjects(profile.getProjects()))
                .extraCurriculars(convertExtraCurriculars(profile.getExtraCurriculars()))
                .otherSkills(convertSkills(profile.getOtherSkills()))
                .otherAwards(convertAwards(profile.getOtherAwards()))
                .hobbies(profile.getHobbies() != null ? profile.getHobbies() : Collections.emptyList())
                .build();
    }

    private GenerateResumeRequest.ContactInfo convertContactInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            return null;
        }

        return GenerateResumeRequest.ContactInfo.builder()
                .address(convertLocation(contactInfo.getAddress()))
                .mediaProfiles(convertMediaProfiles(contactInfo.getMediaProfiles()))
                .build();
    }

    private GenerateResumeRequest.Location convertLocation(Location location) {
        if (location == null) {
            return null;
        }

        return GenerateResumeRequest.Location.builder()
                .city(location.getCity())
                .country(location.getCountry())
                .build();
    }

    private List<GenerateResumeRequest.MediaProfile> convertMediaProfiles(List<MediaProfile> mediaProfiles) {
        if (mediaProfiles == null || mediaProfiles.isEmpty()) {
            return Collections.emptyList();
        }

        return mediaProfiles.stream()
                .map(mp -> GenerateResumeRequest.MediaProfile.builder()
                        .platform(mp.getPlatform())
                        .handle(mp.getHandle())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GenerateResumeRequest.Education> convertEducations(List<Education> educations) {
        if (educations == null || educations.isEmpty()) {
            return Collections.emptyList();
        }

        return educations.stream()
                .map(edu -> GenerateResumeRequest.Education.builder()
                        .qualification(edu.getQualification())
                        .institution(edu.getInstitution())
                        .duration(convertDuration(edu.getDuration()))
                        .location(convertLocation(edu.getLocation()))
                        .description(edu.getDescription() != null ? edu.getDescription() : Collections.emptyList())
                        .courses(edu.getCourses() != null ? edu.getCourses() : Collections.emptyList())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GenerateResumeRequest.ProfileExperience> convertExperiences(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return Collections.emptyList();
        }

        return experiences.stream()
                .map(exp -> GenerateResumeRequest.ProfileExperience.builder()
                        .title(exp.getTitle())
                        .company(exp.getCompany())
                        .description(exp.getDescription() != null ? exp.getDescription() : Collections.emptyList())
                        .duration(convertDuration(exp.getDuration()))
                        .location(convertLocation(exp.getLocation()))
                        .skills(convertSkills(exp.getSkills()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<GenerateResumeRequest.ProfileProject> convertProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return Collections.emptyList();
        }

        return projects.stream()
                .map(proj -> GenerateResumeRequest.ProfileProject.builder()
                        .title(proj.getTitle())
                        .description(proj.getDescription() != null ? proj.getDescription() : Collections.emptyList())
                        .duration(convertDuration(proj.getDuration()))
                        .skills(convertSkills(proj.getSkills()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<GenerateResumeRequest.ProfileExperience> convertExtraCurriculars(List<Experience> extraCurriculars) {
        return convertExperiences(extraCurriculars);
    }

    private List<GenerateResumeRequest.Skill> convertSkills(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return Collections.emptyList();
        }

        return skills.stream()
                .map(skill -> GenerateResumeRequest.Skill.builder()
                        .name(skill.getName())
                        .type(skill.getType())
                        .proficiencyLevel(skill.getProficiencyLevel())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GenerateResumeRequest.Award> convertAwards(List<Award> awards) {
        if (awards == null || awards.isEmpty()) {
            return Collections.emptyList();
        }

        return awards.stream()
                .map(award -> GenerateResumeRequest.Award.builder()
                        .title(award.getTitle())
                        .date(convertDuration(award.getDate()))
                        .affilatedTo(award.getAffilatedTo())
                        .build())
                .collect(Collectors.toList());
    }

    private GenerateResumeRequest.Duration convertDuration(Duration duration) {
        if (duration == null) {
            return null;
        }

        return GenerateResumeRequest.Duration.builder()
                .start(duration.getStart())
                .end(duration.getEnd())
                .build();
    }
}
