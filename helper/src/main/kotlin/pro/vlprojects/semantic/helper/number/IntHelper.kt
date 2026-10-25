package pro.vlprojects.semantic.helper.number

public fun Int.isPositive(): Boolean = this > 0

public fun Int.isNegative(): Boolean = this < 0

public fun Int.isZero(): Boolean = this == 0

public fun Int.isNotZero(): Boolean = this != 0

public fun Int.isNonNegative(): Boolean = this >= 0

public fun Int.toSafeShort(): Short = when (this) {
    in Short.MIN_VALUE..Short.MAX_VALUE -> this.toShort()
    else -> throw ArithmeticException("$this is out of the range of Short")
}

public fun Int.toSafeByte(): Byte = when (this) {
    in Byte.MIN_VALUE..Byte.MAX_VALUE -> this.toByte()
    else -> throw ArithmeticException("$this is out of the range of Byte")
}
