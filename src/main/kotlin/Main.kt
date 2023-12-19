import examples.log
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun log(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}

fun CoroutineScope.slog(msg: String) {
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] $msg")
}

suspend fun main() = withContext(CoroutineName("Outer")) {
    log("Starting") // [Outer] Starting
    launch(CoroutineName("Inner")) { 
        slog("A") // [Inner] A
        launch {
            log("B") // [Inner] B
        }
    }
    launch {
        slog("C") // [Outer] C
    }
    GlobalScope.launch {
        log("D") // [null] D
    }
    slog("Ending") // [Outer] Ending
}
