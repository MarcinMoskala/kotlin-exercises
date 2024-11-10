package effective.safe

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

class TokenRepository(
    private val client: TokenClient,
    private val timeProvider: TimeProvider
) {
    private var token: Token? = null
    private val tokenMutex = Mutex()
    
    suspend fun getToken(): Token = tokenMutex.withLock {
        val currentToken = token
        if (currentToken != null && currentToken.expiration > timeProvider.now()) {
            return currentToken
        }
        val newToken = client.fetchToken()
        token = newToken
        return newToken
    }
    
    fun invalidateToken() {
        token = null
    }
}

data class Token(val value: String, val expiration: LocalDateTime)

interface TimeProvider {
    fun now(): LocalDateTime
}
interface TokenClient {
    suspend fun fetchToken(): Token
}

