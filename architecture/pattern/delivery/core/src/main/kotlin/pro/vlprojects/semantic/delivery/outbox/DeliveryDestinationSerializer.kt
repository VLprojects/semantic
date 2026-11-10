package pro.vlprojects.semantic.delivery.outbox

public fun interface DeliveryDestinationSerializer {

    public fun serialize(destination: DeliveryDestination): String
}
