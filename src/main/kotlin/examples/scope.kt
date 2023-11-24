package examples.scope

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.zip.ZipInputStream

fun main() {
    println(listOf("a", "b", "c").map { it.uppercase() }) // [A, B, C]
    println("a".let { it.uppercase() }) // A
    
    val fis = FileInputStream("someFile.gz")
    val bis = BufferedInputStream(fis)
    val gis = ZipInputStream(bis)
    val ois = ObjectInputStream(gis)
    val someObject = ois.readObject()
}

//class CoursesService(
//    private val userRepository: UserRepository,
//    private val coursesRepository: CoursesRepository,
//    private val userCoursesFactory: UserCoursesFactory,
//) {
//    fun getActiveCourses(token: String): UserCourses? {
//        val user = userRepository.getUser(token) ?: return null
//        val activeCourses = coursesRepository
//            .getActiveCourses(user.id) ?: return null
//        return userCoursesFactory.produce(activeCourses)
//    }
//}

//class UserCreationService(
//    private val userRepository: UserRepository,
//    private val idGenerator: IdGenerator,
//) {
//    fun addUser(request: UserCreationRequest): User =
//        request.toUserDto()
//            .also { userRepository.addUser(it) }
//            .toUser()
//
//    // Anti-pattern! Avoid using member extensions
//    private fun UserCreationRequest.toUserDto() = UserDto(
//        userId = idGenerator.generate(),
//        firstName = this.name,
//        lastName = this.surname,
//    )
//}

//class User(val name: String)
//
//var user: User? = null
//
//fun showUserNameIfPresent() {
//    // will not work, because cannot smart-cast a property
//    // if (user != null) {
//    //     println(user.name)
//    // }
//
//    // works
//    // val u = user
//    // if (u != null) {
//    //     println(u.name)
//    // }
//
//    // perfect
//    user?.let { println(it.name) }
//}

//fun addUser(request: UserCreationRequest): User =
//    request.toUserDto()
//        .also { userRepository.addUser(it) }
//        .also { log("User created: $it") }
//        .toUser()
//
//class CachingDatabaseFactory(
//    private val databaseFactory: DatabaseFactory,
//) : DatabaseFactory {
//    private var cache: Database? = null
//
//    override fun createDatabase(): Database = cache
//        ?: databaseFactory.createDatabase()
//            .also { cache = it }
//}

//val scope = CoroutineScope(SupervisorJob())
//with(scope) {
//    launch {
//        // ...
//    }
//    launch {
//        // ...
//    }
//}
//
//// unit-test assertions
//with(user) {
//    assertEquals(aName, name)
//    assertEquals(aSurname, surname)
//    assertEquals(aWebsite, socialMedia?.websiteUrl)
//    assertEquals(aTwitter, socialMedia?.twitter)
//    assertEquals(aLinkedIn, socialMedia?.linkedin)
//    assertEquals(aGithub, socialMedia?.github)
//}
