package pro.vlprojects.semantic.delivery.outbox.error

import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination

public class OutboxDelivererNotResolvedException(
    destination: DeliveryDestination,
) : OutboxDeliveryException("No deliverer supports the destination: $destination")
