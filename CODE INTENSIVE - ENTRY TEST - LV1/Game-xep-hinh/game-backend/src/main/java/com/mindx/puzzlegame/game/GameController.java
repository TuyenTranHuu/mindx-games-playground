package com.mindx.puzzlegame.game;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    GameDtos.GameResponse start(Authentication authentication) {
        return gameService.start((UUID) authentication.getPrincipal());
    }

    @PostMapping("/{id}/finish")
    GameDtos.GameResponse finish(Authentication authentication, @PathVariable UUID id,
                                 @Valid @RequestBody GameDtos.FinishRequest request) {
        return gameService.finish((UUID) authentication.getPrincipal(), id, request);
    }

    @GetMapping("/history")
    GameDtos.HistoryResponse history(Authentication authentication,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return gameService.history((UUID) authentication.getPrincipal(), page, size);
    }
}
