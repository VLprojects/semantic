package pro.vlprojects.semantic.helper.number

public fun Long.isPositive(): Boolean = this > 0L

public fun Long.isNegative(): Boolean = this < 0L

public fun Long.isZero(): Boolean = this == 0L

public fun Long.isNotZero(): Boolean = this != 0L

public fun Long.isNonNegative(): Boolean = this >= 0L

public fun Long.toSafeInt(): Int = Math.toIntExact(this)

public fun Long.toSafeShort(): Short = this
    .toSafeInt()
    .toSafeShort()

public fun Long.toSafeByte(): Byte = this
    .toSafeInt()
    .toSafeByte()
