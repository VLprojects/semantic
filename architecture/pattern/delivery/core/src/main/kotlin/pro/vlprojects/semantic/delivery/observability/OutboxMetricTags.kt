package pro.vlprojects.semantic.delivery.observability

public object OutboxMetricTags {

    public const val HANDLER: String = "handler"
    public const val PAYLOAD_TYPE: String = "payload.type"
    public const val EXCEPTION: String = "exception"
    public const val OUTCOME: String = "outcome"
    public const val OUTCOME_SUCCESS: String = "success"
    public const val OUTCOME_FAILURE: String = "failure"
}
