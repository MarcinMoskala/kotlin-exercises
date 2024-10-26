package coroutines.flow.temperatureservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class TemperatureService(
    private val temperatureDataSource: TemperatureDataSource,
    backgroundScope: CoroutineScope,
) {
    private val lastKnownTemperature =
        ConcurrentHashMap<String, Fahrenheit>()

    fun getWeatherUpdates(city: String): Flow<Fahrenheit> =
        TODO()

    fun getLastKnownWeather(city: String): Fahrenheit? =
        lastKnownTemperature[city]

    fun getAllLastKnownWeather(): Map<String, Fahrenheit> =
        lastKnownTemperature.toMap()

    private fun celsiusToFahrenheit(celsius: Double) =
        Fahrenheit(celsius * 9 / 5 + 32)
}

interface TemperatureDataSource {
    fun getWeatherStream(): Flow<TemperatureData>
}

data class TemperatureData(
    val city: String,
    val temperature: Double,
)

data class Fahrenheit(
    val temperature: Double,
)

@ExperimentalCoroutinesApi
class WeatherServiceTest {

    @Test
    fun `should emit temperature updates in Fahrenheit`() = runTest {
        // given
        val testDataSource = object : TemperatureDataSource {
            override fun getWeatherStream(): Flow<TemperatureData> = flow {
                delay(1)
                emit(TemperatureData("TestCity", 10.0))
                emit(TemperatureData("TestCity2", 20.0))
                delay(1)
                emit(TemperatureData("TestCity", 30.0))
                emit(TemperatureData("TestCity3", 40.0))
                emit(TemperatureData("TestCity2", 50.0))
                delay(1)
            }
        }
        val service = TemperatureService(testDataSource, backgroundScope)

        // when
        val emitted = mutableListOf<Fahrenheit>()
        service.getWeatherUpdates("TestCity")
            .onEach { emitted.add(it) }
            .launchIn(backgroundScope)
        delay(10)

        // then
        assertEquals(listOf(Fahrenheit(50.0), Fahrenheit(86.0)), emitted)
    }

    @Test
    fun `should store last known temperature update in Fahrenheit`() = runTest {
        // given
        val testDataSource = object : TemperatureDataSource {
            override fun getWeatherStream(): Flow<TemperatureData> = flow {
                delay(100)
                emit(TemperatureData("TestCity", 10.0))
                delay(100)
                emit(TemperatureData("TestCity2", 20.0))
                delay(100)
                emit(TemperatureData("TestCity", 30.0))
                delay(100)
                emit(TemperatureData("TestCity3", 40.0))
                delay(100)
                emit(TemperatureData("TestCity2", 50.0))
            }
        }
        val service = TemperatureService(testDataSource, backgroundScope)

        // when
        val emitted = mutableListOf<Fahrenheit>()
        service.getWeatherUpdates("TestCity")
            .onEach { emitted.add(it) }
            .launchIn(backgroundScope)

        delay(150)
        assertEquals(Fahrenheit(50.0), service.getLastKnownWeather("TestCity"))
        assertEquals(Fahrenheit(50.0), service.getAllLastKnownWeather()["TestCity"])

        delay(200)
        assertEquals(Fahrenheit(86.0), service.getLastKnownWeather("TestCity"))
        assertEquals(Fahrenheit(86.0), service.getAllLastKnownWeather()["TestCity"])
    }

    @Test
    fun `should emit last known temperature update on start`() = runTest {
        // given
        val testDataSource = object : TemperatureDataSource {
            override fun getWeatherStream(): Flow<TemperatureData> = flow {
                delay(100)
                emit(TemperatureData("TestCity", 10.0))
                delay(100)
                emit(TemperatureData("TestCity2", 20.0))
            }
        }
        val service = TemperatureService(testDataSource, backgroundScope)
        service.getWeatherUpdates("TestCity").first()
        assertEquals(100, currentTime)

        // when
        val result = service.getWeatherUpdates("TestCity").first()

        // then
        assertEquals(Fahrenheit(50.0), result)
        assertEquals(100, currentTime)

        // when
        val result2 = service.getWeatherUpdates("TestCity2").first()

        // then
        assertEquals(Fahrenheit(68.0), result2)
    }
}
