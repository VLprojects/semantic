package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.math.BigDecimal

class WeightId(value: BigDecimal) : AbstractId<BigDecimal>(value)
