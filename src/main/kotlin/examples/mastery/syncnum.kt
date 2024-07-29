package examples.syncnum

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main() {
   var num = 0
   coroutineScope {
       for (i in 1..1000) {
           launch {
               delay(10)
               num++
           }
       }
   }
   print(num)
}
