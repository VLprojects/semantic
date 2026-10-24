package pro.vlprojects.semantic.ddd.unit.tactical.base

import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.base.NamedSpecification

enum class OrderStatus {
    OPEN,
    PAID,
}

data class Customer(
    val name: String,
    val email: String?,
)

data class OrderLine(
    val sku: String,
    val quantity: Int,
)

data class Order(
    val status: OrderStatus,
    val total: Int,
    val customer: Customer?,
    val lines: List<OrderLine>,
)

data object IsOpen : NamedSpecification<Order>({ Order::status eq OrderStatus.OPEN })

data class TotalAtLeast(
    val amount: Int,
) : NamedSpecification<Order>({ Order::total atLeast amount })

data class OfCustomer(
    val name: String,
) : NamedSpecification<Order>({ Order::customer / Customer::name eq name })

data class ContainsSku(
    val sku: String,
) : NamedSpecification<Order>({ Order::lines.any { OrderLine::sku eq sku } })

class WithoutEmail : NamedSpecification<Order>({ (Order::customer / Customer::email).isNull() })

data object NonEmpty : Specification<Collection<*>> {

    override fun isSatisfiedBy(candidate: Collection<*>): Boolean = candidate.isNotEmpty()
}

fun order(
    status: OrderStatus = OrderStatus.OPEN,
    total: Int = 100,
    customer: Customer? = Customer("ann", null),
    lines: List<OrderLine> = listOf(OrderLine("a", 1)),
) = Order(status, total, customer, lines)
