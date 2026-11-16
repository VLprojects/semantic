package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import java.net.SocketTimeoutException

class ReadTimeoutException :
    SocketTimeoutException("The read timed out"),
    TransientFailure,
    RetryableFailure
