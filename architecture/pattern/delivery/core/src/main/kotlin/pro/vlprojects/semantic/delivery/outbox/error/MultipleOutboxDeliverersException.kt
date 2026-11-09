package pro.vlprojects.semantic.delivery.outbox.error

import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination

public class MultipleOutboxDeliverersException(
    destination: DeliveryDestination,
) : OutboxDeliveryException("Several deliverers support the destination: $destination")
