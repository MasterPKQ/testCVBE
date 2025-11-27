package com.example.identity.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateResumeRequest {
    private ProfileData profile;
    private JobInfo job;
    private ResumeCreationOptions options;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileData {
        private String name;
        private ContactInfo contactInfo;
        private List<Education> education;
        private List<ProfileExperience> experiences;
        private List<ProfileProject> projects;
        private List<ProfileExperience> extraCurriculars;
        private List<Skill> otherSkills;
        private List<Award> otherAwards;
        private List<String> hobbies;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileExperience {
        private String title;
        private String company;
        private List<String> description;
        private Duration duration;
        private Location location;
        private List<Skill> skills;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileProject {
        private String title;
        private List<String> description;
        private Duration duration;
        private List<Skill> skills;
    }

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
    public static class Duration {
        private String start;
        private String end;
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumeCreationOptions {
        @Builder.Default
        private boolean addHighlights = false;
        @Builder.Default
        private boolean addHobbies = false;
        @Builder.Default
        private boolean addAwards = false;
        @Builder.Default
        private boolean addSkills = true;
        @Builder.Default
        private int minDescriptionLength = 0;
        @Builder.Default
        private int maxDescriptionLength = 5;
        private FilterStrategy filterStrategy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterStrategy {
        @Builder.Default
        private int pages = -1;
        @Builder.Default
        private int minExperiences = -1;
        @Builder.Default
        private int maxExperiences = -1;
        @Builder.Default
        private int minProjects = -1;
        @Builder.Default
        private int maxProjects = -1;
        @Builder.Default
        private int minExtraCurriculars = -1;
        @Builder.Default
        private int maxExtraCurriculars = -1;
    }
}

