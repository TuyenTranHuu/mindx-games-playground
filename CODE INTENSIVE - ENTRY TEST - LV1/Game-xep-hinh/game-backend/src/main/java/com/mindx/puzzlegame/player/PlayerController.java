package com.mindx.puzzlegame.player;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/anonymous")
    PlayerDtos.AuthResponse anonymous(@RequestBody(required = false) PlayerDtos.AnonymousRequest body,
                                      HttpServletRequest request) {
        return playerService.anonymous(body == null ? null : body.deviceToken(), request);
    }

    @PostMapping("/token")
    PlayerDtos.AuthResponse token(@Valid @RequestBody PlayerDtos.TokenRequest body,
                                  HttpServletRequest request) {
        return playerService.authenticateDevice(body.deviceToken(), request);
    }

    @PostMapping("/recover")
    PlayerDtos.AuthResponse recover(@Valid @RequestBody PlayerDtos.RecoverRequest body,
                                    HttpServletRequest request) {
        return playerService.recover(body.recoveryCode(), request);
    }

    @GetMapping("/me")
    PlayerDtos.PlayerResponse me(Authentication authentication) {
        return playerService.getMe((UUID) authentication.getPrincipal());
    }

    @PatchMapping("/me")
    PlayerDtos.PlayerResponse updateMe(Authentication authentication,
                                       @Valid @RequestBody PlayerDtos.NicknameRequest body) {
        return playerService.changeNickname((UUID) authentication.getPrincipal(), body.nickname());
    }
}
