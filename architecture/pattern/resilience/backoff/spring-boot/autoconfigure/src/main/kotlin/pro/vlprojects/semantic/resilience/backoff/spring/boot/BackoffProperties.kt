package pro.vlprojects.semantic.resilience.backoff.spring.boot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "semantic.resilience.backoff", ignoreUnknownFields = false)
public class BackoffProperties(
    public val default: BackoffSettings = BackoffSettings(),
    public val instances: Map<String, BackoffSettings> = emptyMap(),
)
