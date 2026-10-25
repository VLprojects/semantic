package pro.vlprojects.semantic.format

import pro.vlprojects.semantic.helper.text.takeLastSymbols
import pro.vlprojects.semantic.helper.text.takeSymbols

private const val ELLIPSIS = "…"

public fun interface EllipsisMode {

    public fun abbreviate(value: String, keepLength: Int): String

    public data object HideBeginning : EllipsisMode {

        override fun abbreviate(value: String, keepLength: Int): String =
            "$ELLIPSIS${value.takeLastSymbols(keepLength)}"
    }

    public data object HideMiddle : EllipsisMode {

        override fun abbreviate(value: String, keepLength: Int): String =
            "${value.takeSymbols(keepLength - keepLength / 2)}$ELLIPSIS${value.takeLastSymbols(keepLength / 2)}"
    }

    public data object HideEnding : EllipsisMode {

        override fun abbreviate(value: String, keepLength: Int): String = "${value.takeSymbols(keepLength)}$ELLIPSIS"
    }
}
