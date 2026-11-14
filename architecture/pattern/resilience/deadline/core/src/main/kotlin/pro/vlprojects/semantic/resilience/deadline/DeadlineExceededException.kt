package pro.vlprojects.semantic.resilience.deadline

import java.time.Duration

public class DeadlineExceededException(
    public val name: String,
    public val limit: Duration,
) : RuntimeException("Deadline '$name' of $limit was exceeded")
