package pro.vlprojects.semantic.helper.number

import java.math.BigDecimal

public fun BigDecimal.isPositive(): Boolean = this.signum() > 0

public fun BigDecimal.isNegative(): Boolean = this.signum() < 0

public fun BigDecimal.isZero(): Boolean = this.signum() == 0

public fun BigDecimal.isNotZero(): Boolean = this.signum() != 0

public fun BigDecimal.isNonNegative(): Boolean = this.signum() >= 0

public fun BigDecimal.toSafeDouble(): Double = this
    .toDouble()
    .takeIf(Double::isFinite)
    ?: throw ArithmeticException("$this is out of the range of Double")

public fun BigDecimal.toSafeFloat(): Float = this
    .toFloat()
    .takeIf(Float::isFinite)
    ?: throw ArithmeticException("$this is out of the range of Float")

public fun BigDecimal.toSafeLong(): Long = this.longValueExact()

public fun BigDecimal.toSafeInt(): Int = this.intValueExact()

public fun BigDecimal.toSafeShort(): Short = this.shortValueExact()

public fun BigDecimal.toSafeByte(): Byte = this.byteValueExact()
