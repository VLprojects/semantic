package pro.vlprojects.semantic.messaging

import java.util.UUID

public class Envelope<out M : Any>(
    public val messageId: UUID,
    public val idempotencyKey: String,
    public val message: M,
    public val stamps: List<Stamp> = emptyList(),
) {

    public fun with(vararg stamps: Stamp): Envelope<M> = Envelope(
        messageId,
        idempotencyKey,
        message,
        this.stamps + stamps,
    )

    public fun <S : Stamp> withoutAll(type: Class<S>): Envelope<M> = stamps
        .filterNot(type::isInstance)
        .let { Envelope(messageId, idempotencyKey, message, it) }

    public fun <S : Stamp> all(type: Class<S>): List<S> = stamps
        .filterIsInstance(type)

    public fun <S : Stamp> last(type: Class<S>): S? = all(type)
        .lastOrNull()

    public inline fun <reified S : Stamp> withoutAll(): Envelope<M> = withoutAll(S::class.java)

    public inline fun <reified S : Stamp> all(): List<S> = all(S::class.java)

    public inline fun <reified S : Stamp> last(): S? = last(S::class.java)
}
