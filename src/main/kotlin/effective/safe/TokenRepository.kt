package effective.safe.tokenrepository

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TokenRepository(
    private val client: TokenClient,
    private val timeProvider: TimeProvider
) {
    private var token: Token? = null

    suspend fun getToken(): Token {
        val currentToken = token
        if (currentToken != null && 
            currentToken.expiration > timeProvider.now()) {
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

class TokenRepositoryTest {
    @Test
    fun `should fetch token first, and then not fetch it if available`() = runTest {
        // given
        var timesFetchTokenCalled = 0
        val token = Token("token", LocalDateTime.now().plusHours(1))
        val client = object : TokenClient {
            override suspend fun fetchToken(): Token {
                delay(1000)
                timesFetchTokenCalled++
                return token
            }
        }
        val timeProvider = object : TimeProvider {
            override fun now(): LocalDateTime = LocalDateTime.now()
        }
        val repository = TokenRepository(client, timeProvider)

        // when fetching token three times
        assertEquals(token, repository.getToken())
        assertEquals(token, repository.getToken())
        assertEquals(token, repository.getToken())

        // then fetchToken should be called only once, and it should take as long as the first call
        assertEquals(1, timesFetchTokenCalled)
        assertEquals(1000L, currentTime)
    }

    @Test
    fun `should fetch token if expired`() = runTest {
        // given
        var i = 1
        val timeProvider = object : TimeProvider {
            override fun now(): LocalDateTime = LocalDateTime.ofEpochSecond(currentTime / 1000, 0, UTC)
        }
        val client = object : TokenClient {
            override suspend fun fetchToken(): Token = Token("token${i++}", timeProvider.now().plusHours(1))
        }
        val repository = TokenRepository(client, timeProvider)

        // when fetching token
        val token = repository.getToken()

        // then token should have the appropriate expiration date
        assertEquals(currentTime / 1000 + 1.hours.inWholeSeconds, token.expiration.toEpochSecond(UTC))

        // when fetching token after expiration
        delay(1.hours + 5.minutes)
        val newToken = repository.getToken()

        // then it is a new token
        assertNotEquals(token, newToken)

        // then new token should have the appropriate expiration date
        assertEquals(currentTime / 1000 + 1.hours.inWholeSeconds, newToken.expiration.toEpochSecond(UTC))
    }

    @Test
    fun `should generate only one token for multiple requests`() = runTest {
        // given
        var i = 1
        var fetchTokenCalled = 0
        val timeProvider = object : TimeProvider {
            override fun now(): LocalDateTime = LocalDateTime.now()
        }
        val client = object : TokenClient {
            override suspend fun fetchToken(): Token {
                fetchTokenCalled++
                delay(1000)
                return Token("token${i++}", timeProvider.now().plusHours(1))
            }
        }
        val repository = TokenRepository(client, timeProvider)

        // when fetching token by multiple coroutines concurrently
        val tokens = coroutineScope {
            (1..10).map { async { repository.getToken() } }
                .awaitAll()
        }

        // then all tokens should be the same
        assertEquals(1, tokens.toSet().size)

        // and fetchToken should be called only once
        assertEquals(1, fetchTokenCalled)

        // and it should take as long as the first call
        assertEquals(1000L, currentTime)
    }

    @Test
    fun `should provide new token after invalidation`() = runTest { 
        // given
        var i = 1
        val timeProvider = object : TimeProvider {
            override fun now(): LocalDateTime = LocalDateTime.now()
        }
        val client = object : TokenClient {
            override suspend fun fetchToken(): Token = Token("token${i++}", timeProvider.now().plusHours(1))
        }
        val repository = TokenRepository(client, timeProvider)

        // when fetching token
        val token = repository.getToken()

        // then token should be the same
        assertEquals(token, repository.getToken())

        // when invalidating token
        repository.invalidateToken()

        // then new token should be different
        assertNotEquals(token, repository.getToken())
    }

    @Test
    fun `should not invalidate token that is currently fetching`() = runTest {
        // given
        var i = 1
        val timeProvider = object : TimeProvider {
            override fun now(): LocalDateTime = LocalDateTime.now()
        }
        val client = object : TokenClient {
            override suspend fun fetchToken(): Token {
                delay(1000)
                return Token("token${i++}", timeProvider.now().plusHours(1))
            }
        }
        val repository = TokenRepository(client, timeProvider)

        // when fetching token
        val tokenAsync = async { 
            repository.getToken()
        }

        // and in-between invalidating token
        delay(500)
        repository.invalidateToken()

        // when fetching token again
        val token2 = repository.getToken()

        // or fetching later
        val token = tokenAsync.await()
        val token3 = repository.getToken()

        // then token should be the same
        assertEquals(token, token2)
        assertEquals(token, token3)
    }
}
