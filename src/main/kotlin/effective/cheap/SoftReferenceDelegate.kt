package cheap

import java.lang.ref.SoftReference
import java.math.BigInteger
import kotlin.reflect.KProperty

private val FIB_CACHE by SoftReferenceDelegate { mutableMapOf<Int, BigInteger>() }

fun fib(n: Int): BigInteger = FIB_CACHE.getOrPut(n) {
    if (n <= 1) BigInteger.ONE else fib(n - 1) + fib(n - 2)
}

class SoftReferenceDelegate<T : Any>(val initialization: () -> T) {
    private var reference: SoftReference<T>? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val stored = reference?.get()
        if (stored != null) return stored
        val new = initialization()
        reference = SoftReference(new)
        return new
    }
}
