package com.example.identity.configuration;

import com.example.identity.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -9); // trừ 9 tiếng
        Date threshold = cal.getTime();

        invalidatedTokenRepository.deleteExpiredTokens(threshold);
        log.info(" Dọn dẹp token có expirationTime + 9h < now (trước {}): {}", threshold, new Date());
    }
}