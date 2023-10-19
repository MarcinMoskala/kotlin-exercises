package functional.project

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: String,
    val key: String,
    val email: String,
    val name: String,
    val surname: String,
    val isAdmin: Boolean,
    val creationTime: LocalDateTime,
)

data class UserDto(
    val id: String,
    val key: String,
    val email: String,
    val name: String,
    val surname: String,
    val isAdmin: Boolean,
    val passwordHash: String,
    val creationTime: LocalDateTime,
)

fun User.toUserDto(passwordHash: String): UserDto = UserDto(
    id = id,
    key = key,
    email = email,
    name = name,
    surname = surname,
    isAdmin = isAdmin,
    passwordHash = passwordHash,
    creationTime = creationTime,
)

fun UserDto.toUser(): User = User(
    id = id,
    key = key,
    email = email,
    name = name,
    surname = surname,
    isAdmin = isAdmin,
    creationTime = creationTime,
)

data class AddUser(
    val email: String,
    val name: String,
    val surname: String,
    val passwordHash: String,
)

data class UserPatch(
    val email: String?,
    val name: String?,
    val surname: String?,
)

data class UserStatistics(
    val numberOfUsersCreatedEachDay: Map<LocalDate, Int>,
)
