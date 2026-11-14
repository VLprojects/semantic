package pro.vlprojects.semantic.pattern.unit.behavioral.chain

import pro.vlprojects.semantic.pattern.behavioral.chain.Chain
import pro.vlprojects.semantic.pattern.behavioral.chain.Handler

class LimitApproval(
    private val approver: String,
    private val limit: Int,
) : Handler<Expense, String> {

    override fun handle(request: Expense, chain: Chain<Expense, String>) = when {
        request.amount <= limit -> approver
        else -> chain.proceed(request)
    }
}
