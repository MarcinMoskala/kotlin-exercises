package functional.project

interface UserRepository {
    fun getUser(id: String): UserDto?
    fun getUserByKey(key: String): UserDto?
    fun getUserByEmail(email: String): UserDto?
    fun isAvailableKey(key: String): Boolean
    fun updateUser(user: UserDto)
    fun addUser(user: UserDto)
    fun getAllUsers(): List<UserDto>
}

class FakeUserRepository : UserRepository {
    private val users = mutableMapOf<String, UserDto>()
    var checkedKeys = setOf<String>()
        private set
    private var keyNotAvailable = false

    fun cleanup() {
        users.clear()
        checkedKeys = setOf()
        keyNotAvailable = false
    }
    
    fun allKeysAreUnavailable() {
        keyNotAvailable = true
    }

    override fun getUser(id: String): UserDto? = users[id]

    override fun getUserByKey(key: String): UserDto? = users.values.firstOrNull { it.key == key }

    override fun getUserByEmail(email: String): UserDto? = users.values.firstOrNull { it.email == email }

    override fun isAvailableKey(key: String): Boolean {
        checkedKeys += key
        if (keyNotAvailable) return false
        return users.values.none { it.key == key }
    }

    override fun updateUser(user: UserDto) {
        users[user.id] = user
    }

    override fun addUser(user: UserDto) {
        users[user.id] = user
    }

    override fun getAllUsers(): List<UserDto> = users.values.toList()
}
