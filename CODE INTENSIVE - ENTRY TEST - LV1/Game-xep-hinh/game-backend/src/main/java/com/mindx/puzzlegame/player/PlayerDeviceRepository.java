package com.mindx.puzzlegame.player;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PlayerDeviceRepository extends JpaRepository<PlayerDevice, UUID> {
    Optional<PlayerDevice> findByDeviceTokenHash(String deviceTokenHash);
}
