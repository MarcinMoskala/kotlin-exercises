package functional.project

import kotlin.time.Duration

class Cache<K, V>(
    // Currently not supported
    val clearAfterWrite: Duration? = null,
    // Currently not supported
    val clearAfterRead: Duration? = null,
    val load: (K) -> V?,
) {
    private val cache: MutableMap<K, V> = mutableMapOf()
    fun store(key: K, value: V) {
        cache[key] = value
    }

    fun get(key: K): V? = cache[key] ?: load(key)?.also { cache[key] = it }
    
    fun clear() {
        cache.clear()
    }
    
    fun remove(key: K) {
        cache.remove(key)
    }
}

fun <K, V> cache(
    init: CacheBuilder<K, V>.() -> Unit,
): Cache<K, V> = CacheBuilder<K, V>().apply(init).build()

class CacheBuilder<K, V> {
    var clearAfterWrite: Duration? = null
    var clearAfterRead: Duration? = null
    var load: ((K) -> V?)? = null
        private set

    fun load(load: (K) -> V?) {
        this.load = load
    }

    fun build(): Cache<K, V> = Cache(clearAfterWrite, clearAfterRead, load ?: error("Load function is required"))
}
