package pro.vlprojects.semantic.pattern.unit.behavioral.chain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.behavioral.chain.ChainOfResponsibility
import pro.vlprojects.semantic.pattern.behavioral.chain.Handler

class ChainOfResponsibilityTests {

    private val manager = LimitApproval("manager", 1_000)

    private val director = LimitApproval("director", 10_000)

    private val approval = ChainOfResponsibility.of(listOf(manager, director)) { _ -> "board" }

    @Test
    fun `the first handler that can handle a request handles it`() {
        assertEquals("manager", approval.handleRequest(Expense(500)))
    }

    @Test
    fun `a handler that cannot handle a request passes it on to its successor`() {
        assertEquals("director", approval.handleRequest(Expense(5_000)))
    }

    @Test
    fun `a request that no handler handles reaches the end of the chain`() {
        assertEquals("board", approval.handleRequest(Expense(50_000)))
    }

    @Test
    fun `a handler that handles a request does not pass it on to its successor`() {
        val passed = mutableListOf<Expense>()
        val board = ChainOfResponsibility<Expense, String> { request ->
            passed.add(request)
            "board"
        }

        ChainOfResponsibility.of(listOf(manager), board).handleRequest(Expense(500))

        assertEquals(emptyList<Expense>(), passed)
    }

    @Test
    fun `a chain without handlers is its end`() {
        val board = ChainOfResponsibility.of(emptyList<Handler<Expense, String>>()) { _ -> "board" }

        assertEquals("board", board.handleRequest(Expense(500)))
    }

    @Test
    fun `a handler works before and after its successor`() {
        val log = mutableListOf<String>()
        val audit = Handler<Expense, String> { request, successor ->
            log.add("received ${request.amount}")
            successor
                .handleRequest(request)
                .also { approver -> log.add("approved by $approver") }
        }
        val audited = ChainOfResponsibility.of(listOf(audit, manager, director)) { _ -> "board" }

        audited.handleRequest(Expense(5_000))

        assertEquals(listOf("received 5000", "approved by director"), log)
    }

    @Test
    fun `a handler passes a request on to its successor again`() {
        val attempts = mutableListOf<Expense>()
        val retry = Handler<Expense, String> { request, successor ->
            try {
                successor.handleRequest(request)
            } catch (failure: IllegalStateException) {
                successor.handleRequest(request)
            }
        }
        val board = ChainOfResponsibility<Expense, String> { request ->
            attempts.add(request)
            check(attempts.size > 1) { "The first attempt of $request fails" }
            "board"
        }
        val retried = ChainOfResponsibility.of(listOf(retry, manager), board)

        assertEquals("board", retried.handleRequest(Expense(50_000)))
        assertEquals(2, attempts.size)
    }

    @Test
    fun `a handler passes a request of its own on to its successor`() {
        val surcharge = Handler<Expense, String> { request, successor ->
            Expense(request.amount + 600).let(successor::handleRequest)
        }
        val surcharged = ChainOfResponsibility.of(listOf(surcharge, manager, director)) { _ -> "board" }

        assertEquals("director", surcharged.handleRequest(Expense(500)))
    }

    @Test
    fun `a chain keeps the handlers it is made of`() {
        val handlers = mutableListOf<Handler<Expense, String>>(director)
        val directorOnly = ChainOfResponsibility.of(handlers) { _ -> "board" }

        handlers.add(0, manager)

        assertEquals("director", directorOnly.handleRequest(Expense(500)))
    }

    @Test
    fun `a handler takes part in several chains`() {
        val travel = ChainOfResponsibility.of(listOf(manager)) { _ -> "travel desk" }
        val equipment = ChainOfResponsibility.of(listOf(manager)) { _ -> "procurement" }

        assertEquals("travel desk", travel.handleRequest(Expense(5_000)))
        assertEquals("procurement", equipment.handleRequest(Expense(5_000)))
    }
}
