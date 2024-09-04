package examples.coroutines.flow

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun main() {
    val f = flow { 
        emit("A")
        delay(1000)
        emit("B")
        delay(1000)
        emit("C")
        delay(1000)
    }
    f.collect { println(it) }
    f.collect { println(it) }
    f.collect { println(it) }
    
//    val f = suspend {
//        println("A")
//        delay(1000)
//        println("B")
//        delay(1000)
//        println("C")
//        delay(1000)
//    }
//    f()
//    f()
//    f()
}












// Flow builders

//fun <T> flowOf(vararg values: T) = flow<T> {
//    for (value in values) {
//        emit(value)
//    }
//}
//
//fun <T> List<T>.asFlow() = flow<T> {
//    for (value in this@asFlow) {
//        emit(value)
//    }
//}
//
//fun <T> (suspend () -> T).asFlow() = flow<T> {
//    val value = this@asFlow()
//    emit(value)
//}

// Flow processing functions

//fun <T> Flow<T>.onEach(action: suspend (T) -> Unit) = flow<T> {
//    collect { value ->
//        action(value)
//        emit(value)
//    }
//}
//
//fun <T, R> Flow<T>.map(transform: suspend (T) -> R) = flow<R> {
//    collect { value ->
//        val newValue = transform(value)
//        emit(newValue)
//    }
//}
//
//fun <T> Flow<T>.filter(predicate: suspend (T) -> Boolean) = flow<T> {
//    collect { value ->
//        if (predicate(value)) {
//            emit(value)
//        }
//    }
//}
//
//fun <T> Flow<T>.onStart(action: suspend () -> Unit) = flow<T> {
//    action()
//    collect { value ->
//        emit(value)
//    }
//}
//
//fun <T> Flow<T>.onCompletion(action: suspend () -> Unit) = flow<T> {
//    try {
//        collect { value ->
//            emit(value)
//        }
//    } finally {
//        action()
//    }
//}

// Flow exceptions

//fun <T> Flow<T>.catch(action: suspend FlowCollector<T>.(Throwable) -> Unit) = flow<T> {
//    try {
//        collect { value ->
//            emit(value)
//        }
//    } catch (e: Throwable) {
//        action(e)
//    }
//}
//
//fun <T> Flow<T>.retry(predicate: (cause: Throwable, attempt: Long)  -> Boolean) = flow {
//    var attempt = 0L
//    while (true) {
//        try {
//            collect { value ->
//                emit(value)
//            }
//            break
//        } catch (cause: Throwable) {
//            if (predicate(cause, attempt)) {
//                attempt++
//            } else {
//                throw cause
//            }
//        }
//    }
//}
