package com.mindx.puzzlegame.player;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_devices")
public class PlayerDevice {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "device_token_hash", nullable = false, unique = true, length = 64)
    private String deviceTokenHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at", nullable = false)
    private Instant lastUsedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected PlayerDevice() {}

    public PlayerDevice(UUID id, Player player, String deviceTokenHash, String userAgent,
                        Instant expiresAt, Instant now) {
        this.id = id;
        this.player = player;
        this.deviceTokenHash = deviceTokenHash;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
        this.lastUsedAt = now;
        this.createdAt = now;
    }

    public Player getPlayer() { return player; }

    public boolean isUsableAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed(Instant now) { this.lastUsedAt = now; }
}
