package pro.vlprojects.semantic.resilience.backoff.spring.boot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "semantic.resilience.backoff", ignoreUnknownFields = false)
public class BackoffProperties(
    public val default: BackoffInstanceProperties = BackoffInstanceProperties(),
    public val instances: Map<String, BackoffInstanceProperties> = emptyMap(),
)
