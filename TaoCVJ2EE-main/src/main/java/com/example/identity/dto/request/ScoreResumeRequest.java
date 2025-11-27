package com.example.identity.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreResumeRequest {
    private JobInfo job;
    private ResumeData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobInfo {
        private String title;
        private String company;
        private String description;
        private Duration duration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Duration {
        private String start;
        private String end;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumeData {
        private String name;
        private ContactInfo contactInfo;
        private List<String> highlights;
        private List<Education> education;
        private List<Experience> experiences;
        private List<Project> projects;
        private List<Experience> extraCurriculars;
        private List<Skill> skills;
        private List<Award> awards;
        private List<String> hobbies;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInfo {
        private Location address;
        private List<MediaProfile> mediaProfiles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaProfile {
        private String platform;
        private String handle;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Location {
        private String city;
        private String country;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Education {
        private String qualification;
        private String institution;
        private Duration duration;
        private Location location;
        private List<String> description;
        private List<String> courses;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Experience {
        private String title;
        private String company;
        private List<String> description;
        private Duration duration;
        private Location location;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Project {
        private String title;
        private List<String> description;
        private Duration duration;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Skill {
        private String name;
        private String type;
        private String proficiencyLevel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Award {
        private String title;
        private Duration date;
        private String affilatedTo;
    }
}

