package coroutines.flow.weatherservice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherService(
    private val weatherDataSource: WeatherDataSource
) {
    var lastWeatherUpdate: WeatherUpdate? = null
        private set

    fun getWeatherUpdates(city: String): Flow<WeatherUpdate> =
        TODO()

    fun celsiusToFahrenheit(celsius: Double): Double = 
        celsius * 9 / 5 + 32

    private fun dataToUpdate(
        city: String, 
        weatherData: WeatherData
    ): WeatherUpdate = WeatherUpdate.Success(
      city = city, 
      temperature = celsiusToFahrenheit(weatherData.temperature)
    )
}

interface WeatherDataSource {
    fun getWeatherStream(city: String): Flow<WeatherData>
}

data class WeatherData(val temperature: Double)
sealed class WeatherUpdate {
    object Loading : WeatherUpdate()
    data class Success(val city: String, val temperature: Double) : WeatherUpdate()
}

@ExperimentalCoroutinesApi
class WeatherServiceTest {

    @Test
    fun `should emit Loading at the start`(): Unit = runTest {
        val testDataSource = object : WeatherDataSource {
            override fun getWeatherStream(city: String): Flow<WeatherData> = flow { }
        }
        val service = WeatherService(testDataSource)

        val result = service.getWeatherUpdates("TestCity").toList()

        assertEquals(listOf(WeatherUpdate.Loading), result)
    }

    @Test
    fun `should filter out temperatures less than or equal to 0`(): Unit = runTest {
        val testDataSource = object : WeatherDataSource {
            override fun getWeatherStream(city: String): Flow<WeatherData> = flow {
                emit(WeatherData(-5.0))
                emit(WeatherData(0.0))
                emit(WeatherData(5.0))
            }
        }
        val service = WeatherService(testDataSource)

        val result = service.getWeatherUpdates("TestCity").toList()

        val expected = listOf(
            WeatherUpdate.Loading,
            WeatherUpdate.Success("TestCity", 41.0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should map temperature from Celsius to Fahrenheit`(): Unit = runTest {
        val testDataSource = object : WeatherDataSource {
            override fun getWeatherStream(city: String): Flow<WeatherData> = flow {
                emit(WeatherData(10.0))
            }
        }
        val service = WeatherService(testDataSource)

        val result = service.getWeatherUpdates("TestCity").toList()

        val expected = listOf(
            WeatherUpdate.Loading,
            WeatherUpdate.Success("TestCity", 50.0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should update lastWeatherUpdate while values are still emitted`(): Unit = runTest {
        val testDataSource = object : WeatherDataSource {
            override fun getWeatherStream(city: String): Flow<WeatherData> = flow {
                emit(WeatherData(10.0))
                delay(1000)
                emit(WeatherData(20.0))
            }
        }
        val service = WeatherService(testDataSource)

        service.getWeatherUpdates("TestCity").launchIn(backgroundScope)
        delay(500)
        assertEquals(WeatherUpdate.Success("TestCity", 50.0), service.lastWeatherUpdate)
    }

    @Test
    fun `should reset lastWeatherUpdate on completion`(): Unit = runTest {
        val testDataSource = object : WeatherDataSource {
            override fun getWeatherStream(city: String): Flow<WeatherData> = flow {
                emit(WeatherData(10.0))
            }
        }
        val service = WeatherService(testDataSource)

        service.getWeatherUpdates("TestCity").collect()

        assertNull(service.lastWeatherUpdate)
    }
}
