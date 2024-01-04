package coroutines.flow.observeappointmentsusecase

import kotlinx.coroutines.flow.*
import java.time.Instant

class ObserveAppointmentsUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    fun observeAppointments(): Flow<List<Appointment>> =
        appointmentRepository
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
data class AppointmentUpdate(
    val appointments: List<Appointment>
) : AppointmentEvent()
data object AppointmentConfirmed : AppointmentEvent()
data class Appointment(val title: String, val time: Instant)
data class ApiException(val code: Int) : Throwable()
