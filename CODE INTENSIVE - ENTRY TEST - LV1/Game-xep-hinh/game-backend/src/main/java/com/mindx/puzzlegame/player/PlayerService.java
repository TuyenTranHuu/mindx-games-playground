package com.mindx.puzzlegame.player;

import com.mindx.puzzlegame.common.ApiException;
import com.mindx.puzzlegame.config.AppSecurityProperties;
import com.mindx.puzzlegame.security.CryptoService;
import com.mindx.puzzlegame.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerDeviceRepository deviceRepository;
    private final CryptoService cryptoService;
    private final TokenService tokenService;
    private final RecoveryAttemptService recoveryAttemptService;
    private final long deviceTokenDays;

    public PlayerService(PlayerRepository playerRepository, PlayerDeviceRepository deviceRepository,
                         CryptoService cryptoService, TokenService tokenService,
                         RecoveryAttemptService recoveryAttemptService, AppSecurityProperties properties) {
        this.playerRepository = playerRepository;
        this.deviceRepository = deviceRepository;
        this.cryptoService = cryptoService;
        this.tokenService = tokenService;
        this.recoveryAttemptService = recoveryAttemptService;
        this.deviceTokenDays = properties.deviceTokenDays();
    }

    @Transactional
    public PlayerDtos.AuthResponse anonymous(String deviceToken, HttpServletRequest request) {
        if (deviceToken != null && !deviceToken.isBlank()) {
            return authenticateDevice(deviceToken, request);
        }

        Instant now = Instant.now();
        UUID playerId = UUID.randomUUID();
        String recoveryCode = cryptoService.randomToken(18);
        String rawDeviceToken = cryptoService.randomToken(32);
        String nickname = "Người chơi " + String.format("%04d", Math.floorMod(playerId.hashCode(), 10000));
        Player player = new Player(playerId, nickname, cryptoService.hmac(recoveryCode),
                cryptoService.hmac(clientIp(request)), now);
        playerRepository.save(player);
        deviceRepository.save(new PlayerDevice(UUID.randomUUID(), player, cryptoService.hmac(rawDeviceToken),
                limitedUserAgent(request), now.plus(deviceTokenDays, ChronoUnit.DAYS), now));
        return authResponse(player, rawDeviceToken, recoveryCode, true);
    }

    @Transactional
    public PlayerDtos.AuthResponse authenticateDevice(String deviceToken, HttpServletRequest request) {
        Instant now = Instant.now();
        PlayerDevice device = deviceRepository.findByDeviceTokenHash(cryptoService.hmac(deviceToken))
                .filter(item -> item.isUsableAt(now))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Device token không hợp lệ hoặc đã hết hạn"));
        Player player = requireActive(device.getPlayer());
        device.markUsed(now);
        player.markActive(now, cryptoService.hmac(clientIp(request)));
        return authResponse(player, null, null, false);
    }

    @Transactional
    public PlayerDtos.AuthResponse recover(String recoveryCode, HttpServletRequest request) {
        String attemptKey = cryptoService.hmac(clientIp(request));
        recoveryAttemptService.checkAllowed(attemptKey);
        Player player = playerRepository.findByRecoveryCodeHash(cryptoService.hmac(recoveryCode.trim()))
                .orElse(null);
        if (player == null) {
            recoveryAttemptService.recordFailure(attemptKey);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Mã khôi phục không đúng");
        }
        requireActive(player);
        recoveryAttemptService.clear(attemptKey);
        Instant now = Instant.now();
        String rawDeviceToken = cryptoService.randomToken(32);
        deviceRepository.save(new PlayerDevice(UUID.randomUUID(), player, cryptoService.hmac(rawDeviceToken),
                limitedUserAgent(request), now.plus(deviceTokenDays, ChronoUnit.DAYS), now));
        player.markActive(now, cryptoService.hmac(clientIp(request)));
        return authResponse(player, rawDeviceToken, null, false);
    }

    @Transactional(readOnly = true)
    public PlayerDtos.PlayerResponse getMe(UUID playerId) {
        Player player = requirePlayer(playerId);
        return new PlayerDtos.PlayerResponse(player.getId(), player.getNickname(), player.getStatus().name());
    }

    @Transactional
    public PlayerDtos.PlayerResponse changeNickname(UUID playerId, String rawNickname) {
        String nickname = rawNickname.trim().replaceAll("\\s+", " ");
        if (nickname.length() < 3 || nickname.length() > 30) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Biệt danh phải dài từ 3 đến 30 ký tự");
        }
        Player player = requirePlayer(playerId);
        player.changeNickname(nickname, Instant.now());
        return new PlayerDtos.PlayerResponse(player.getId(), player.getNickname(), player.getStatus().name());
    }

    public Player requirePlayer(UUID id) {
        return playerRepository.findById(id)
                .map(this::requireActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy người chơi"));
    }

    private Player requireActive(Player player) {
        if (player.getStatus() != PlayerStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Người chơi đã bị khóa");
        }
        return player;
    }

    private PlayerDtos.AuthResponse authResponse(Player player, String deviceToken,
                                                   String recoveryCode, boolean newPlayer) {
        return new PlayerDtos.AuthResponse(player.getId(), player.getNickname(),
                tokenService.issuePlayerToken(player.getId()), deviceToken, recoveryCode, newPlayer);
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private String limitedUserAgent(HttpServletRequest request) {
        String value = request.getHeader("User-Agent");
        if (value == null) return null;
        return value.substring(0, Math.min(500, value.length()));
    }
}
