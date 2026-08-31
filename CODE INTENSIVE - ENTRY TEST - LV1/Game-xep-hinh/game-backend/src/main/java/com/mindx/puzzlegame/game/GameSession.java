package com.mindx.puzzlegame.game;

import com.mindx.puzzlegame.player.Player;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "initial_board", columnDefinition = "jsonb", nullable = false)
    private List<Integer> initialBoard;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_board", columnDefinition = "jsonb", nullable = false)
    private List<Integer> currentBoard;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> moves;

    @Column(name = "move_count", nullable = false)
    private int moveCount;

    @Column(name = "active_elapsed_seconds", nullable = false)
    private int activeElapsedSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 20)
    private ValidationStatus validationStatus;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "saved_at")
    private Instant savedAt;

    @Column(name = "resumed_at")
    private Instant resumedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected GameSession() {}

    public GameSession(UUID id, Player player, List<Integer> board, Instant now) {
        this.id = id;
        this.player = player;
        this.initialBoard = new ArrayList<>(board);
        this.currentBoard = new ArrayList<>(board);
        this.moves = new ArrayList<>();
        this.status = GameStatus.PLAYING;
        this.validationStatus = ValidationStatus.PENDING;
        this.startedAt = now;
        this.resumedAt = now;
        this.lastActivityAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public Player getPlayer() { return player; }
    public List<Integer> getInitialBoard() { return List.copyOf(initialBoard); }
    public List<Integer> getCurrentBoard() { return List.copyOf(currentBoard); }
    public List<String> getMoves() { return List.copyOf(moves); }
    public int getMoveCount() { return moveCount; }
    public int getActiveElapsedSeconds() { return activeElapsedSeconds; }
    public GameStatus getStatus() { return status; }
    public ValidationStatus getValidationStatus() { return validationStatus; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public void finish(List<Integer> board, List<String> moves, boolean won, Instant now) {
        this.currentBoard = new ArrayList<>(board);
        this.moves = new ArrayList<>(moves);
        this.moveCount = moves.size();
        this.activeElapsedSeconds += (int) Duration.between(resumedAt, now).toSeconds();
        this.status = won ? GameStatus.WON : GameStatus.ENDED;
        this.validationStatus = ValidationStatus.VALID;
        this.finishedAt = now;
        this.lastActivityAt = now;
        this.updatedAt = now;
    }

    public void invalidate(Instant now) {
        this.validationStatus = ValidationStatus.INVALID;
        this.status = GameStatus.ENDED;
        this.finishedAt = now;
        this.lastActivityAt = now;
        this.updatedAt = now;
    }

    public void abandon(Instant now) {
        this.status = GameStatus.ABANDONED;
        this.finishedAt = now;
        this.lastActivityAt = now;
        this.updatedAt = now;
    }
}
