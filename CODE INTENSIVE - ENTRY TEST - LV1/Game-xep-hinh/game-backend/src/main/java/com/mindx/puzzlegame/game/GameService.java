package com.mindx.puzzlegame.game;

import com.mindx.puzzlegame.common.ApiException;
import com.mindx.puzzlegame.player.Player;
import com.mindx.puzzlegame.player.PlayerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    private final GameSessionRepository gameRepository;
    private final PlayerService playerService;
    private final BoardValidationService boardValidation;

    public GameService(GameSessionRepository gameRepository, PlayerService playerService,
                       BoardValidationService boardValidation) {
        this.gameRepository = gameRepository;
        this.playerService = playerService;
        this.boardValidation = boardValidation;
    }

    @Transactional
    public GameDtos.GameResponse start(UUID playerId) {
        List<GameSession> savedGames = gameRepository.findByPlayerIdAndStatusIn(
                playerId, List.of(GameStatus.SAVED));
        if (!savedGames.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Bạn đang có một ván chưa kết thúc");
        }
        Instant now = Instant.now();
        gameRepository.findByPlayerIdAndStatusIn(playerId, List.of(GameStatus.PLAYING))
                .forEach(game -> game.abandon(now));
        Player player = playerService.requirePlayer(playerId);
        GameSession session = new GameSession(UUID.randomUUID(), player,
                boardValidation.shuffledBoard(), now);
        return toResponse(gameRepository.save(session));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public GameDtos.GameResponse finish(UUID playerId, UUID gameId, GameDtos.FinishRequest request) {
        GameSession session = gameRepository.findByIdAndPlayerId(gameId, playerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy lượt chơi"));
        if (session.getStatus() != GameStatus.PLAYING) {
            throw new ApiException(HttpStatus.CONFLICT, "Lượt chơi không còn ở trạng thái đang chơi");
        }

        boolean requestedWin = "won".equalsIgnoreCase(request.result());
        boolean requestedEnd = "ended".equalsIgnoreCase(request.result());
        if (!requestedWin && !requestedEnd) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Kết quả chỉ nhận won hoặc ended");
        }

        try {
            List<Integer> finalBoard = boardValidation.replay(session.getInitialBoard(), request.moves());
            if (requestedWin && !boardValidation.isSolved(finalBoard)) {
                session.invalidate(Instant.now());
                throw new ApiException(HttpStatus.BAD_REQUEST, "Bàn cờ cuối chưa ở trạng thái chiến thắng");
            }
            session.finish(finalBoard, request.moves(), requestedWin, Instant.now());
            return toResponse(session);
        } catch (ApiException exception) {
            session.invalidate(Instant.now());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public GameDtos.HistoryResponse history(UUID playerId, int page, int size) {
        playerService.requirePlayer(playerId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        Page<GameSession> result = gameRepository.findByPlayerIdOrderByCreatedAtDesc(
                playerId, PageRequest.of(safePage, safeSize));
        return new GameDtos.HistoryResponse(result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private GameDtos.GameResponse toResponse(GameSession session) {
        return new GameDtos.GameResponse(session.getId(), session.getCurrentBoard(),
                session.getMoveCount(), session.getActiveElapsedSeconds(), session.getStatus().name(),
                session.getValidationStatus().name(), session.getStartedAt(), session.getFinishedAt());
    }
}
