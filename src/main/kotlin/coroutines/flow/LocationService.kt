package coroutines.flow.locationservice

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LocationService(
    locationRepository: LocationRepository,
    backgroundScope: CoroutineScope,
) {
    fun observeLocation(): Flow<Location> = TODO()

    fun currentLocation(): Location? = TODO()
}

interface LocationRepository {
    fun observeLocation(): Flow<Location>
}

data class Location(val latitude: Double, val longitude: Double)

class LocationObserverTest {

    @Test
    fun `should allow observing location`() = runTest {
        val locationRepository = FakeLocationRepository()
        val locationService = LocationService(locationRepository, backgroundScope)
        var locations = listOf<Location>()
        locationService.observeLocation().onEach {
            locations = locations + it
        }.launchIn(backgroundScope)
        runCurrent()
        assertEquals(0, locations.size)
        locationRepository.emitLocation(Location(11.1, 22.2))
        runCurrent()
        assertEquals(1, locations.size)
    }

    @Test
    fun `should reuse the same connection`() = runTest {
        val locationRepository = FakeLocationRepository()
        val locationService = LocationService(locationRepository, backgroundScope)
        locationService.observeLocation().launchIn(backgroundScope)
        locationService.observeLocation().launchIn(backgroundScope)
        locationService.observeLocation().launchIn(backgroundScope)
        runCurrent()
        kotlin.test.assertEquals(1, locationRepository.observersCount())
    }

    @Test
    fun `should provide current location`() = runTest {
        val locationRepository = FakeLocationRepository()
        val locationService = LocationService(locationRepository, backgroundScope)
        assertEquals(null, locationService.currentLocation())
        runCurrent()
        assertEquals(null, locationService.currentLocation())

        val l1 = Location(1.1, 2.2)
        locationRepository.emitLocation(l1)
        runCurrent()
        assertEquals(l1, locationService.currentLocation())

        val l2 = Location(3.3, 4.4)
        locationRepository.emitLocation(l2)
        runCurrent()
        assertEquals(l2, locationService.currentLocation())

        val l3 = Location(5.5, 6.6)
        locationRepository.emitLocation(l3)
        runCurrent()
        assertEquals(l3, locationService.currentLocation())
    }

    @Test
    fun `should conflate location updates`() = runTest {
        val locationRepository = FakeLocationRepository()
        val locationService = LocationService(locationRepository, backgroundScope)

        var locations = listOf<Location>()
        locationService.observeLocation().onEach {
            delay(1000)
            locations = locations + it
        }.launchIn(backgroundScope)

        runCurrent()
        assertEquals(0, locations.size)

        repeat(100) {
            locationRepository.emitLocation(Location(it.toDouble(), it.toDouble()))
            delay(100)
        }

        assertEquals(
            listOf(
                Location(latitude = 0.0, longitude = 0.0),
                Location(latitude = 9.0, longitude = 9.0),
                Location(latitude = 19.0, longitude = 19.0),
                Location(latitude = 29.0, longitude = 29.0),
                Location(latitude = 39.0, longitude = 39.0),
                Location(latitude = 49.0, longitude = 49.0),
                Location(latitude = 59.0, longitude = 59.0),
                Location(latitude = 69.0, longitude = 69.0),
                Location(latitude = 79.0, longitude = 79.0),
                Location(latitude = 89.0, longitude = 89.0)
            ), locations
        )
    }

    @Test
    fun `should emit only distinct locations`() = runTest {
        val locationRepository = FakeLocationRepository()
        val locationService = LocationService(locationRepository, backgroundScope)
        var locations = listOf<Location>()
        locationService.observeLocation().onEach {
            locations = locations + it
        }.launchIn(backgroundScope)
        runCurrent()
        assertEquals(0, locations.size)
        locationRepository.emitLocation(Location(5.5, 6.6))
        runCurrent()
        assertEquals(1, locations.size)
        locationRepository.emitLocation(Location(5.5, 6.6))
        runCurrent()
        assertEquals(1, locations.size)
        locationRepository.emitLocation(Location(5.5, 6.6))
        runCurrent()
        assertEquals(1, locations.size)
    }
}

class FakeLocationRepository : LocationRepository {
    private val locationFlow = MutableSharedFlow<Location>()

    override fun observeLocation(): Flow<Location> = locationFlow

    suspend fun emitLocation(location: Location) {
        locationFlow.emit(location)
    }

    fun observersCount(): Int = locationFlow.subscriptionCount.value
}
