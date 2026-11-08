package pro.vlprojects.semantic.resilience.bulkhead

import java.time.Duration

public interface BulkheadListener {

    public fun onPermitted(bulkhead: Bulkhead, waited: Duration) {
    }

    public fun onRejected(bulkhead: Bulkhead, waited: Duration) {
    }

    public fun onFinished(bulkhead: Bulkhead) {
    }
}
