package com.example.identity.controller;

import com.example.identity.dto.request.ApiResponse;
import com.example.identity.entity.Profile;
import com.example.identity.service.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {
    ProfileService profileService;

    @GetMapping
    public ApiResponse<Profile> get() {
        return ApiResponse.<Profile>builder()
                .message("Thành công")
                .result(profileService.getProfile())
                .build();
    }

    @PostMapping
    public ApiResponse<Profile> create(@RequestBody Profile profile) {
        return ApiResponse.<Profile>builder()
                .message("Thành công")
                .result(profileService.create(profile))
                .build();
    }

    @PatchMapping
    public ApiResponse<Profile> update(@RequestBody Profile profile) {
        return ApiResponse.<Profile>builder()
                .message("Thành công")
                .result(profileService.update(profile))
                .build();
    }
}
