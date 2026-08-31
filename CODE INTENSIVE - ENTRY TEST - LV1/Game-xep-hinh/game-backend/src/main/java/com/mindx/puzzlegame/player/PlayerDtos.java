package com.mindx.puzzlegame.player;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class PlayerDtos {
    private PlayerDtos() {}

    public record AnonymousRequest(String deviceToken) {}
    public record TokenRequest(@NotBlank(message = "Thiếu device token") String deviceToken) {}
    public record RecoverRequest(@NotBlank(message = "Thiếu mã khôi phục") String recoveryCode) {}
    public record NicknameRequest(
            @NotBlank(message = "Biệt danh không được để trống")
            @Size(min = 3, max = 30, message = "Biệt danh phải dài từ 3 đến 30 ký tự")
            String nickname) {}

    public record AuthResponse(
            UUID playerId,
            String nickname,
            String accessToken,
            String deviceToken,
            String recoveryCode,
            boolean newPlayer) {}

    public record PlayerResponse(UUID playerId, String nickname, String status) {}
}
