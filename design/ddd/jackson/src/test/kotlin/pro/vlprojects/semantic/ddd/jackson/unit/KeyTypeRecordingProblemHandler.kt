package pro.vlprojects.semantic.ddd.jackson.unit

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.DeserializationProblemHandler

class KeyTypeRecordingProblemHandler : DeserializationProblemHandler() {

    val weirdKeyTypes = mutableListOf<Class<*>>()

    override fun handleWeirdKey(
        context: DeserializationContext,
        rawKeyType: Class<*>,
        keyValue: String,
        failureMsg: String,
    ): Any {
        weirdKeyTypes += rawKeyType
        return NOT_HANDLED
    }
}
