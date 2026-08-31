package com.mindx.puzzlegame.security;

import com.mindx.puzzlegame.config.AppSecurityProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long playerJwtMinutes;

    public TokenService(AppSecurityProperties properties) {
        byte[] keyBytes = properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        this.playerJwtMinutes = properties.playerJwtMinutes();
    }

    public String issuePlayerToken(UUID playerId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("puzzle-game-api")
                .issuedAt(now)
                .expiresAt(now.plus(playerJwtMinutes, ChronoUnit.MINUTES))
                .subject(playerId.toString())
                .claim("role", "PLAYER")
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public UUID readPlayerId(String token) {
        Jwt jwt = decoder.decode(token);
        if (!"PLAYER".equals(jwt.getClaimAsString("role"))) {
            throw new JwtException("Invalid token role");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
