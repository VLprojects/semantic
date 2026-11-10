package pro.vlprojects.semantic.delivery.outbox

public fun interface DeliveryDestinationDeserializer {

    public fun deserialize(destination: String): DeliveryDestination
}
