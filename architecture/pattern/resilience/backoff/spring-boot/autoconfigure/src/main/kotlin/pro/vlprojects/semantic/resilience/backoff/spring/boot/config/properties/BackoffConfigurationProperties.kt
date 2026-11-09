package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "semantic.resilience.backoff", ignoreUnknownFields = false)
public data class BackoffConfigurationProperties(
    val default: BackoffDefaultProperties = BackoffDefaultProperties(),
    val instances: Map<String, BackoffProperties> = emptyMap(),
)
