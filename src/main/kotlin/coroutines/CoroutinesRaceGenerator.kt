package coroutines

import coroutines.ChallengeStatement.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

suspend fun generateChallengeBlock(
    expectedStatements: Int,
    difficulty: CoroutinesRacesDifficulty
) = generateChallengeBlock(
    expectedStatements,
    vg = CoroutinesGameValueGenerator(),
    types = difficulty.typesForDifficulty()
)

enum class CoroutinesRacesDifficulty {
    Simple,
    WithSynchronization,
    WithSynchronizationAndExceptions,
    WithExceptions
}

private fun CoroutinesRacesDifficulty.typesForDifficulty(): List<ChallengeStatementType> =
    when (this) {
        CoroutinesRacesDifficulty.Simple -> listOf(
            ChallengeStatementType.Delay,
            ChallengeStatementType.Print,
            ChallengeStatementType.Launch,
            ChallengeStatementType.AsyncAwait,
            ChallengeStatementType.AsyncAwait,
            ChallengeStatementType.AsyncAwait,
        )

        CoroutinesRacesDifficulty.WithSynchronization -> listOf(
            ChallengeStatementType.Delay,
            ChallengeStatementType.Print,
            ChallengeStatementType.LaunchJoin,
            ChallengeStatementType.ScopeLaunch,
            ChallengeStatementType.AsyncAwait,
            ChallengeStatementType.CoroutineScope,
            ChallengeStatementType.JobCompleteJoin,
        )

        CoroutinesRacesDifficulty.WithExceptions -> listOf(
            ChallengeStatementType.Delay,
            ChallengeStatementType.Delay,
            ChallengeStatementType.Print,
            ChallengeStatementType.Print,
            ChallengeStatementType.Launch,
            ChallengeStatementType.ScopeLaunch,
            ChallengeStatementType.AsyncAwait,
            ChallengeStatementType.AsyncAwait,
            ChallengeStatementType.CoroutineScope,
            ChallengeStatementType.ThrowException,
            ChallengeStatementType.TryCatch,
            ChallengeStatementType.SupervisorScope,
            ChallengeStatementType.SupervisorScope,
        )

        CoroutinesRacesDifficulty.WithSynchronizationAndExceptions -> listOf(
            ChallengeStatementType.Delay,
            ChallengeStatementType.Print,
            ChallengeStatementType.AsyncAwait,
            ChallengeStatementType.CoroutineScope,
            ChallengeStatementType.LaunchJoin,
            ChallengeStatementType.LaunchCancel,
            ChallengeStatementType.JobCompleteJoin,
            ChallengeStatementType.ScopeLaunch,
            ChallengeStatementType.ThrowException,
            ChallengeStatementType.TryCatch,
            ChallengeStatementType.SupervisorScope,
        )
    }

suspend private fun generateChallengeBlock(
    expectedStatements: Int,
    vg: CoroutinesGameValueGenerator = CoroutinesGameValueGenerator(),
    types: List<ChallengeStatementType> = ChallengeStatementType.entries,
): ChallengeBlock {
    var state: ChallengeBlock =
        ChallengeStatement.CoroutineScope(
            statements = generateInitialBodyStatements(
                expectedStatements,
                vg = vg,
                types = types
            )
        )
    do {
        prepareValuesInHolder(vg, state)
        while (true) {
            val statementsToAdd = expectedStatements - state.countStatements()
            if (statementsToAdd <= 0) break
            state = state.addRandomStatementToRandomBlock(
                statementLeft = statementsToAdd,
                vg = vg,
                types = types
            )
        }
        yield()
        state = state
            .purgePrintsThatHappenAtTheSameTime()
            .purgeStatementsThatNotAffectResult()
            .purgeUsagesWithoutStatementsOrStatementsWithoutUsages()
            .purgeJoinsThatResultWithInfiniteWait()
        yield()
    } while (state.countStatements() < expectedStatements)
    return state
}

private fun ChallengeStatement.valuesUsed() = buildList<String> {
    forEveryStatement {
        if (it is Async) {
            add(it.resultString)
        }
        if (it is Print) {
            add(it.text)
        }
        if (it is PrintAwait) {
            add(it.text)
        }
    }
}

private fun prepareValuesInHolder(vg: CoroutinesGameValueGenerator, state: ChallengeStatement) {
    val values = mutableSetOf<String>()
    val jobs = mutableSetOf<String>()
    val strings = mutableSetOf<String>()
    state.forEveryStatement {
        if (it is LaunchJob) {
            jobs += it.variableName
        }
        if (it is Async) {
            values += it.variableName
            strings += it.resultString
        }
        if (it is Print) {
            strings += it.text
        }
        if (it is PrintAwait) {
            strings += it.text
        }
        if (it is ChallengeStatement.Job) {
            jobs += it.variableName
        }
    }
    vg.restartJobNames(jobs)
    vg.restartVariableNames(values)
    vg.restartStrings(strings)
}

private fun generateInitialBodyStatements(
    statementLeft: Int,
    vg: CoroutinesGameValueGenerator,
    types: List<ChallengeStatementType>,
): List<ChallengeStatement> =
    buildList {
        add(
            generateChallengeStatement(
                randomChallengeStatementType(
                    statementsLeft = statementLeft,
                    isFirstInBlock = true,
                    vg = vg,
                    types = types
                ),
                statementLeft = statementLeft,
                vg = vg,
                types = types,
            )
        )
    }

private fun generateChallengeStatement(
    type: ChallengeStatementType,
    statementLeft: Int,
    vg: CoroutinesGameValueGenerator,
    types: List<ChallengeStatementType>,
): ChallengeStatement {
    fun generateStatementBody() =
        generateInitialBodyStatements(
            statementLeft = statementLeft - type.statementsNeeded,
            vg = vg,
            types = types
        )
    return when (type) {
        ChallengeStatementType.Delay -> ChallengeStatement.Delay(
            time = vg.random.nextInt(1, 3) * 1000
        )

        ChallengeStatementType.Print -> ChallengeStatement.Print(
            text = vg.nextString()
        )

        ChallengeStatementType.Launch -> ChallengeStatement.Launch(
            statements = generateStatementBody()
        )

        ChallengeStatementType.CoroutineScope -> ChallengeStatement.CoroutineScope(
            statements = generateStatementBody()
        )

        ChallengeStatementType.AsyncAwait -> ChallengeStatement.Async(
            variableName = vg.nextVariableName(),
            resultString = vg.nextString(),
            statements = generateStatementBody()
        )

        ChallengeStatementType.LaunchJoin, ChallengeStatementType.LaunchCancel -> LaunchJob(
            variableName = vg.nextJobName(),
            statements = generateStatementBody()
        )

        ChallengeStatementType.ScopeLaunch -> ChallengeStatement.ScopeLaunch(
            statements = generateStatementBody(),
        )

        ChallengeStatementType.JobCompleteJoin -> ChallengeStatement.Job(
            variableName = vg.nextJobName()
        )

        ChallengeStatementType.ThrowException -> ChallengeStatement.ThrowException(
            exceptionType = ExceptionType.entries.random(),
        )

        ChallengeStatementType.TryCatch -> ChallengeStatement.TryCatch(
            statements = generateStatementBody(),
            exceptionType = ExceptionType.entries.random(),
        )

        ChallengeStatementType.SupervisorScope -> ChallengeStatement.SupervisorScope(
            statements = generateStatementBody()
        )
    }
}

private fun generateStatementUsagesStatements(
    statementType: ChallengeStatementType,
    statement: ChallengeStatement,
    vg: CoroutinesGameValueGenerator,
): List<ChallengeStatement> = when (statementType) {
    ChallengeStatementType.AsyncAwait -> listOf(
        PrintAwait(
            variableName = (statement as Async).variableName,
            text = statement.resultString
        ),
    )

    ChallengeStatementType.LaunchJoin -> listOf(
        Join(
            variableName = (statement as LaunchJob).variableName
        )
    )

    ChallengeStatementType.LaunchCancel -> listOf(
        Cancel(
            variableName = (statement as LaunchJob).variableName
        )
    )

    ChallengeStatementType.JobCompleteJoin -> {
        val variableName = (statement as ChallengeStatement.Job).variableName
        listOf(
            Join(variableName = variableName),
            CompleteJob(variableName = variableName)
        )
    }

    else -> if (!statementType.hasUsage) listOf() else error("Define usages for $statementType")
}

private fun randomChallengeStatementType(
    vg: CoroutinesGameValueGenerator,
    isFirstInBlock: Boolean = false,
    statementsLeft: Int = Int.MAX_VALUE,
    types: List<ChallengeStatementType>,
) =
    types.filter { statementsLeft >= it.statementsNeeded }
        .let { if (isFirstInBlock) it.filterNot { it.hasUsage } - listOf(ChallengeStatementType.Print) else it }
        .randomOrNull(vg.random) ?: ChallengeStatementType.Print

enum class ChallengeStatementType(
    val isBlock: Boolean = false,
    val hasUsage: Boolean = false,
) {
    Delay,
    Print,
    Launch(isBlock = true),
    AsyncAwait(isBlock = true, hasUsage = true),

    // 2
    CoroutineScope(isBlock = true),
    LaunchJoin(isBlock = true, hasUsage = true),
    LaunchCancel(isBlock = true, hasUsage = true),
    ScopeLaunch(isBlock = true),
    JobCompleteJoin(hasUsage = true),

    // 3
    ThrowException,
    TryCatch(isBlock = true),
    SupervisorScope(isBlock = true);

    val statementsNeeded = 1 + (if (isBlock) 1 else 0) + (if (hasUsage) 1 else 0)
}

fun ChallengeStatement.countStatements(): Int =
    if (this is ChallengeBlock) statements.sumOf { it.countStatements() } + 1
    else 1

private fun ChallengeStatement.countBlocks(): Int =
    if (this is ChallengeBlock) statements.sumOf { it.countBlocks() } + 1
    else 0

private fun ChallengeStatement.countPossibleInsertionPoints(): Int =
    if (this is ChallengeBlock) statements.size + 1 + statements.sumOf { it.countPossibleInsertionPoints() }
    else 0

private fun ChallengeBlock.addRandomStatementToRandomBlock(
    statementLeft: Int,
    vg: CoroutinesGameValueGenerator,
    types: List<ChallengeStatementType>,
): ChallengeBlock {
    val statementType =
        randomChallengeStatementType(statementsLeft = statementLeft, vg = vg, types = types)
    val statement = generateChallengeStatement(
        statementType,
        statementLeft = statementLeft - statementType.statementsNeeded,
        vg = vg,
        types = types,
    )
    val usageStatements = generateStatementUsagesStatements(statementType, statement, vg = vg)
    return when (usageStatements.size) {
        0 -> addStatementAtRandomPosition(statement, vg = vg)
        1 -> addStatementAtRandomPosition(statement, vg = vg)
            .addStatementAtRandomPositionAfter(
                usageStatements.first(),
                afterStatement = statement,
                vg = vg
            )

        2 -> {
            val (u1, u2) = usageStatements
            addStatementAtFirstPosition(statement)
                .addStatementAtRandomPositionAfter(u1, afterStatement = statement, vg = vg)
                .addStatementAtRandomPositionAfter(u2, afterStatement = u1, vg = vg)

        }

        else -> error("Unsupported number of usages: ${usageStatements.size}")
    }
}

private fun ChallengeBlock.addStatementAtFirstPosition(
    statement: ChallengeStatement,
): ChallengeBlock {
    return withStatements(listOf(statement) + statements)
}

fun ChallengeBlock.addStatementAtRandomPosition(
    statement: ChallengeStatement,
    vg: CoroutinesGameValueGenerator,
): ChallengeBlock {
    val possibleInsertionPoints = countPossibleInsertionPoints()
    val chosenPoint =
        if (possibleInsertionPoints <= 1) 0 else vg.random.nextInt(0, possibleInsertionPoints)

    fun ChallengeBlock.addStatementToChosenInsertionPoint(pointsLeft: Int): ChallengeBlock = when {
        pointsLeft <= statements.size -> withStatements( // Add in this block
            statements.plusAt(pointsLeft, statement)
        )

        else -> {
            var pointsLeftCount = pointsLeft - statements.size - 1
            withStatements(statements.map { s ->
                if (s !is ChallengeBlock) return@map s
                val insertionPointsInStatement = s.countPossibleInsertionPoints()
                if (pointsLeftCount <= insertionPointsInStatement) {
                    s.addStatementToChosenInsertionPoint(pointsLeftCount)
                } else {
                    pointsLeftCount -= insertionPointsInStatement
                    s
                }
            })
        }
    }

    return addStatementToChosenInsertionPoint(chosenPoint)
}

fun ChallengeBlock.addStatementAtRandomPositionAfter(
    statement: ChallengeStatement,
    afterStatement: ChallengeStatement,
    vg: CoroutinesGameValueGenerator,
): ChallengeBlock {
    fun ChallengeBlock.findStatementToStartInsertion(): ChallengeBlock =
        if (afterStatement in statements) {
            val positionAfterStatement = statements.indexOf(afterStatement) + 1
            withStatements(
                statements.take(positionAfterStatement) +
                        ChallengeStatement.CoroutineScope(statements = statements.drop(positionAfterStatement))
                            .addStatementAtRandomPosition(statement, vg)
                            .statements
            )
        } else {
            withStatements(statements.map { if (it is ChallengeBlock) it.findStatementToStartInsertion() else it })
        }

    return findStatementToStartInsertion()
}

fun ChallengeBlock.purgeStatementsThatNotAffectResult(): ChallengeBlock {
    // We must compare like statements, otherwise comparing launch or async is useless
    fun getResult(statements: List<ChallengeStatement>) =
        ChallengeStatement.CoroutineScope(statements = statements).getResult()

    val currentResult = getResult(statements)
    fun theSameResult(statements: List<ChallengeStatement>) = getResult(statements) == currentResult

    var newStatements = statements

    // Purge print that is the first statement
    newStatements.firstOrNull()?.let { firstStatement ->
        if (firstStatement is Print) {
            newStatements -= firstStatement
        }
    }

    // Try inlining different statements
    newStatements.forEach { statement ->
        if (statement !is ChallengeBlock) return@forEach
        val afterInlining =
            statements.flatMap { if (it == statement) statement.statements else listOf(it) }
                .removeUsages(statement)
        if (theSameResult(afterInlining)) {
            newStatements = afterInlining
        }
    }

    // Try removing different statements
    newStatements.forEach { statement ->
        val afterRemoving = (newStatements - statement).removeUsages(statement)
        if (theSameResult(afterRemoving)) {
            newStatements = afterRemoving
        }
    }

    // Remove empty blocks
    val emptyBlocks = newStatements.filter { it is ChallengeBlock && it.statements.isEmpty() }
    newStatements = newStatements.minus(emptyBlocks)
    for (emptyBlock in emptyBlocks) {
        newStatements = newStatements.removeUsages(emptyBlock)
    }

    // Remove block with only print
    for (statement in newStatements) {
        if (statement is ChallengeBlock && statement.statements.all { it is Print }) {
            newStatements -= statement
        }
    }

    // Remove try-catch blocks with only throw inside
    for (statement in newStatements) {
        if (statement is TryCatch && statement.statements.singleOrNull() is ThrowException) {
            newStatements -= statement
        }
    }

    // Remove repeating print or delay
    for ((curr, next) in newStatements.zipWithNext()) {
        if (curr is Print && next is Print) {
            newStatements -= curr
        }
        if (curr is ChallengeStatement.Delay && next is ChallengeStatement.Delay) {
            newStatements -= curr
        }
    }

    // Remove repeating print+delay or delay+print
    for ((e1, e2, e3, e4) in newStatements.windowed(4, 1)) {
        if (e1 is Print && e2 is ChallengeStatement.Delay && e3 is Print && e4 is ChallengeStatement.Delay) {
            newStatements -= e3
            newStatements -= e4
        }
        if (e1 is ChallengeStatement.Delay && e2 is Print && e3 is ChallengeStatement.Delay && e4 is Print) {
            newStatements -= e3
            newStatements -= e4
        }
    }

    // Try the same for all statements
    newStatements =
        newStatements.map { if (it is ChallengeBlock) it.purgeStatementsThatNotAffectResult() else it }

    if (newStatements != statements) {
        return withStatements(newStatements)
            .purgeStatementsThatNotAffectResult()
    } else {
        return this
    }
}

fun List<ChallengeStatement>.removeUsages(statement: ChallengeStatement): List<ChallengeStatement> {
    if (statement is WithUsage) {
        return ChallengeStatement.CoroutineScope(statements = this)
            .mapNotNullStatement {
                when (it) {
                    is PrintAwait ->
                        if (it.variableName == statement.variableName) Print(
                            text = it.text
                        ) else it

                    is Usage ->
                        if (it.variableName == statement.variableName) null else it

                    else -> it
                }
            }
            .statements
    }
    return this
}

private fun ChallengeBlock.purgePrintsThatHappenAtTheSameTime(): ChallengeBlock {
    val results = this.getResult()
    var newStatement = this
    for ((elem, next) in results.zipWithNext()) {
        if (elem.time == next.time) {
            val findPrint = this.findStatement { it is Print && it.text == elem.value }
                ?: this.findStatement { it is Print && it.text == next.value }
                ?: continue
            newStatement = this - findPrint
        }
    }
    return newStatement
}

private fun ChallengeBlock.purgeUsagesWithoutStatementsOrStatementsWithoutUsages(): ChallengeBlock {
    val challenge = this
    return challenge.mapNotNullStatement { statement ->
        when {
            statement is Usage && !challenge.anyStatement { it is WithUsage && statement.variableName == it.variableName } -> null
            statement is WithUsage && !challenge.anyStatement { it is Usage && statement.variableName == it.variableName } -> {
                when (statement) {
                    is Async -> Launch(statements = statement.statements)
                    is LaunchJob -> Launch(statements = statement.statements)
                    else -> error("$statement is not a statement with usage (if it is, add it to when)")
                }
            }

            else -> statement
        }
    }
}

private fun ChallengeBlock.purgeJoinsThatResultWithInfiniteWait(): ChallengeBlock {
    fun ChallengeStatement.hasInfiniteWait(): Boolean =
        this.getResult().any { it.time > 1_000_000 }

    fun ChallengeBlock.withoutUsageItsDeclarationAndItsUsages(usage: ChallengeStatement): ChallengeBlock {
        val variableName = when (usage) {
            is WithUsage -> usage.variableName
            is Usage -> usage.variableName
            else -> error("Incorrect argument: $usage")
        }
        return mapNotNullStatement {
            when {
                it is LaunchJob && it.variableName == variableName -> Launch(
                    statements = it.statements
                )

                it is WithUsage && it.variableName == variableName -> null
                it is Usage && it.variableName == variableName -> null
                else -> it
            }
        }
    }

    if (!hasInfiniteWait()) {
        return this
    }
    val potentialStatementsToRemove = this.filterFromAllStatements { it is ChallengeStatement.Job }
    for (potentialStatement in potentialStatementsToRemove) {
        if (!(this - potentialStatement).hasInfiniteWait()) {
            return withoutUsageItsDeclarationAndItsUsages(potentialStatement)
        }
    }

    // We got a problem! More than one infinite waits, purging them all
    return potentialStatementsToRemove.fold(this) { acc, usage ->
        acc.withoutUsageItsDeclarationAndItsUsages(
            usage
        )
    }
}

fun ChallengeStatement.forEveryStatement(block: (ChallengeStatement) -> Unit) {
    block(this)
    if (this is ChallengeBlock) {
        statements.forEach { it.forEveryStatement(block) }
    }
}

fun ChallengeBlock.mapStatementsRecursive(block: (List<ChallengeStatement>) -> List<ChallengeStatement>): ChallengeBlock =
    withStatements(statements.let(block).map { if (it is ChallengeBlock) it.mapStatementsRecursive(block) else it })

fun ChallengeBlock.findStatement(predicate: (ChallengeStatement) -> Boolean): ChallengeStatement? {
    for (statement in statements) {
        if (predicate(statement)) return statement
        if (statement is ChallengeBlock) {
            val found = statement.findStatement(predicate)
            if (found != null) return found
        }
    }
    return null
}

private fun ChallengeStatement.anyStatement(block: (ChallengeStatement) -> Boolean): Boolean {
    if (block(this)) return true
    if (this is ChallengeBlock) {
        for (statement in statements) {
            if (statement.anyStatement(block)) return true
        }
    }
    return false
}

private fun ChallengeBlock.filterFromAllStatements(predicate: (ChallengeStatement) -> Boolean): List<ChallengeStatement> =
    buildList { forEveryStatement { if (predicate(it)) add(it) } }

private fun ChallengeBlock.mapNotNullStatement(block: (ChallengeStatement) -> ChallengeStatement?): ChallengeBlock =
    withStatements(statements.mapNotNull { block(it) }
        .map { if (it is ChallengeBlock) it.mapNotNullStatement(block) else it })

operator fun ChallengeStatement.contains(statement: ChallengeStatement): Boolean =
    when (this) {
        is ChallengeBlock -> this == statement || statements.any { it.contains(statement) }
        else -> this == statement
    }

private operator fun ChallengeBlock.minus(elem: ChallengeStatement): ChallengeBlock =
    withStatements((statements - elem).map { if (it is ChallengeBlock) it - elem else it })

private fun ChallengeBlock.minusAll(vararg elem: ChallengeStatement): ChallengeBlock =
    withStatements((statements - elem).map { if (it is ChallengeBlock) it.minusAll(*elem) else it })

private fun <T> List<T>.plusAt(index: Int, element: T): List<T> {
    require(index in 0..size) { "Tried to add element at $index, but size is only $size" }
    val mutable = toMutableList()
    mutable.add(index, element)
    return mutable
}

sealed class ChallengeStatement {
    abstract val id: String

    sealed class ChallengeBlock : ChallengeStatement() {
        abstract val statements: List<ChallengeStatement>
        abstract fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock
    }

    interface WithUsage {
        val variableName: String
    }

    interface Usage {
        val variableName: String
    }

    data class Delay(
        override val id: String = randomId(),
        val time: Int,
    ) : ChallengeStatement()

    data class Print(
        override val id: String = randomId(),
        val text: String
    ) : ChallengeStatement()

    data class Launch(
        override val id: String = randomId(),
        override val statements: List<ChallengeStatement>
    ) : ChallengeBlock() {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }

    data class ScopeLaunch(
        override val id: String = randomId(),
        override val statements: List<ChallengeStatement>
    ) : ChallengeBlock() {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }

    data class CoroutineScope(
        override val id: String = randomId(),
        override val statements: List<ChallengeStatement>,
    ) : ChallengeBlock() {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }

    data class LaunchJob(
        override val id: String = randomId(),
        override val variableName: String,
        override val statements: List<ChallengeStatement>,
    ) : ChallengeBlock(), WithUsage {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }

    data class Async(
        override val id: String = randomId(),
        override val variableName: String,
        val resultString: String,
        override val statements: List<ChallengeStatement>,
    ) : ChallengeBlock(), WithUsage {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }

    data class Job(
        override val id: String = randomId(),
        override val variableName: String,
    ) : ChallengeStatement(), WithUsage

    data class Join(
        override val id: String = randomId(),
        override val variableName: String
    ) : ChallengeStatement(), Usage

    data class Cancel(
        override val id: String = randomId(),
        override val variableName: String
    ) : ChallengeStatement(), Usage

    data class PrintAwait(
        override val id: String = randomId(),
        override val variableName: String,
        val text: String
    ) : ChallengeStatement(), Usage

    data class CompleteJob(
        override val id: String = randomId(),
        override val variableName: String
    ) : ChallengeStatement(), Usage

    data class ThrowException(
        override val id: String = randomId(),
        val exceptionType: ExceptionType,
    ) : ChallengeStatement()

    data class TryCatch(
        override val id: String = randomId(),
        override val statements: List<ChallengeStatement>,
        val exceptionType: ExceptionType,
    ) : ChallengeBlock() {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }

    enum class ExceptionType {
        CancellationException,
        Exception,
        MyException,
    }

    data class SupervisorScope(
        override val id: String = randomId(),
        override val statements: List<ChallengeStatement>
    ) :
        ChallengeBlock() {
        override fun withStatements(statements: List<ChallengeStatement>): ChallengeBlock =
            copy(statements = statements)
    }
}

fun randomId() = UUID.randomUUID().toString()

fun ChallengeStatement.toCode(): String = when (this) {
    is ChallengeStatement.CoroutineScope -> "coroutineScope {\n${statements.toCodeWithIndent()}\n}"
    is ScopeLaunch -> "backgroundScope.launch {\n${statements.toCodeWithIndent()}\n}"
    is Launch -> "launch {\n${statements.toCodeWithIndent()}\n}"
    is LaunchJob -> "val $variableName = launch {\n${statements.toCodeWithIndent()}\n}"
    is Async -> "val $variableName = async {\n${statements.toCodeWithIndent()}\n    \"$resultString\"\n}"
    is ChallengeStatement.Delay -> "delay($time)"
    is Print -> "println(\"${text}\")"
    is PrintAwait -> "println($variableName.await())"
    is Join -> "$variableName.join()"
    is Cancel -> "$variableName.cancel()"
    is ChallengeStatement.Job -> "val $variableName = Job()"
    is CompleteJob -> "$variableName.complete()"
    is SupervisorScope -> "supervisorScope {\n${statements.toCodeWithIndent()}\n}"
    is TryCatch -> {
        val exception = when (exceptionType) {
            ExceptionType.CancellationException -> "CancellationException"
            ExceptionType.Exception -> "Exception"
            ExceptionType.MyException -> "MyException"
        }
        "try {\n${statements.toCodeWithIndent()}\n} catch (e: $exception) {\n    println(\"Got exception\")\n}"
    }

    is ThrowException -> {
        val exception = when (exceptionType) {
            ExceptionType.CancellationException -> "CancellationException()"
            ExceptionType.Exception -> "Exception()"
            ExceptionType.MyException -> "MyException()"
        }
        "throw $exception"
    }
}

private fun List<ChallengeStatement>.toCodeWithIndent() = joinToString(separator = "\n") {
    it.toCode().prependIndent("    ")
}

data class PrintWithTime(val value: String, val time: Long)

fun ChallengeStatement.getResult(): List<PrintWithTime> = buildList<PrintWithTime> {
    try {
        val jobs = mutableMapOf<String, Job>()
        val deferred = mutableMapOf<String, Deferred<String>>()
        runTest(timeout = 1.seconds) {
            val ignoringHandler = CoroutineExceptionHandler({ _, _ -> })
            val backgroundScope = CoroutineScope(backgroundScope.coroutineContext + SupervisorJob() + ignoringHandler)
            try {
                withContext(ignoringHandler) {
                    withTimeout(10_000_000) {
                        suspend fun evaluate(statement: ChallengeStatement, scope: CoroutineScope) {
                            when (statement) {
                                is ChallengeStatement.CoroutineScope -> coroutineScope {
                                    statement.statements.forEach { evaluate(it, this@coroutineScope) }
                                }

                                is Launch -> scope.launch {
                                    statement.statements.forEach { evaluate(it, this@launch) }
                                }

                                is ScopeLaunch -> backgroundScope.launch {
                                    statement.statements.forEach { evaluate(it, this@launch) }
                                }

                                is LaunchJob -> jobs[statement.variableName] =
                                    scope.launch {
                                        statement.statements.forEach { evaluate(it, this@launch) }
                                    }

                                is Async -> deferred[statement.variableName] =
                                    scope.async {
                                        statement.statements.forEach { evaluate(it, this@async) }
                                        statement.resultString
                                    }

                                is ChallengeStatement.Delay -> delay(statement.time.toLong())
                                is Print -> add(
                                    PrintWithTime(
                                        statement.text,
                                        currentTime
                                    )
                                )

                                is Cancel -> jobs[statement.variableName]?.cancel()
                                is Join -> jobs[statement.variableName]?.join()
                                is PrintAwait -> add(
                                    PrintWithTime(
                                        deferred[statement.variableName]?.await().orEmpty(),
                                        currentTime
                                    )
                                )

                                is ChallengeStatement.Job -> jobs[statement.variableName] = Job()

                                is CompleteJob -> (jobs[statement.variableName] as? CompletableJob)?.complete()

                                is SupervisorScope -> supervisorScope {
                                    statement.statements.forEach { evaluate(it, this@supervisorScope) }
                                }

                                is TryCatch -> when (statement.exceptionType) {
                                    ExceptionType.CancellationException -> try {
                                        statement.statements.forEach { evaluate(it, this) }
                                    } catch (e: CancellationException) {
                                        add(PrintWithTime("Got exception", currentTime))
                                    }

                                    ExceptionType.Exception -> try {
                                        statement.statements.forEach { evaluate(it, this) }
                                    } catch (e: Exception) {
                                        add(PrintWithTime("Got exception", currentTime))
                                    }

                                    ExceptionType.MyException -> try {
                                        statement.statements.forEach { evaluate(it, this) }
                                    } catch (e: MyException) {
                                        add(PrintWithTime("Got exception", currentTime))
                                    }
                                }

                                is ThrowException -> when (statement.exceptionType) {
                                    ExceptionType.CancellationException -> throw GameCancellationException()
                                    ExceptionType.Exception -> throw GameException()
                                    ExceptionType.MyException -> throw MyException()
                                }
                            }
                        }
                        evaluate(this@getResult, this)
                        backgroundScope.cancel()
                        add(PrintWithTime("(done)", currentTime))
                    }
                }
            } catch (t: TimeoutCancellationException) {
                add(PrintWithTime("(waiting forever)", currentTime))
            } catch (npe: GameException) {
                add(PrintWithTime("(exception)", currentTime))
            } catch (npe: MyException) {
                add(PrintWithTime("(exception)", currentTime))
            } catch (npe: GameCancellationException) {
                add(PrintWithTime("(cancellation exception)", currentTime))
            }
        }
    } catch (e: Throwable) {
        println("Exception for ${this@getResult}")
        throw e
    }
}

private class MyException : Exception()
private class GameException : Exception()
private class GameCancellationException : CancellationException("")

fun ChallengeStatement.getStringResult() = getResult()
    .joinToString(separator = "\n") { "[${it.time}] ${it.value}" }

fun ChallengeStatement.getSequentialResult(): List<String> {
    val result = getResult()
    return result
        .windowed(size = 2, step = 1, partialWindows = true)
        .flatMapIndexed { i, window ->
            buildList {
                if (window.size == 2) {
                    val (elem, next) = window
                    if (i == 0 && elem.time != 0L) {
                        add("(${elem.time / 1000} sec)")
                    }
                    add(elem.value)
                    if (elem.time != next.time) {
                        val timeDiffSec = (next.time - elem.time) / 1000
                        add("($timeDiffSec sec)")
                    }
                } else {
                    val (elem) = window
                    if (result.size == 1 && elem.time > 0) add("(${elem.time / 1000} sec)")
                    add(elem.value)
                }
            }
        }
}

class CoroutinesGameValueGenerator(seed: Long = Random.nextLong()) {
    val random = Random(seed)

    val variableNamesInitial = (1..1000).map { "value$it" }
    private val variableNames = ArrayDeque(variableNamesInitial)
    fun nextVariableName() = variableNames.removeFirst()
    fun restartVariableNames(used: Set<String>) {
        variableNames.clear()
        variableNames.addAll(variableNamesInitial)
        variableNames.removeAll(used)
    }

    val jobNamesInitial = (1..1000).map { "job$it" }
    private val jobNames = ArrayDeque(jobNamesInitial)
    fun nextJobName() = jobNames.removeFirst()
    fun restartJobNames(used: Set<String>) {
        jobNames.clear()
        jobNames.addAll(jobNamesInitial)
        jobNames.removeAll(used)
    }

    val stringsInitial = ('A'..'Z').map { it.toString() } + (1..1000).map { "v$it" }
    private val strings = ArrayDeque(stringsInitial)
    fun nextString() = strings.removeFirst()
    fun restartStrings(used: Set<String>) {
        strings.clear()
        strings.addAll(stringsInitial)
        strings.removeAll(used)
    }
}

suspend fun main() = repeat(20) {
    val challenge = generateChallengeBlock(10, CoroutinesRacesDifficulty.WithSynchronization)
    println(challenge.toCode())
    println(challenge.getStringResult())
}
