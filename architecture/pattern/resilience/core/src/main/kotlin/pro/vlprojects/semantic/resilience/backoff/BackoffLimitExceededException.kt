package pro.vlprojects.semantic.resilience.backoff

public class BackoffLimitExceededException(
    public val attempt: Int,
) : RuntimeException("The backoff limit is exceeded after the attempt $attempt")
