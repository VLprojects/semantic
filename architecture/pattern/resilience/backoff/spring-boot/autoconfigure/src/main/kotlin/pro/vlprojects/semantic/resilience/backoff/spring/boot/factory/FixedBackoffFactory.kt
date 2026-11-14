package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import java.time.Duration

public class FixedBackoffFactory : BackoffFactory<FixedBackoffFactory.Config> {

    override fun create(config: Config): Backoff = FixedBackoff(config.delay)

    public data class Config(val delay: Duration = Duration.ofSeconds(1))
}
