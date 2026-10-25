package pro.vlprojects.semantic.helper.number

public fun Byte.isPositive(): Boolean = this > 0

public fun Byte.isNegative(): Boolean = this < 0

public fun Byte.isZero(): Boolean = this == 0.toByte()

public fun Byte.isNotZero(): Boolean = this != 0.toByte()

public fun Byte.isNonNegative(): Boolean = this >= 0
