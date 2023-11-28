package domain.comment

interface UuidProvider {
    fun next(): String
}