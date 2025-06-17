package coroutines.starting.evaluationservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EvaluationService(
    val evaluateWithPrimaryEngine: EvaluateWithPrimaryEngineUseCase,
    val evaluateWithSecondaryEngines: EvaluateWithSecondaryEnginesUseCase,
    val backgroundScope: CoroutineScope,
) {
    suspend fun evaluate(orderDetails: OrderDetails): Result<EvaluationResponse> =
        evaluateWithPrimaryEngine(orderDetails)
            .also {
                backgroundScope.launch {
                    evaluateWithSecondaryEngines(orderDetails)
                }
            }
}

class EvaluationServiceTest {

    @Test
    fun `response should not await secondary engine evaluation`() = runTest {
        val primaryEngine = object : EvaluateWithPrimaryEngineUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<EvaluationResponse> {
                delay(1000)
                return Result.success(EvaluationResponse(orderDetails.orderId, "Primary Engine Result"))
            }
        }

        val secondaryEngines = object : EvaluateWithSecondaryEnginesUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<Unit> {
                delay(2000) // Simulate longer secondary engine evaluation
                return Result.success(Unit)
            }
        }

        val service = EvaluationService(primaryEngine, secondaryEngines, backgroundScope)

        val result = service.evaluate(OrderDetails("12345"))

        assert(result.isSuccess)
        assert(result.getOrNull()?.status == "Primary Engine Result")
        assert(currentTime == 1000L)

        backgroundScope.coroutineContext.job.children.forEach { it.join() }
        assert(currentTime == 3000L)
    }
}

interface EvaluateWithPrimaryEngineUseCase {
    suspend operator fun invoke(orderDetails: OrderDetails): Result<EvaluationResponse>
}

interface EvaluateWithSecondaryEnginesUseCase {
    suspend operator fun invoke(orderDetails: OrderDetails): Result<Unit>
}

data class OrderDetails(
    val orderId: String,
)

data class EvaluationResponse(
    val orderId: String,
    val status: String,
)