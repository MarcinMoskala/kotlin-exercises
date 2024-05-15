package effective.safe.companydetailsrepository

import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.time.measureTime

class CompanyDetailsRepository(
    private val client: CompanyDetailsClient,
    dispatcher: CoroutineDispatcher
) {
    private val details = mutableMapOf<Company, CompanyDetails>()

    suspend fun getDetails(company: Company): CompanyDetails {
        val current = getDetailsOrNull(company)
        if (current == null) {
            val companyDetails = client.fetchDetails(company)
            details[company] = companyDetails
            return companyDetails
        }
        return current
    }

    fun getDetailsOrNull(company: Company): CompanyDetails? = 
        details[company]

    fun getReadyDetails(): Map<Company, CompanyDetails> =
        details
    
    fun clear() {
        details.clear()
    }
}

// Run in main
suspend fun performanceTest(): Unit = coroutineScope {
    val companies = (0 until 100_000).map { Company(it.toString()) }
    val client = FakeCompanyDetailsClient(
        details = buildMap {
            companies.forEach { put(it, CompanyDetails("Company${it.id}", "Address${it.id}", BigDecimal.TEN)) }
        }
    )
    val repository = CompanyDetailsRepository(client, Dispatchers.IO)
    val dispatcher = newFixedThreadPoolContext(100, "test")

    // The time of getting and storing details
    measureTime {
        companies.map { async(dispatcher) { repository.getDetails(it) } }.awaitAll()
    }.also {
        val averageTime = it.inWholeNanoseconds / companies.size
        println("Average time of getting details: $averageTime ns")
    }

    // The time of getting details from cache
    measureTime {
        companies.map { async(dispatcher) { repository.getDetailsOrNull(it) } }.awaitAll()
    }.also {
        val averageTime = it.inWholeNanoseconds / companies.size
        println("Average time of getting details from cache: $averageTime ns")
    }

    // The time of getting all details
    val repeats = 1000
    measureTime {
        coroutineScope {
            repeat(repeats) {
                launch(dispatcher) {
                    repository.getReadyDetails()
                }
            }
        }
    }.also {
        val averageTime = it.inWholeNanoseconds / repeats
        println("Time of getting all details: $averageTime ns")
    }
}

interface CompanyDetailsClient {
    suspend fun fetchDetails(company: Company): CompanyDetails
}

data class CompanyDetails(val name: String, val address: String, val revenue: BigDecimal)
data class Company(val id: String)

@OptIn(ExperimentalStdlibApi::class)
class CompanyDetailsRepositoryTest {
    @Test
    fun `detailsFor should fetch details from client`() = runTest {
        // given
        val company = Company("1")
        val details = CompanyDetails("Company", "Address", BigDecimal.TEN)
        val client = FakeCompanyDetailsClient()
        client.setDetails(company, details)
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        // when
        val result = repository.getDetails(company)

        // then
        assertEquals(details, result)
    }

    @Test
    fun `detailsFor should cache details`() = runTest {
        // given
        val company = Company("1")
        val details = CompanyDetails("Company", "Address", BigDecimal.TEN)
        val client = FakeCompanyDetailsClient(
            details = mapOf(company to details),
            delay = 1000
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        // when
        val result1 = repository.getDetails(company)

        // then       
        assertEquals(details, result1)
        assertEquals(1000, currentTime)

        // when
        client.clear()

        // then
        val result = repository.getDetails(company)
        assertEquals(details, result)
    }

    @Test
    fun `getReadyDetails should return all details`() = runTest {
        // given
        val company1 = Company("1")
        val company2 = Company("2")
        val details1 = CompanyDetails("Company1", "Address1", BigDecimal.TEN)
        val details2 = CompanyDetails("Company2", "Address2", BigDecimal.ONE)
        val client = FakeCompanyDetailsClient(
            details = mapOf(company1 to details1, company2 to details2)
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        // when
        repository.getDetails(company1)
        repository.getDetails(company2)
        val result = repository.getReadyDetails()

        // then
        assertEquals(mapOf(company1 to details1, company2 to details2), result)
    }

    @Test
    fun `getReadyDetails should fetch details asynchronously`() = runTest {
        // given
        val company1 = Company("1")
        val company2 = Company("2")
        val details1 = CompanyDetails("Company1", "Address1", BigDecimal.TEN)
        val details2 = CompanyDetails("Company2", "Address2", BigDecimal.ONE)
        val client = FakeCompanyDetailsClient(
            details = mapOf(company1 to details1, company2 to details2),
            delay = 1000,
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        // when
        coroutineScope {
            launch {
                repository.getDetails(company1)
                assertEquals(1000, currentTime)
            }
            launch {
                repository.getDetails(company2)
                assertEquals(1000, currentTime)
            }
        }
        val result = repository.getReadyDetails()

        // then
        assertEquals(mapOf(company1 to details1, company2 to details2), result)
        assertEquals(1000, currentTime)
    }

    @Test
    fun `should not expose internal collection`() = runTest {
        // given
        val company = Company("1")
        val details = CompanyDetails("Company", "Address", BigDecimal.TEN)
        val client = FakeCompanyDetailsClient(
            details = mapOf(company to details)
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)
        val detailsMap = repository.getReadyDetails()

        // when
        repository.getDetails(company)

        // then
        assertEquals(mapOf(), detailsMap)
    }

    @Test
    fun `getDetailsOrNull should return details if exists`() = runTest {
        // given
        val company = Company("1")
        val company2 = Company("2")
        val details = CompanyDetails("Company", "Address", BigDecimal.TEN)
        val client = FakeCompanyDetailsClient(
            details = mapOf(company to details)
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        // then
        assertEquals(null, repository.getDetailsOrNull(company))
        assertEquals(null, repository.getDetailsOrNull(company2))

        // when
        repository.getDetails(company)

        // then
        assertEquals(details, repository.getDetailsOrNull(company))
        assertEquals(null, repository.getDetailsOrNull(company2))
    }

    // Synchronization tests

    @Test
    fun `should cache all details`() = runBlocking(Dispatchers.IO) {
        val parallelCalls = 10_000
        val companies = (0 until parallelCalls).map { Company(it.toString()) }
        val client = FakeCompanyDetailsClient(
            details = buildMap {
                companies.forEach { put(it, CompanyDetails("Company${it.id}", "Address${it.id}", BigDecimal.TEN)) }
            }
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        coroutineScope {
            for (company in companies) {
                launch {
                    val details = repository.getDetails(company)
                    assertEquals(
                        CompanyDetails("Company${company.id}", "Address${company.id}", BigDecimal.TEN),
                        details
                    )
                }
            }
        }

        assertEquals(parallelCalls, repository.getReadyDetails().size)
    }

    @Test
    fun `should not have conflict when changing details and getting all`() = runBlocking(Dispatchers.IO) {
        val parallelCalls = 10_000
        val companies = (0 until parallelCalls).map { Company(it.toString()) }
        val client = FakeCompanyDetailsClient(
            details = buildMap {
                companies.forEach { put(it, CompanyDetails("Company${it.id}", "Address${it.id}", BigDecimal.TEN)) }
            }
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        for (company in companies) {
            launch { repository.getDetails(company) }
        }
        repeat(1000) {
            launch { repository.getReadyDetails() }
        }
    }

    @Test
    @Ignore
    fun `should not fetch the same details twice`() = runTest {
        val company = Company("1")
        val details = CompanyDetails("Company", "Address", BigDecimal.TEN)
        val client = FakeCompanyDetailsClient(
            details = mapOf(company to details),
            delay = 1000
        )
        val repository = CompanyDetailsRepository(client, coroutineContext[CoroutineDispatcher]!!)

        coroutineScope {
            launch { repository.getDetails(company) }
            launch { repository.getDetails(company) }
        }

        assertEquals(1, client.calls)
    }
}

class FakeCompanyDetailsClient(
    details: Map<Company, CompanyDetails> = emptyMap(),
    var delay: Long = 0,
) : CompanyDetailsClient {
    private val details: MutableMap<Company, CompanyDetails> = details.toMutableMap()
    var calls = 0
        private set

    override suspend fun fetchDetails(company: Company): CompanyDetails {
        calls++
        delay(delay)
        return details[company] ?: error("Company not found")
    }

    fun setDetails(company: Company, details: CompanyDetails) {
        this.details[company] = details
    }

    fun clear() {
        details.clear()
        delay = 0
    }
}
