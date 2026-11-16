package pro.vlprojects.semantic.resilience.deadline

import java.time.Duration

public interface DeadlineListener {

    public fun onCompleted(elapsed: Duration) {}

    public fun onFailed(elapsed: Duration, failure: Throwable) {}

    public fun onExpired(limit: Duration) {}

    public fun onOverrun(elapsed: Duration) {}
}
