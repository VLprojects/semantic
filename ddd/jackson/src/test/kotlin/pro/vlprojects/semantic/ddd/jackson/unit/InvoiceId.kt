package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import java.util.UUID

class InvoiceId(override val value: UUID) : BoxedIdentifier<UUID>
