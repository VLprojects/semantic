package pro.vlprojects.semantic.resilience.retry

import java.time.Duration

public fun interface Sleeper {

    public fun sleep(delay: Duration)

    public companion object Factory {

        public fun ofThread(): Sleeper = Sleeper { delay -> Thread.sleep(delay) }
    }
}
