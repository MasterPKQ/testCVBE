package com.example.identity.service;

import com.example.identity.dto.response.StatsResponse;
import com.example.identity.dto.response.StatsResponse.UserGrowth;
import com.example.identity.repository.ProfileRepository;
import com.example.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public StatsResponse getStats() {
        // Tổng người dùng và profile
        long totalUsers = userRepository.count();
        long totalProfiles = profileRepository.count();
        long pageViews = 9; // demo tạm, sau này có thể tracking thật

        // Tăng trưởng người dùng 6 tháng gần đây
        List<UserGrowth> userGrowth = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int year = date.getYear();
            int month = date.getMonthValue();

            long count = 0;
            try {
                count = userRepository.countByCreatedAtMonth(year, month);
            } catch (Exception e) {
                // Nếu query lỗi, để count = 0
                System.err.println("❌ Lỗi count user tháng " + month + ": " + e.getMessage());
            }

            userGrowth.add(new UserGrowth("Th" + month, count));
        }

        // Trả về StatsResponse
//        return StatsResponse.builder()
//                .totalUsers(totalUsers)
//                .totalProfiles(totalProfiles)
//                .pageViews(pageViews)
//                .userGrowth(userGrowth)
//                .build();
        return new StatsResponse(totalUsers, totalProfiles, pageViews, userGrowth);
    }
}
