package coroutines

import coroutines.ChallengeStatement.ChallengeBlock
import functional.collections.map.filter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

private fun generateChallenge(expectedStatements: Int, random: Random = Random): ChallengeStatement {
    var state: ChallengeStatement =
        ChallengeStatement.CoroutineScope(generateInitialBodyStatements(expectedStatements, random = random))
    while (true) {
        val statementsToAdd = expectedStatements - state.countStatements()
        if (statementsToAdd <= 0) break
        state = state.addRandomStatementToRandomBlock(statementLimit = statementsToAdd, random = random)
    }
    return state
}

private fun generateInitialBodyStatements(statementLimit: Int, random: Random): List<ChallengeStatement> = buildList {
    val firstStatement = randomStatement(statementLimit - 2, random)
    add(firstStatement)
    add(randomStatement(statementLimit - firstStatement.countStatements() - 2, random))
}

private fun randomStatement(blockStatementsLimit: Int, random: Random) = generateChallengeStatement(
    randomChallengeStatementType(allowBlocks = blockStatementsLimit > 2, random = random),
    blockStatementsLimit = blockStatementsLimit,
    random = random
)

private fun generateChallengeStatement(type: ChallengeStatementType, blockStatementsLimit: Int, random: Random) =
    when (type) {
        ChallengeStatementType.Delay -> ChallengeStatement.Delay(random.nextInt(1, 3) * 1000)
        ChallengeStatementType.Print -> ChallengeStatement.Print(('A'..'Z').random(random).toString())
        ChallengeStatementType.Launch -> ChallengeStatement.Launch(
            generateInitialBodyStatements(
                blockStatementsLimit,
                random = random
            )
        )

        ChallengeStatementType.CoroutineScope -> ChallengeStatement.CoroutineScope(
            generateInitialBodyStatements(
                blockStatementsLimit,
                random = random
            )
        )
    }

private fun randomChallengeStatementType(
    random: Random = Random,
    allowPrint: Boolean = true,
    allowBlocks: Boolean = true
) =
    (ChallengeStatementType.entries)
        .let { if (allowBlocks) it else it.filter { !it.isBlock } }
        .let { if (allowPrint) it else it - ChallengeStatementType.Print }
        .random(random)

enum class ChallengeStatementType(val isBlock: Boolean = false) {
    Delay,
    Print,
    Launch(isBlock = true),
    CoroutineScope(isBlock = true),
    // 2
//    AsyncAwait(isBlock = true),
//    LaunchJoin(isBlock = true),
//    LaunchCancel(isBlock = true),
    // 3
//    ThrowException,
//    ThrowCancellationException,
//    TryCatch(isBlock = true),
//    TryCatchCoroutineScope(isBlock = true),
//    SupervisorScope(isBlock = true),
}

private fun ChallengeStatement.countStatements(): Int =
    if (this is ChallengeStatement.ChallengeBlock) statements.sumOf { it.countStatements() } + 1
    else 1

private fun ChallengeStatement.countBlocks(): Int =
    if (this is ChallengeStatement.ChallengeBlock) statements.sumOf { it.countBlocks() } + 1
    else 0

private fun ChallengeStatement.addRandomStatementToRandomBlock(
    statementLimit: Int,
    random: Random
): ChallengeStatement {
    val statement = generateChallengeStatement(
        randomChallengeStatementType(allowBlocks = statementLimit > 2),
        statementLimit - 2,
        random = random
    )

    val possibleInsertionPoints = countBlocks()
    val chosenBlock = if (possibleInsertionPoints <= 1) 1 else random.nextInt(1, possibleInsertionPoints)
    var current = 0
    fun ChallengeStatement.addStatementToChosenBlock(): ChallengeStatement =
        if (this is ChallengeBlock) {
            current++
            if (current == chosenBlock) this.withStatement(statement) else this
        } else {
            this
        }
    return addStatementToChosenBlock()
}

private fun ChallengeBlock.withStatement(statement: ChallengeStatement): ChallengeBlock = when (this) {
    is ChallengeStatement.Launch -> ChallengeStatement.Launch(statements + statement)
    is ChallengeStatement.CoroutineScope -> ChallengeStatement.CoroutineScope(statements + statement)
}

sealed class ChallengeStatement {
    sealed class ChallengeBlock : ChallengeStatement() {
        abstract val statements: List<ChallengeStatement>
    }

    // 1
    data class Delay(val time: Int) : ChallengeStatement()
    data class Print(val text: String) : ChallengeStatement()
    data class Launch(override val statements: List<ChallengeStatement>) : ChallengeBlock()
    data class CoroutineScope(override val statements: List<ChallengeStatement>) : ChallengeBlock()
    // 2
//    data object RunBlocking
//    data object Exception: ChallengeStatement()
//    data class TryCatch(override val statements: List<ChallengeStatement>): ChallengeBlock()
//    data class SupervisorScope(override val statements: List<ChallengeStatement>): ChallengeBlock()
}

private fun ChallengeStatement.toCode(): String = when (this) {
    is ChallengeStatement.CoroutineScope -> "coroutineScope {\n${
        statements.joinToString(separator = "\n") {
            it.toCode().prependIndent("    ")
        }
    }\n}"

    is ChallengeStatement.Launch -> "launch {\n${
        statements.joinToString(separator = "\n") {
            it.toCode().prependIndent("    ")
        }
    }\n}"

    is ChallengeStatement.Delay -> "delay($time)"
    is ChallengeStatement.Print -> "println(\"${text}\")"
}

private fun ChallengeStatement.getResult(): String = buildList<String> {
    suspend fun evaluate(statement: ChallengeStatement, scope: CoroutineScope) {
        when (statement) {
            is ChallengeStatement.CoroutineScope -> coroutineScope {
                statement.statements.forEach {
                    evaluate(
                        it,
                        this
                    )
                }
            }

            is ChallengeStatement.Launch -> scope.launch { statement.statements.forEach { evaluate(it, this) } }
            is ChallengeStatement.Delay -> add("(${statement.time / 1000} sec)")
            is ChallengeStatement.Print -> add(statement.text)
        }
    }

    runTest {
        evaluate(this@getResult, this)
    }
}.joinToString("\n")

fun main() {
    val challenge = generateChallenge(10)
    println(challenge)
    println("******")
    println(challenge.toCode())
    println("******")
    println(challenge.getResult())
}

class AsyncGameTest {

    @Test
    fun `should not create bigger challenge than expected`() = repeat(20) {
        val size = it * 2 + 5
        val challenge = generateChallenge(size, Random(it * 1234L))
        assertEquals(size, challenge.countStatements())
    }

    @Test
    fun `should not generate two non-block statements (like print or delay) one after another`() {

    }

    @Test
    fun `should purge statements that do not change result`() {

    }

    @Test
    fun `should produce correct code`() {
    }

    @Test
    fun `should predict correct result`() {
    }
}
