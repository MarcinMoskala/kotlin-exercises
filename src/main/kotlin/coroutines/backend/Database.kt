package backend

import kotlinx.coroutines.delay

interface Database {
    suspend fun getUsers(): List<User>
    suspend fun addUser(user: User)
}

class DatabaseImpl : Database {

    var users = listOf<User>()

    override suspend fun getUsers(): List<User> {
        delay(500)
        return users
    }

    override suspend fun addUser(user: User) {
        delay(500)
        users = users + user
    }
}