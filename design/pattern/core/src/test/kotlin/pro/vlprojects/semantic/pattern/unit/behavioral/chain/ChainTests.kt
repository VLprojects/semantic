package pro.vlprojects.semantic.pattern.unit.behavioral.chain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.behavioral.chain.Chain
import pro.vlprojects.semantic.pattern.behavioral.chain.Handler

class ChainTests {

    private val manager = LimitApproval("manager", 1_000)

    private val director = LimitApproval("director", 10_000)

    private val approval = Chain.of(listOf(manager, director)) { _ -> "board" }

    @Test
    fun `the first handler that can handle a request handles it`() {
        assertEquals("manager", approval.proceed(Expense(500)))
    }

    @Test
    fun `a handler that cannot handle a request passes it on`() {
        assertEquals("director", approval.proceed(Expense(5_000)))
    }

    @Test
    fun `a request that no handler handles reaches the end of the chain`() {
        assertEquals("board", approval.proceed(Expense(50_000)))
    }

    @Test
    fun `a handler that handles a request does not pass it on`() {
        val passed = mutableListOf<Expense>()
        val board = Chain<Expense, String> { request ->
            passed.add(request)
            "board"
        }

        Chain.of(listOf(manager), board).proceed(Expense(500))

        assertEquals(emptyList<Expense>(), passed)
    }

    @Test
    fun `a chain without handlers is its end`() {
        val board = Chain.of(emptyList<Handler<Expense, String>>()) { _ -> "board" }

        assertEquals("board", board.proceed(Expense(500)))
    }

    @Test
    fun `a handler works before and after the rest of the chain`() {
        val log = mutableListOf<String>()
        val audit = Handler<Expense, String> { request, chain ->
            log.add("received ${request.amount}")
            chain
                .proceed(request)
                .also { approver -> log.add("approved by $approver") }
        }

        Chain.of(listOf(audit, manager, director)) { _ -> "board" }.proceed(Expense(5_000))

        assertEquals(listOf("received 5000", "approved by director"), log)
    }

    @Test
    fun `a handler passes a request on again`() {
        val attempts = mutableListOf<Expense>()
        val retry = Handler<Expense, String> { request, chain ->
            try {
                chain.proceed(request)
            } catch (failure: IllegalStateException) {
                chain.proceed(request)
            }
        }
        val board = Chain<Expense, String> { request ->
            attempts.add(request)
            check(attempts.size > 1) { "The first attempt of $request fails" }
            "board"
        }

        assertEquals("board", Chain.of(listOf(retry, manager), board).proceed(Expense(50_000)))
        assertEquals(2, attempts.size)
    }

    @Test
    fun `a handler passes on a request of its own`() {
        val surcharge = Handler<Expense, String> { request, chain ->
            Expense(request.amount + 600).let(chain::proceed)
        }
        val surcharged = Chain.of(listOf(surcharge, manager, director)) { _ -> "board" }

        assertEquals("director", surcharged.proceed(Expense(500)))
    }

    @Test
    fun `a chain keeps the handlers it is made of`() {
        val handlers = mutableListOf<Handler<Expense, String>>(director)
        val directorOnly = Chain.of(handlers) { _ -> "board" }

        handlers.add(0, manager)

        assertEquals("director", directorOnly.proceed(Expense(500)))
    }

    @Test
    fun `a handler takes part in several chains`() {
        val travel = Chain.of(listOf(manager)) { _ -> "travel desk" }
        val equipment = Chain.of(listOf(manager)) { _ -> "procurement" }

        assertEquals("travel desk", travel.proceed(Expense(5_000)))
        assertEquals("procurement", equipment.proceed(Expense(5_000)))
    }
}
