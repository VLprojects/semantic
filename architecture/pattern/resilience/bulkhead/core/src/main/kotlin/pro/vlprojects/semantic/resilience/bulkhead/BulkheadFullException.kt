package pro.vlprojects.semantic.resilience.bulkhead

import java.time.Duration
import java.util.concurrent.RejectedExecutionException

public class BulkheadFullException(
    public val bulkheadName: String,
    public val limit: Int,
    public val waitLimit: Duration,
) : RejectedExecutionException("Bulkhead '$bulkheadName' is full: limit $limit, wait limit $waitLimit")
