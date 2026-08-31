package com.mindx.puzzlegame.player;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "players")
public class Player {
    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "recovery_code_hash", nullable = false, unique = true, length = 64)
    private String recoveryCodeHash;

    @Column(name = "ip_hmac", nullable = false, length = 64)
    private String ipHmac;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlayerStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    protected Player() {}

    public Player(UUID id, String nickname, String recoveryCodeHash, String ipHmac, Instant now) {
        this.id = id;
        this.nickname = nickname;
        this.recoveryCodeHash = recoveryCodeHash;
        this.ipHmac = ipHmac;
        this.status = PlayerStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
        this.lastActiveAt = now;
    }

    public UUID getId() { return id; }
    public String getNickname() { return nickname; }
    public String getRecoveryCodeHash() { return recoveryCodeHash; }
    public PlayerStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastActiveAt() { return lastActiveAt; }

    public void changeNickname(String nickname, Instant now) {
        this.nickname = nickname;
        this.updatedAt = now;
        this.lastActiveAt = now;
    }

    public void markActive(Instant now, String ipHmac) {
        this.lastActiveAt = now;
        this.updatedAt = now;
        this.ipHmac = ipHmac;
    }
}
