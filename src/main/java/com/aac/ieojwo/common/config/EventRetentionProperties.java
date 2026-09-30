package com.aac.ieojwo.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.retention")
public record EventRetentionProperties(
        Duration cardUsage,
        Duration stt,
        Duration sensor,
        Duration location
) {
}
