package coroutines.starting.evaluationservice

import kotlinx.coroutines.*
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
                coroutineScope {
                    launch {
                        evaluateWithSecondaryEngines(orderDetails)
                    }
                }
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

    @Test
    fun `should return failure when primary engine fails`() = runTest {
        // Given
        val primaryEngine = object : EvaluateWithPrimaryEngineUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<EvaluationResponse> {
                delay(500)
                return Result.failure(RuntimeException("Primary engine error"))
            }
        }

        val secondaryEngines = object : EvaluateWithSecondaryEnginesUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<Unit> {
                delay(1000)
                return Result.success(Unit)
            }
        }

        val service = EvaluationService(primaryEngine, secondaryEngines, backgroundScope)

        // When
        val result = service.evaluate(OrderDetails("12345"))

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Primary engine error")
        assert(currentTime == 500L)
    }

    @Test
    fun `should not affect primary result when secondary engine fails`() = runTest {
        // Given
        val primaryEngine = object : EvaluateWithPrimaryEngineUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<EvaluationResponse> {
                delay(1000)
                return Result.success(EvaluationResponse(orderDetails.orderId, "Primary Engine Result"))
            }
        }

        val secondaryEngines = object : EvaluateWithSecondaryEnginesUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<Unit> {
                delay(1500)
                return Result.failure(RuntimeException("Secondary engine error"))
            }
        }

        val service = EvaluationService(primaryEngine, secondaryEngines, backgroundScope)

        // When
        val result = service.evaluate(OrderDetails("12345"))

        // Then
        assert(result.isSuccess)
        assert(result.getOrNull()?.status == "Primary Engine Result")
        assert(currentTime == 1000L)

        backgroundScope.coroutineContext.job.children.forEach { it.join() }
        assert(currentTime == 2500L)
    }

    @Test
    fun `should process different order details correctly`() = runTest {
        // Given
        var capturedOrderId: String? = null

        val primaryEngine = object : EvaluateWithPrimaryEngineUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<EvaluationResponse> {
                capturedOrderId = orderDetails.orderId
                return Result.success(EvaluationResponse(orderDetails.orderId, "Processed ${orderDetails.orderId}"))
            }
        }

        val secondaryEngines = object : EvaluateWithSecondaryEnginesUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<Unit> {
                return Result.success(Unit)
            }
        }

        val service = EvaluationService(primaryEngine, secondaryEngines, backgroundScope)

        // When
        val customOrderId = "CUSTOM-7890"
        val result = service.evaluate(OrderDetails(customOrderId))

        // Then
        assert(result.isSuccess)
        assert(capturedOrderId == customOrderId)
        assert(result.getOrNull()?.orderId == customOrderId)
        assert(result.getOrNull()?.status == "Processed $customOrderId")
    }

    @Test
    fun `should always call secondary engines in background`() = runTest {
        // Given
        var secondaryEnginesCalled = false

        val primaryEngine = object : EvaluateWithPrimaryEngineUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<EvaluationResponse> {
                return Result.success(EvaluationResponse(orderDetails.orderId, "Success"))
            }
        }

        val secondaryEngines = object : EvaluateWithSecondaryEnginesUseCase {
            override suspend fun invoke(orderDetails: OrderDetails): Result<Unit> {
                secondaryEnginesCalled = true
                return Result.success(Unit)
            }
        }

        val service = EvaluationService(primaryEngine, secondaryEngines, backgroundScope)

        // When
        service.evaluate(OrderDetails("12345"))

        // Then
        assert(!secondaryEnginesCalled) // Should not be called immediately

        backgroundScope.coroutineContext.job.children.forEach { it.join() }
        assert(secondaryEnginesCalled) // Should be called after joining
    }
}
