package collections

import java.io.File
import kotlin.system.measureTimeMillis

val file = File("AccessList.yaml")
val yamlReader = YamlReader()

class PrimeAccessListRepository {
    private val accessList: PrimeAccessList = yamlReader.readYaml(file)

    fun isOnAllowList(userId: Long): Boolean = TODO()

    fun isOnDenyList(userId: Long): Boolean = TODO()
}

class PrimeAccessList(
    val entries: List<PrimeAccessEntry>
)

class PrimeAccessEntry(
    val userId: Long,
    val allowList: Boolean,
    val allowListReason: String?,
    val denyList: Boolean,
    val denyListReason: String?
)

fun main() {
    val repo = PrimeAccessListRepository()

    measureTimeMillis {
        for (userId in 1L..10_000L) {
            repo.isOnAllowList(userId)
        }
    }.let { println("Checking 10_000 ids by isOnAllowList took $it") }

    measureTimeMillis {
        for (userId in 1L..10_000L) {
            repo.isOnDenyList(userId)
        }
    }.let { println("Checking 10_000 ids by isOnDenyList took $it") }
}
