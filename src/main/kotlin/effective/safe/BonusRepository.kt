package safe

import org.junit.Before
import org.junit.Test
import safe.BonusRepository.User
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

// This repo is incomplete and full of errors. Improve it
class BonusRepository(
    private val bonusesService: BonusesService
) {
    private val users: MutableSet<User> = mutableSetOf()
    private val bonuses: MutableMap<User, MutableList<String>> = mutableMapOf()

    fun addUser(user: User) {
        users += user
    }

    operator fun contains(user: User) = user in users

    fun changeUserSurname(userId: Int, newSurname: String?) {
        users.first { it.id == userId }.surname = newSurname
    }

    fun addBonus(user: User, bonus: String) {
        bonuses[user] = bonuses[user]?.toMutableList()?.apply { add(bonus) }
            ?: mutableListOf(bonus)
        bonusesService.update(bonuses)
    }

    fun removeBonus(user: User, bonus: String) {
        bonuses[user] = bonuses[user]?.toMutableList()?.apply { remove(bonus) }
            ?: mutableListOf()
        bonusesService.update(bonuses)
    }

    fun updateBonus(user: User, oldBonus: String, newBonus: String) {
        bonuses[user] = bonuses[user]?.toMutableList()?.apply {
            remove(oldBonus)
            add(newBonus)
        } ?: mutableListOf(newBonus)
        bonusesService.update(bonuses)
    }

    fun bonusesOf(user: User) = bonuses[user]!!

    data class User(
        var id: Int,
        var surname: String?,
        var name: String?
    )
}

fun main() {
    val bonusRepository = BonusRepository(
        bonusesService = PrintingBonusesService
    )
    val user1 = User(0, "Moskała", "Marcin")
    val user2 = User(1, "Kowalski", "Jan")
    bonusRepository.addUser(user1)
    bonusRepository.addUser(user2)
    // ...
}

interface BonusesService {
    fun update(bonuses: Map<User, List<String>>)
}

object PrintingBonusesService : BonusesService {
    override fun update(bonuses: Map<User, List<String>>) {
        print("Bonuses changed to $bonuses")
    }
}

class BonusRepositoryTest {
    private val user1 = User(0, "Moskała", "Marcin")
    private val user1withSurnameChanged = User(0, "Aaron", "Marcin")
    private val bonus1 = "Bonus1"
    private val bonus2 = "Bonus2"

    lateinit var bonusService: FakeBonusesService
    lateinit var repo: BonusRepository
    
    @Before
    fun setUp() {
        bonusService = FakeBonusesService()
        repo = BonusRepository(bonusService)
    }
    
    @Test
    fun `When user is added, it can be found with contains`() {
        val user1 = user1.copy()
        assert(user1 !in repo)
        repo.addUser(user1)
        assert(user1 in repo)
    }

    @Test
    fun `User exists after surname change`() {
        val user1 = user1.copy() // We need it because User is mutable
        val user1withSurnameChanged = user1withSurnameChanged.copy() // We need it because User is mutable
        repo.addUser(user1.copy())
        repo.addUser(User(0, "BBB", "AAA"))
        repo.addUser(User(1, "CCC", "DDD"))
        assert(user1.copy() in repo)
        assertNotEquals(user1, user1withSurnameChanged, "Values should be different, actual $user1 and $user1withSurnameChanged")
        assertNotEquals(user1.hashCode(), user1withSurnameChanged.hashCode(), "Values should be different, actual $user1 and $user1withSurnameChanged")

        repo.changeUserSurname(user1.id, user1withSurnameChanged.surname)
        assert(user1withSurnameChanged in repo) { "No user $user1withSurnameChanged in the repository $repo" }
    }

    @Test
    fun `Bonus add, remove and update test`() {
        val user1 = user1.copy() // We need it because User is mutable
        repo.addUser(user1)

        repo.addBonus(user1, bonus1)
        assert(bonus1 in repo.bonusesOf(user1))
    }

    @Test
    fun `Repo is not modified when we change bonuses`() {
        val user1 = user1.copy() // We need it because User is mutable
        repo.addUser(user1)

        repo.addBonus(user1, bonus1)
        assert(bonus1 in repo.bonusesOf(user1))

        repo.updateBonus(user1, bonus1, bonus2)
        assert(bonus1 !in repo.bonusesOf(user1))
        assert(bonus2 in repo.bonusesOf(user1))

        repo.removeBonus(user1, bonus1)
        assert(bonus1 !in repo.bonusesOf(user1))
    }

    @Test
    fun `Whenever bonuses are modified, BonusesService is notified`() {
        val user1 = user1.copy() // We need it because User is mutable
        var lastUpdatedBonuses = mapOf<User, List<String>>()
        repo.addUser(user1)
        repo.addBonus(user1, bonus1)
        assertEquals(mapOf(user1 to listOf(bonus1)), lastUpdatedBonuses)
        repo.updateBonus(user1, bonus1, bonus2)
        assertEquals(mapOf(user1 to listOf(bonus2)), lastUpdatedBonuses)
        repo.removeBonus(user1, bonus2)
        assertEquals(mapOf(user1 to listOf<String>()), lastUpdatedBonuses)
    }

    @Test
    fun `Bonuses are preserved after user surname change`() {
        val user1 = user1.copy() // We need it because user is mutable
        val user1withSurnameChanged = user1withSurnameChanged.copy() // We need it because user is mutable
        repo.addUser(user1)
        repo.addBonus(user1, bonus1)
        repo.changeUserSurname(user1.id, user1withSurnameChanged.surname)
        assert(user1withSurnameChanged in repo) { "Name change does not work correctly" }
        assert(bonus1 in repo.bonusesOf(user1withSurnameChanged)) {
            "Bonus is not preserved through surname change"
        }
    }

    @Test
    fun `User class should be read-only`() {
        val members = User::class.members
        members.filterIsInstance<KProperty<*>>()
            .forEach {
                assert(it !is KMutableProperty<*>) { "Property ${it.name} is not final" }
            }
    }
    
    class FakeBonusesService : BonusesService {
        var lastUpdatedBonuses = mapOf<User, List<String>>()
        override fun update(bonuses: Map<User, List<String>>) {
            lastUpdatedBonuses = bonuses
        }
    }
}
