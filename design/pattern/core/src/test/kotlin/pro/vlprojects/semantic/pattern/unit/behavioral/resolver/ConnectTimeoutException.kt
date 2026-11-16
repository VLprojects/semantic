package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import java.net.SocketTimeoutException

class ConnectTimeoutException :
    SocketTimeoutException("The connection timed out"),
    TransientFailure
