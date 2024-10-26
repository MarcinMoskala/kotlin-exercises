package examples.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce

//fun main() = runBlocking {
//   val channel = Channel<Int>()
//   launch {
//       repeat(5) { index ->
//           channel.send(index * 2)
//           delay(1000)
//       }
//   }
//
//   repeat(5) {
//       val received = channel.receive()
//       print(received)
//   }
//}

//fun main() = runBlocking {
//   val channel = Channel<Int>()
//   launch {
//       repeat(5) { index ->
//           channel.send(index * 2)
//           delay(1000)
//       }
//       channel.close()
//   }
//
//   for (i in channel) {
//       print(i)
//   }
//}

//fun main() = runBlocking {
//   val channel = produce { 
//       repeat(5) { index ->
//           send(index * 2)
//           delay(1000)
//       }
//   }
//
//    for (i in channel) {
//        print(i)
//    }
//}

//fun main() = runBlocking<Unit> {
//   val channel = Channel<String>(Channel.UNLIMITED)
//
//   launch {
//       repeat(5) {
//           channel.send("Ping $it")
//           println("Message sent")
//       }
//       channel.close()
//   }
//
//   launch {
//       delay(1000)
//       for(text in channel) {
//           println(text)
//           delay(1000)
//       }
//   }
//}

//fun CoroutineScope.produceNumbers() = produce {
//   var x = 1 // start from 1
//   while (true) {
//       send(x++)
//       delay(100)
//   }
//}
//
//fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
//   for (msg in channel) {
//       println("Processor #$id received $msg")
//   }
//}
//
//fun main() = runBlocking {
//   val producer = produceNumbers()
//   repeat(5) { launchProcessor(it, producer) }
//   delay(950)
//   producer.cancel()
//}

//suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
//   while (true) {
//       delay(time)
//       channel.send(s)
//   }
//}
//
//fun main() = runBlocking {
//   val channel = Channel<String>()
//   launch { sendString(channel, "foo", 200L) }
//   launch { sendString(channel, "BAR!", 500L) }
//   repeat(6) {
//       println(channel.receive())
//   }
//   coroutineContext.cancelChildren()
//}

//fun CoroutineScope.produceNumbers() = produce<Int> {
//    var x = 1
//    while (true) send(x++)
//}
//
//fun CoroutineScope.square(
//    numbers: ReceiveChannel<Int>
//): ReceiveChannel<Int> = produce {
//    for (x in numbers) send(x * x)
//}
//
//fun main() = runBlocking {
//    val numbers = produceNumbers()
//    val squares = square(numbers)
//    for (i in 1..5) println(squares.receive())
//    println("Done!")
//    coroutineContext.cancelChildren()
//}
