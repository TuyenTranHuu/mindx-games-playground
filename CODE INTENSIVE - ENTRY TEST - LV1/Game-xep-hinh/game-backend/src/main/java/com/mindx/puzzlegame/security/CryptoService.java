package com.mindx.puzzlegame.security;

import com.mindx.puzzlegame.config.AppSecurityProperties;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class CryptoService {
    private final byte[] secret;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(AppSecurityProperties properties) {
        this.secret = properties.hmacSecret().getBytes(StandardCharsets.UTF_8);
    }

    public String randomToken(int bytes) {
        byte[] value = new byte[bytes];
        random.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create HMAC", exception);
        }
    }
}
