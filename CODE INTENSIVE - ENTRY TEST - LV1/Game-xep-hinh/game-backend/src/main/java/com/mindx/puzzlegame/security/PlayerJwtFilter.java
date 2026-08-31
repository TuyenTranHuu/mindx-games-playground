package com.mindx.puzzlegame.security;

import com.mindx.puzzlegame.player.PlayerRepository;
import com.mindx.puzzlegame.player.PlayerStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class PlayerJwtFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final PlayerRepository playerRepository;

    public PlayerJwtFilter(TokenService tokenService, PlayerRepository playerRepository) {
        this.tokenService = tokenService;
        this.playerRepository = playerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                UUID playerId = tokenService.readPlayerId(authorization.substring(7));
                playerRepository.findById(playerId)
                        .filter(player -> player.getStatus() == PlayerStatus.ACTIVE)
                        .ifPresent(player -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        playerId, null, List.of(new SimpleGrantedAuthority("ROLE_PLAYER")))));
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
