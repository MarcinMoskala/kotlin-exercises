package coroutines.starting.biggestcompanyservice

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BiggestCompanyService(
    private val companyRepository: CompanyRepository,
    private val backgroundScope: CoroutineScope,
) {
    private val _companyWithHighestRevenue = MutableStateFlow<CompanyDetails?>(null)
    val companyWithHighestRevenue = _companyWithHighestRevenue

    fun updateHighestRevenueCompany() {
        // TODO
    }
}

interface CompanyRepository {
    suspend fun getCompanyList(): List<Company>
    suspend fun getCompanyDetails(id: String): CompanyDetails
}

data class Company(val id: String, val name: String)
data class CompanyDetails(val id: String, val name: String, val revenue: Double, val employees: Int)

class BiggestCompanyServiceTest {

    @Test
    fun `should update company with highest revenue`() {
        val highestRevenueCompany = CompanyDetails("2", "Company B", 1000.0, 100)
        val repo = ImmediateFakeCompanyRepo(
            listOf(
                Company("1", "Company A"),
                Company("2", "Company B"),
                Company("3", "Company C")
            ),
            listOf(
                CompanyDetails("1", "Company A", 500.0, 50),
                highestRevenueCompany,
                CompanyDetails("3", "Company C", 750.0, 75)
            )
        )
        val testScope = TestScope()
        val service = BiggestCompanyService(repo, testScope)

        service.updateHighestRevenueCompany()
        testScope.advanceUntilIdle() // Process all coroutines

        assertEquals(highestRevenueCompany, service.companyWithHighestRevenue.value)
    }

    @Test
    fun `should handle empty company list`() {
        // given
        val repo = ImmediateFakeCompanyRepo(emptyList(), emptyList())
        val testScope = TestScope()
        val service = BiggestCompanyService(repo, testScope)

        // when
        service.updateHighestRevenueCompany()
        testScope.advanceUntilIdle() // Process all coroutines

        // then
        assertNull(service.companyWithHighestRevenue.value)
    }

    @Test
    fun `should fetch company details asynchronously`() {
        // given
        val repo = DelayedFakeCompanyRepo()
        val testScope = TestScope()
        val service = BiggestCompanyService(repo, testScope)

        // when
        service.updateHighestRevenueCompany()

        testScope.advanceTimeBy(DelayedFakeCompanyRepo.GET_COMPANY_LIST_DELAY)

        assertEquals(1, repo.getListCallCount)
        assertEquals(0, repo.getDetailsCallCount)

        testScope.advanceTimeBy(DelayedFakeCompanyRepo.GET_COMPANY_DETAILS_DELAY)

        assertEquals(1, repo.getListCallCount)

        assert(repo.getDetailsCallCount != 1) {
            "Details are fetched synchronously, but they should be fetched asynchronously, use async!"
        }
        assertEquals(3, repo.getDetailsCallCount, "After that time all details should be fetched")

        testScope.advanceUntilIdle()
        assertEquals(DelayedFakeCompanyRepo.GET_COMPANY_LIST_DELAY + DelayedFakeCompanyRepo.GET_COMPANY_DETAILS_DELAY, testScope.currentTime)
    }
}

class ImmediateFakeCompanyRepo(
    private val companies: List<Company>,
    private val companyDetails: List<CompanyDetails>
) : CompanyRepository {

    override suspend fun getCompanyList(): List<Company> = companies

    override suspend fun getCompanyDetails(id: String): CompanyDetails =
        companyDetails.first { it.id == id }
}

class DelayedFakeCompanyRepo : CompanyRepository {
    var getDetailsCallCount = 0
    var getListCallCount = 0

    override suspend fun getCompanyList(): List<Company> {
        getListCallCount++
        delay(DelayedFakeCompanyRepo.GET_COMPANY_LIST_DELAY)
        return listOf(
            Company("1", "Company A"),
            Company("2", "Company B"),
            Company("3", "Company C")
        )
    }

    override suspend fun getCompanyDetails(id: String): CompanyDetails {
        getDetailsCallCount++
        delay(DelayedFakeCompanyRepo.GET_COMPANY_DETAILS_DELAY)
        return when(id) {
            "1" -> CompanyDetails("1", "Company A", 500.0, 50)
            "2" -> CompanyDetails("2", "Company B", 1000.0, 100)
            "3" -> CompanyDetails("3", "Company C", 750.0, 75)
            else -> throw IllegalArgumentException("Unknown company id: $id")
        }
    }

    companion object {
        const val GET_COMPANY_LIST_DELAY = 200L
        const val GET_COMPANY_DETAILS_DELAY = 100L
    }
}
