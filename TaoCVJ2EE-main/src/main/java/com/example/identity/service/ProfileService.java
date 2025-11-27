package com.example.identity.service;

import com.example.identity.entity.Profile;
import com.example.identity.repository.ProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    ProfileRepository profileRepository;
    UserService userService;

    public Profile getProfile() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        return profileRepository.findProfileByUser_Username(username)
                .orElseGet(() -> create(new Profile()));
    }


    public Profile create(Profile profile) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        profile.setUser(userService.getUserByUsername(name));
        return profileRepository.save(profile);
    }

//    public Profile update(Profile profile) {
//        Profile profile1 = getProfile(); // lấy profile hiện tại
//
//        if (profile.getName() != null) {
//            profile1.setName(profile.getName());
//        }
//
//        if (profile.getContactInfo() != null) {
//
//            if (profile1.getContactInfo() == null) {
//                profile1.setContactInfo(new ContactInfo());
//            }
//            ContactInfo incoming = profile.getContactInfo();
//            ContactInfo existing = profile1.getContactInfo();
//
//            // update address
//            if (incoming.getAddress() != null) {
//                existing.setAddress(incoming.getAddress());
//            }
//
//            // update mediaProfiles
//            if (incoming.getMediaProfiles() != null) {
//                existing.setMediaProfiles(incoming.getMediaProfiles());
//            }
//        }
//
//        // Update lists: nếu incoming != null thì replace, nếu null thì giữ nguyên
//        if (profile.getEducations() != null) {
//            profile1.setEducations(profile.getEducations());
//        }
//
//        if (profile.getExperiences() != null) {
//            profile1.setExperiences(profile.getExperiences());
//        }
//
//        if (profile.getProjects() != null) {
//            profile1.setProjects(profile.getProjects());
//        }
//
//        if (profile.getExtraCurriculars() != null) {
//            profile1.setExtraCurriculars(profile.getExtraCurriculars());
//        }
//
//        if (profile.getOtherSkills() != null) {
//            profile1.setOtherSkills(profile.getOtherSkills());
//        }
//
//        if (profile.getOtherAwards() != null) {
//            profile1.setOtherAwards(profile.getOtherAwards());
//        }
//
//        if (profile.getHobbies() != null) {
//            profile1.setHobbies(profile.getHobbies());
//        }
//
//        return profileRepository.save(profile1);
//    }

    public Profile update(Profile incoming) {
        Profile existing = getProfile();

        // Update simple fields
        if (incoming.getName() != null) {
            existing.setName(incoming.getName());
        }

        // Update embedded ContactInfo
        if (incoming.getContactInfo() != null) {
            if (existing.getContactInfo() == null) {
                existing.setContactInfo(incoming.getContactInfo());
            } else {
                if (incoming.getContactInfo().getAddress() != null) {
                    existing.getContactInfo().setAddress(incoming.getContactInfo().getAddress());
                }
                if (incoming.getContactInfo().getMediaProfiles() != null) {
                    if (existing.getContactInfo().getMediaProfiles() == null) {
                        existing.getContactInfo().setMediaProfiles(new ArrayList<>());
                    } else {
                        existing.getContactInfo().getMediaProfiles().clear();
                    }
                    existing.getContactInfo().getMediaProfiles().addAll(incoming.getContactInfo().getMediaProfiles());
                }
            }
        }

        // Update list fields
        mergeList(existing.getEducations(), incoming.getEducations());
        mergeList(existing.getExperiences(), incoming.getExperiences());
        mergeList(existing.getProjects(), incoming.getProjects());
        mergeList(existing.getExtraCurriculars(), incoming.getExtraCurriculars());
        mergeList(existing.getOtherSkills(), incoming.getOtherSkills());
        mergeList(existing.getOtherAwards(), incoming.getOtherAwards());

        // Update hobbies
        if (incoming.getHobbies() != null) {
            if (existing.getHobbies() == null) {
                existing.setHobbies(new ArrayList<>());
            } else {
                existing.getHobbies().clear();
            }
            existing.getHobbies().addAll(incoming.getHobbies());
        }

        return profileRepository.save(existing);
    }

    // Hàm merge list tránh orphanRemoval
    private <T> void mergeList(List<T> existingList, List<T> incomingList) {
        if (incomingList != null) {
            if (existingList == null) {
                existingList = new ArrayList<>();
            } else {
                existingList.clear();
            }
            existingList.addAll(incomingList);
        }
    }
}
