package pro.vlprojects.semantic.helper.number

import java.math.BigDecimal

public fun BigDecimal.isPositive(): Boolean = signum() > 0

public fun BigDecimal.isNegative(): Boolean = signum() < 0

public fun BigDecimal.isZero(): Boolean = signum() == 0

public fun BigDecimal.isNotZero(): Boolean = signum() != 0

public fun BigDecimal.isNonNegative(): Boolean = signum() >= 0

public fun BigDecimal.toSafeDouble(): Double = this
    .toDouble()
    .takeIf(Double::isFinite)
    ?: throw ArithmeticException("$this is out of the range of Double")

public fun BigDecimal.toSafeFloat(): Float = this
    .toFloat()
    .takeIf(Float::isFinite)
    ?: throw ArithmeticException("$this is out of the range of Float")

public fun BigDecimal.toSafeLong(): Long = longValueExact()

public fun BigDecimal.toSafeInt(): Int = intValueExact()

public fun BigDecimal.toSafeShort(): Short = shortValueExact()

public fun BigDecimal.toSafeByte(): Byte = byteValueExact()
