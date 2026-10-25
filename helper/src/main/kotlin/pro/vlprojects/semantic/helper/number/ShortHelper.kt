package pro.vlprojects.semantic.helper.number

public fun Short.isPositive(): Boolean = this > 0

public fun Short.isNegative(): Boolean = this < 0

public fun Short.isZero(): Boolean = this == 0.toShort()

public fun Short.isNotZero(): Boolean = this != 0.toShort()

public fun Short.isNonNegative(): Boolean = this >= 0

public fun Short.toSafeByte(): Byte = this
    .toInt()
    .toSafeByte()
