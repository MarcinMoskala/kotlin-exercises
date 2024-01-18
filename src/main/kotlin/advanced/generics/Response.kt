package advanced.generics.response

sealed class Response<R, E>
class Success<R, E>(val value: R) : Response<R, E>()
class Failure<R, E>(val error: E) : Response<R, E>()

fun usage() {
    //val rs1 = Success(1)
    //val rs2 = Success("ABC")
    //val re1 = Failure(Error())
    //val re2 = Failure("Error")
    //
    //val rs3: Success<Number> = rs1
    //val rs4: Success<Any> = rs1
    //val re3: Failure<Throwable> = re1
    //val re4: Failure<Any> = re1
    //
    //val r1: Response<Int, Throwable> = rs1
    //val r2: Response<Int, Throwable> = re1
}
