package pro.vlprojects.semantic.delivery

public interface Message {

    public val messageId: String

    public val idempotencyKey: String

    public val payload: Any
}
