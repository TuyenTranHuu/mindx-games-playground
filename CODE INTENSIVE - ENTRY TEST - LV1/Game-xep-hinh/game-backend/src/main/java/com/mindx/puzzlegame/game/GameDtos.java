package com.mindx.puzzlegame.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class GameDtos {
    private GameDtos() {}

    public record FinishRequest(
            @NotBlank(message = "Thiếu loại kết thúc") String result,
            @NotNull(message = "Thiếu danh sách bước đi") List<String> moves) {}

    public record GameResponse(
            UUID id,
            List<Integer> board,
            int moveCount,
            int elapsedSeconds,
            String status,
            String validationStatus,
            Instant startedAt,
            Instant finishedAt) {}

    public record HistoryResponse(
            List<GameResponse> items,
            int page,
            int size,
            long totalItems,
            int totalPages) {}
}
