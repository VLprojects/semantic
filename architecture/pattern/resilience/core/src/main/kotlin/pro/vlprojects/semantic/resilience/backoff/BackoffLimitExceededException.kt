package pro.vlprojects.semantic.resilience.backoff

public class BackoffLimitExceededException(
    public val attempt: Int,
) : IllegalStateException("The backoff limit is exceeded after the attempt $attempt")
