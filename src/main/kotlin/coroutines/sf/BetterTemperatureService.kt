package coroutines.sf.bettertemperatureservice

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

    fun observeTemperature(city: String): Flow<Fahrenheit> =
        temperatureDataSource.observeTemperatureUpdates()
            .filter { it.city == city }
            .map { celsiusToFahrenheit(it.temperature) }
            .onEach { lastKnownTemperature[city] = it }
            .onStart { lastKnownTemperature[city]?.let { emit(it) } }

    fun getLastKnown(city: String): Fahrenheit? =
        lastKnownTemperature[city]

    fun getAllLastKnown(): Map<String, Fahrenheit> =
        lastKnownTemperature.toMap()

    private fun celsiusToFahrenheit(celsius: Double) =
        Fahrenheit(celsius * 9 / 5 + 32)
}

interface TemperatureDataSource {
    fun observeTemperatureUpdates(): Flow<TemperatureData>
}

data class TemperatureData(
    val city: String,
    val temperature: Double,
)

data class Fahrenheit(
    val temperature: Double,
)

@ExperimentalCoroutinesApi
class TemperatureServiceTest {

    @Test
    fun `should emit temperature updates in Fahrenheit`() = runTest {
        // given
        val testDataSource = object : TemperatureDataSource {
            override fun observeTemperatureUpdates(): Flow<TemperatureData> = flow {
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
        service.observeTemperature("TestCity")
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
            override fun observeTemperatureUpdates(): Flow<TemperatureData> = flow {
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
        service.observeTemperature("TestCity")
            .onEach { emitted.add(it) }
            .launchIn(backgroundScope)

        delay(150)
        assertEquals(Fahrenheit(50.0), service.getLastKnown("TestCity"))
        assertEquals(Fahrenheit(50.0), service.getAllLastKnown()["TestCity"])

        delay(200)
        assertEquals(Fahrenheit(86.0), service.getLastKnown("TestCity"))
        assertEquals(Fahrenheit(86.0), service.getAllLastKnown()["TestCity"])
    }

    @Test
    fun `should emit last known temperature update on start`() = runTest {
        // given
        val testDataSource = object : TemperatureDataSource {
            override fun observeTemperatureUpdates(): Flow<TemperatureData> = flow {
                delay(100)
                emit(TemperatureData("TestCity", 10.0))
                delay(100)
                emit(TemperatureData("TestCity2", 20.0))
            }
        }
        val service = TemperatureService(testDataSource, backgroundScope)
        service.observeTemperature("TestCity").first()
        assertEquals(100, currentTime)

        // when
        val result = service.observeTemperature("TestCity").first()

        // then
        assertEquals(Fahrenheit(50.0), result)
        assertEquals(100, currentTime)

        // when
        val result2 = service.observeTemperature("TestCity2").first()

        // then
        assertEquals(Fahrenheit(68.0), result2)
    }

    @Test
    fun `should always have only one temperature updates observer`() = runTest {
        // given
        val temperatureUpdatesSource = MutableSharedFlow<TemperatureData>()
        var observersCounter = 0
        val testDataSource = object : TemperatureDataSource {
            override fun observeTemperatureUpdates(): Flow<TemperatureData> = temperatureUpdatesSource
                .onStart { observersCounter++ }
                .onCompletion { observersCounter++ }
        }
        val service = TemperatureService(testDataSource, backgroundScope)

        // then
        delay(10)
        assertEquals(1, observersCounter)

        // when
        val emitted = mutableListOf<Fahrenheit>()
        val jobs = (1..10).map {
            service.observeTemperature("TestCity$it")
                .onEach { emitted.add(it) }
                .launchIn(backgroundScope)
        }
        delay(10)

        // then
        assertEquals(1, observersCounter)

        // when
        jobs.forEach { it.cancel() }
        delay(10)

        // then
        assertEquals(1, observersCounter)
    }

    @Test
    fun `should store temperature updates even if a city is not observed`() = runTest {
        // given
        val temperatureUpdatesSource = MutableSharedFlow<TemperatureData>()
        var observersCounter = 0
        val testDataSource = object : TemperatureDataSource {
            override fun observeTemperatureUpdates(): Flow<TemperatureData> = temperatureUpdatesSource
                .onStart { observersCounter++ }
                .onCompletion { observersCounter++ }
        }
        val service = TemperatureService(testDataSource, backgroundScope)
        runCurrent()

        // when
        temperatureUpdatesSource.emit(TemperatureData("A", 10.0))
        temperatureUpdatesSource.emit(TemperatureData("B", 20.0))
        temperatureUpdatesSource.emit(TemperatureData("C", 30.0))
        temperatureUpdatesSource.emit(TemperatureData("D", 40.0))
        delay(10)

        // then
        assertEquals(Fahrenheit(temperature = 50.0), service.getLastKnown("A"))
        assertEquals(Fahrenheit(temperature = 68.0), service.getLastKnown("B"))
        assertEquals(Fahrenheit(temperature = 86.0), service.getLastKnown("C"))
        assertEquals(Fahrenheit(temperature = 104.0), service.getLastKnown("D"))
        assertEquals(
            mapOf(
                "A" to Fahrenheit(temperature = 50.0),
                "B" to Fahrenheit(temperature = 68.0),
                "C" to Fahrenheit(temperature = 86.0),
                "D" to Fahrenheit(temperature = 104.0)
            ), service.getAllLastKnown()
        )
    }
}
