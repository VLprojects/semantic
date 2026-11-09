package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import java.time.Duration

public const val FIXED_BACKOFF_TYPE: String = "fixed"

public class FixedBackoffFactory : BackoffFactory<FixedBackoffFactory.Config> {

    override val type: String = FIXED_BACKOFF_TYPE

    override val configClass: Class<Config> = Config::class.java

    override fun create(config: Config): Backoff = FixedBackoff(config.delay)

    public data class Config(val delay: Duration = Duration.ofSeconds(1))
}
