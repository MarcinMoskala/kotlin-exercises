package functional.project

import java.util.*

interface TokenRepository {
    fun createToken(userId: String, isAdmin: Boolean): String
    fun isAdmin(token: String): Boolean
    fun getUserId(token: String): String?
}

class FakeTokenRepository : TokenRepository {
    // In real the one, data should be encoded in token
    private val tokens = mutableMapOf<String, TokenDataHolder>()

    fun cleanup() {
        tokens.clear()
    }

    fun shouldRespond(token: String, userId: String, isAdmin: Boolean) {
        tokens[token] = TokenDataHolder(userId, isAdmin)
    }

    override fun createToken(userId: String, isAdmin: Boolean): String {
        val token = UUID.randomUUID().toString()
        tokens[token] = TokenDataHolder(userId, isAdmin)
        return token
    }

    override fun isAdmin(token: String): Boolean = tokens[token]?.isAdmin ?: false

    override fun getUserId(token: String): String? = tokens[token]?.userId

    private data class TokenDataHolder(val userId: String, val isAdmin: Boolean)
}
