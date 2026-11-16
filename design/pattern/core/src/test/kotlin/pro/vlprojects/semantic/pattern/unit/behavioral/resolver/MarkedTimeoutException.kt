package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import java.net.SocketTimeoutException

class MarkedTimeoutException :
    SocketTimeoutException("The marked timeout"),
    RetryableFailure,
    TransientFailure
