// TODO: Implement CounterContext

//fun main(): Unit = runBlocking(CounterContext()) {
//    println(coroutineContext[CounterContext]?.next()) // 0
//    println(coroutineContext[CounterContext]?.next()) // 1
//    launch {
//        println(coroutineContext[CounterContext]?.next())// 2
//        println(coroutineContext[CounterContext]?.next())// 3
//    }
//    launch(CounterContext()) {
//        println(coroutineContext[CounterContext]?.next())// 0
//        println(coroutineContext[CounterContext]?.next())// 1
//    }
//}

@Suppress("FunctionName")
class CounterContextTests {

// TODO: Uncomment
//
//    @Test
//    fun `should return next numbers in the same coroutine`() = runBlocking<Unit>(CounterContext()) {
//        assertEquals(0, coroutineContext[CounterContext]?.next())
//        assertEquals(1, coroutineContext[CounterContext]?.next())
//        assertEquals(2, coroutineContext[CounterContext]?.next())
//        assertEquals(3, coroutineContext[CounterContext]?.next())
//        assertEquals(4, coroutineContext[CounterContext]?.next())
//    }
//
//    @Test
//    fun `should have independent counter for each instance`() = runBlocking<Unit> {
//        launch(CounterContext()) {
//            assertEquals(0, coroutineContext[CounterContext]?.next())
//            assertEquals(1, coroutineContext[CounterContext]?.next())
//            assertEquals(2, coroutineContext[CounterContext]?.next())
//        }
//        launch(CounterContext()) {
//            assertEquals(0, coroutineContext[CounterContext]?.next())
//            assertEquals(1, coroutineContext[CounterContext]?.next())
//            assertEquals(2, coroutineContext[CounterContext]?.next())
//        }
//    }
//
//    @Test
//    fun `should propagate to the child`() = runBlocking<Unit>(CounterContext()) {
//        assertEquals(0, coroutineContext[CounterContext]?.next())
//        launch {
//            assertEquals(1, coroutineContext[CounterContext]?.next())
//            launch {
//                assertEquals(2, coroutineContext[CounterContext]?.next())
//            }
//            launch(CounterContext()) {
//                assertEquals(0, coroutineContext[CounterContext]?.next())
//                assertEquals(1, coroutineContext[CounterContext]?.next())
//                launch {
//                    assertEquals(2, coroutineContext[CounterContext]?.next())
//                }
//            }
//        }
//    }
}
