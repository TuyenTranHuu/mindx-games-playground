package com.mindx.puzzlegame.player;

import com.mindx.puzzlegame.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecoveryAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MINUTES = 15;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt != null && attempt.expiresAt().isAfter(Instant.now()) && attempt.count() >= MAX_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút.");
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAt().isBefore(now)) {
                return new Attempt(1, now.plus(WINDOW_MINUTES, ChronoUnit.MINUTES));
            }
            return new Attempt(current.count() + 1, current.expiresAt());
        });
    }

    public void clear(String key) { attempts.remove(key); }

    private record Attempt(int count, Instant expiresAt) {}
}
