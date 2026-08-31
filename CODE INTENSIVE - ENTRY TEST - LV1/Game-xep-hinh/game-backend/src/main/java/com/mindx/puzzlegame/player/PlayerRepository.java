package com.mindx.puzzlegame.player;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByRecoveryCodeHash(String recoveryCodeHash);
}
