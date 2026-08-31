package com.mindx.puzzlegame.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
    Page<GameSession> findByPlayerIdOrderByCreatedAtDesc(UUID playerId, Pageable pageable);
    Optional<GameSession> findByIdAndPlayerId(UUID id, UUID playerId);
    boolean existsByPlayerIdAndStatusIn(UUID playerId, Collection<GameStatus> statuses);
    List<GameSession> findByPlayerIdAndStatusIn(UUID playerId, Collection<GameStatus> statuses);
}
