package advanced.generics.response

sealed class Response<R, E>
class Success<R, E>(val value: R) : Response<R, E>()
class Failure<R, E>(val error: E) : Response<R, E>()

fun processResponseInt(response: Response<Int, String>) {
    /*...*/
}

fun processResponseString(response: Response<String, Throwable>) {
    /*...*/
}

//fun usage() {
//    processResponseInt(Success<Int>(1))
//    processResponseInt(Failure<String>("ERROR"))
//    processResponseString(Success("ERROR"))
//    processResponseString(Failure(Error("ERROR")))
//
//    val rs1 = Success(1)
//    val re1 = Failure(Error())
//    val re2 = Failure("Error")
//
//    val rs1asNumber: Success<Number> = rs1
//    val rs1asAny: Success<Any> = rs1
//
//    val re1asThrowable: Failure<Throwable> = re1
//    val re1asAny: Failure<Any> = re1
//
//    val r1: Response<Int, Error> = rs1
//    val r2: Response<Int, Error> = re1
//
//    val r3: Response<Int, String> = rs1
//    val r4: Response<Int, String> = re2
//
//    val r5: Response<Any, Throwable> = rs1
//    val r6: Response<Any, Throwable> = re1
//
//    val s = Success(String())
//    val s1: Success<CharSequence> = s
//    val s2: Success<Any> = s
//
//    val e = Failure(Error())
//    val e1: Failure<Throwable> = e
//    val e2: Failure<Any> = e
//}
