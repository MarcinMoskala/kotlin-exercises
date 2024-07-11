package coroutines.examples

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

suspend fun main() = coroutineScope {
   flowOf("A", "B", "C")
       .onEach  {
           delay(1000)
           println("onEach $it") 
       }
       .buffer(100)
       .collect { 
           delay(1000)
           println("collect $it")
       }
}
