package pro.vlprojects.semantic.delivery.outbox.annotation

// Not PROPERTY: Java reflection does not see an annotation on the synthetic holder of a property.
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class IdempotencyKey
