package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.boot.context.properties.ConfigurationProperties

internal const val BACKOFF_PROPERTIES_PREFIX = "semantic.resilience.backoff"

@ConfigurationProperties(BACKOFF_PROPERTIES_PREFIX)
public class BackoffProperties(
    public val default: Map<String, String> = emptyMap(),
    public val instances: Map<String, Map<String, String>> = emptyMap(),
)
