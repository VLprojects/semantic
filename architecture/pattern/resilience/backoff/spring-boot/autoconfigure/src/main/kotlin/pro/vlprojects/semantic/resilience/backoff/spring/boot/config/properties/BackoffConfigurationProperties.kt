package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "semantic.resilience.backoff", ignoreUnknownFields = false)
public class BackoffConfigurationProperties(
    public val default: DefaultBackoffProperties = DefaultBackoffProperties(),
    public val instances: Map<String, BackoffProperties> = emptyMap(),
)
