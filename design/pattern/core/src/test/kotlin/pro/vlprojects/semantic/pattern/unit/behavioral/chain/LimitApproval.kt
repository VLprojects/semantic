package pro.vlprojects.semantic.pattern.unit.behavioral.chain

import pro.vlprojects.semantic.pattern.behavioral.chain.ChainOfResponsibility
import pro.vlprojects.semantic.pattern.behavioral.chain.Handler

class LimitApproval(
    private val approver: String,
    private val limit: Int,
) : Handler<Expense, String> {

    override fun handleRequest(request: Expense, successor: ChainOfResponsibility<Expense, String>) = when {
        request.amount <= limit -> approver
        else -> successor.handleRequest(request)
    }
}
