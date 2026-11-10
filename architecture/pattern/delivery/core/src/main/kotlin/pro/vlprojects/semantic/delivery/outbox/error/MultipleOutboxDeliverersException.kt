package pro.vlprojects.semantic.delivery.outbox.error

import pro.vlprojects.semantic.delivery.outbox.DeliveryDestination

public class MultipleOutboxDeliverersException(
    destination: DeliveryDestination,
) : OutboxRoutingException("Several deliverers support the destination: $destination")
