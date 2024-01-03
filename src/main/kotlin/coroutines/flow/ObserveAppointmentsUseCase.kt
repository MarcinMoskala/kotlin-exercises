package coroutines.flow.appointments

import kotlinx.coroutines.flow.*
import java.time.Instant

class ObserveAppointmentsUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    // should observe only updates
    // should ignore repeating values
    // should retry API exception
    // should not retry API exception with incorrect code
    // should not retry non-API exceptions
    fun observeAppointments(): Flow<List<Appointment>> = appointmentRepository
        .observeAppointments()
        .filterIsInstance<AppointmentUpdate>()
        .map { it.appointments }
        .distinctUntilChanged()
        .retry { it is ApiException && it.code in 500..599 }
}

interface AppointmentRepository {
    fun observeAppointments(): Flow<AppointmentEvent>
}

sealed class AppointmentEvent
data class AppointmentUpdate(val appointments: List<Appointment>) : AppointmentEvent()
object AppointmentConfirmed : AppointmentEvent()
data class Appointment(val title: String, val time: Instant)
data class ApiException(val code: Int) : Throwable()
