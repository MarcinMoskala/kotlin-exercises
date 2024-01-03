package coroutines.flow.locationservice

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
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

class Location

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
        locationRepository.emitLocation(Location())
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

        val l1 = Location()
        locationRepository.emitLocation(l1)
        runCurrent()
        assertEquals(l1, locationService.currentLocation())

        val l2 = Location()
        locationRepository.emitLocation(l2)
        runCurrent()
        assertEquals(l2, locationService.currentLocation())

        val l3 = Location()
        locationRepository.emitLocation(l3)
        runCurrent()
        assertEquals(l3, locationService.currentLocation())
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
