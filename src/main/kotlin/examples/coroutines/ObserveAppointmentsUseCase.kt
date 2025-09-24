@file:OptIn(ExperimentalTime::class)

import app.cash.turbine.turbineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ObserveAppointmentsService(
    private val appointmentRepository: AppointmentRepository
) {
    fun observeAppointments(): Flow<List<Appointment>> =
        appointmentRepository
            .observeAppointments()
            .filterIsInstance<AppointmentsUpdate>()
            .map { it.appointments }
            .distinctUntilChanged()
            .retry { it is ApiException && it.code in 500..599 }
}

interface AppointmentRepository {
    fun observeAppointments(): Flow<AppointmentEvent>
}

sealed class AppointmentEvent
data class AppointmentsUpdate(val appointments: List<Appointment>) : AppointmentEvent()
object AppointmentsConfirmed : AppointmentEvent()

data class Appointment(
    val id: String,
    val date: Instant
)
class ApiException(val code: Int, message: String = ""): Exception(message)

class FakeAppointmentRepository(
    private val flow: Flow<AppointmentEvent>
) : AppointmentRepository {
    override fun observeAppointments() = flow
}

class ObserveAppointmentsServiceTest {
    val aDate1 = Instant.parse("2020-08-30T18:43:00Z")
    val anAppointment1 = Appointment("APP1", aDate1)
    val aDate2 = Instant.parse("2020-08-31T18:43:00Z")
    val anAppointment2 = Appointment("APP2", aDate2)

    @Test
    fun `should emit appointments from updates`() = runTest {
        // given
        val repo = FakeAppointmentRepository(
            flowOf(
                AppointmentsConfirmed,
                AppointmentsUpdate(listOf(anAppointment1)),
                AppointmentsUpdate(listOf(anAppointment2)),
                AppointmentsConfirmed,
            )
        )
        val service = ObserveAppointmentsService(repo)

        // when
        val result = service.observeAppointments().toList()

        // then
        assertEquals(
            listOf(
                listOf(anAppointment1),
                listOf(anAppointment2),
            ),
            result
        )
    }

    @Test
    fun `should eliminate adjacent duplicates`() = runTest {
        // given
        val repo = FakeAppointmentRepository(flow {
            delay(1000)
            emit(AppointmentsUpdate(listOf(anAppointment1)))
            emit(AppointmentsUpdate(listOf(anAppointment1)))
            delay(1000)
            emit(AppointmentsUpdate(listOf(anAppointment2)))
            delay(1000)
            emit(AppointmentsUpdate(listOf(anAppointment2)))
            emit(AppointmentsUpdate(listOf(anAppointment1)))
        })
        val service = ObserveAppointmentsService(repo)

        // when
        val result = service.observeAppointments()
            .map { currentTime to it }
            .toList()

        // then
        assertEquals(
            listOf(
                1000L to listOf(anAppointment1),
                2000L to listOf(anAppointment2),
                3000L to listOf(anAppointment1),
            ), result
        )
    }

    @Test
    fun `should retry if there is an API exception with the code 5XX`() = runTest {
        // given
        var observerNumber = 0
        val source = MutableSharedFlow<Result<AppointmentEvent>>()
        val repo = FakeAppointmentRepository(
            source.onSubscription { observerNumber++ }
                .map { it.getOrThrow() }
        )
        var result: List<Result<List<Appointment>>> = emptyList()
        val service = ObserveAppointmentsService(repo)
        val job = service.observeAppointments()
            .onEach { result += Result.success(it) }
            .catch { result += Result.failure(it) }
            .launchIn(backgroundScope)
        runCurrent()

        // when API exception with code 5XX is thrown
        source.emit(Result.failure(ApiException(502)))
        runCurrent()

        // then should have retried
        assertEquals(2, observerNumber)
        assertEquals(emptyList(), result)
        assert(!job.isCompleted)

        // when new appointment update is emitted
        source.emit(Result.success(AppointmentsUpdate(listOf(anAppointment1))))
        runCurrent()

        // then should have received the new appointment
        assertEquals(listOf(Result.success(listOf(anAppointment1))), result)
        result = emptyList()

        // when API exception with code 4XX is thrown
        source.emit(Result.failure(ApiException(404)))
        runCurrent()

        // then should not have retried
        assertEquals(2, observerNumber)
        assertEquals(listOf(Result.failure(ApiException(404))), result)
        result = emptyList()

        // and new appointment update is ignored
        source.emit(Result.success(AppointmentsUpdate(listOf(anAppointment2))))
        runCurrent()
        assertEquals(emptyList(), result)
        assert(job.isCompleted)
    }

    @Test
    fun `(turbine) should retry if there is an API exception with the code 5XX`() = runTest {
        turbineScope {
            // given
            var observerNumber = 0
            val source = MutableSharedFlow<Result<AppointmentEvent>>()
            val repo = FakeAppointmentRepository(
                source.onSubscription { observerNumber++ }
                    .map { it.getOrThrow() }
            )
            val service = ObserveAppointmentsService(repo)
            val turbine = service.observeAppointments()
                .testIn(backgroundScope)
            runCurrent()

            // when API exception with code 5XX is thrown
            source.emit(Result.failure(ApiException(502)))
            runCurrent()

            // then should have retried
            assertEquals(2, observerNumber)
            turbine.expectNoEvents()

            // when new appointment update is emitted
            source.emit(Result.success(AppointmentsUpdate(listOf(anAppointment1))))
            runCurrent()

            // then should have received the new appointment
            assertEquals(listOf(anAppointment1), turbine.awaitItem())
            turbine.expectNoEvents()

            // when API exception with code 4XX is thrown
            source.emit(Result.failure(ApiException(404)))
            runCurrent()

            // then should not have retried
            assertEquals(2, observerNumber)
            assertEquals(ApiException(404), turbine.awaitError())
            turbine.ensureAllEventsConsumed()

            // and new appointment update is ignored
            source.emit(Result.success(AppointmentsUpdate(listOf(anAppointment2))))
            runCurrent()
            turbine.ensureAllEventsConsumed()
        }
    }
}