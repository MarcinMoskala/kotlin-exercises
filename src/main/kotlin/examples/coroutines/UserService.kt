package examples.coroutines

class UserService(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val emailClient: EmailClient,
) {
    suspend fun registerNewUser(createUserData: CreateUserData): UserProfile {
        val userId = authRepository.createUserAccount(createUserData)
        val profile = userProfileRepository.createProfile(createUserData, userId)
        emailClient.sendWelcomeEmail(
            to = createUserData.email,
            name = profile.displayName
        )
        return profile
    }
}

suspend fun main() {
    val authRepo = object : AuthRepository {
        override suspend fun createUserAccount(createUserData: CreateUserData): String {
            return "user123"
        }
    }

    val userProfileRepo = object : UserProfileRepository {
        override suspend fun createProfile(createUserData: CreateUserData, userId: String): UserProfile {
            return UserProfile(userId, createUserData.displayName, createUserData.email)
        }
    }

    val emailClient = object : EmailClient {
        override suspend fun sendWelcomeEmail(to: String, name: String) {
            println("Welcome email sent to $name at $to")
        }
    }

    val userService = UserService(authRepo, userProfileRepo, emailClient)
    val newUser = userService.registerNewUser(CreateUserData("some@email.com", "password123", "John Doe"))
    println("New user registered: $newUser")
}

interface AuthRepository {
    suspend fun createUserAccount(createUserData: CreateUserData): String
}

interface UserProfileRepository {
    suspend fun createProfile(createUserData: CreateUserData, userId: String): UserProfile
}

interface EmailClient {
    suspend fun sendWelcomeEmail(to: String, name: String)
}

data class CreateUserData(
    val email: String,
    val password: String,
    val displayName: String,
)

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
)