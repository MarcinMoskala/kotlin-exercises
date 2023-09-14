package cheap

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//fun <T> mutableLazy(initializer: () -> T): ReadWriteProperty<Any?, T> = MutableLazy(initializer)
//
//private class MutableLazy<T>(val initializer: () -> T) : ReadWriteProperty<Any?, T> {
//
//    private var value: T? = null
//    private var initialized = false
//
//    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        if (!initialized) {
//            value = initializer()
//            initialized = true
//        }
//        return value as T
//    }
//
//    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        this.value = value
//        initialized = true
//    }
//}
