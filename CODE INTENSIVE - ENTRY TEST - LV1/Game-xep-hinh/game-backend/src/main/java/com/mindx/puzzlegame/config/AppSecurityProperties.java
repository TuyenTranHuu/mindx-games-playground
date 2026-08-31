package com.mindx.puzzlegame.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        String jwtSecret,
        String hmacSecret,
        long playerJwtMinutes,
        long deviceTokenDays
) {}
