// https://en.wikipedia.org/wiki/Greatest_common_divisor
fun gcd(a: Int, b: Int): Int = TODO()

// fizzBuzz function that returns String that represents what should be said in the FizzBuzz game for each number between 1 and 100.
// We list all this numbers in new lines, but we replace some of them with:
// “Fizz” if number is divisible by 3
// “Buzz” if number is divisible by 5
// “FizzBuzz” if number is divisible both by 3 and 5 (by 15)
// Print elements using `console.println`
fun fizzBuzz(console: Console = PrintingConsole) {
    TODO()
}

interface Console {
    fun println(text: String)
}

object PrintingConsole : Console {
    override fun println(text: String) {
        kotlin.io.println(text)
    }
}

// Fibonacci number that starts from 1 and 1 (fib(0) == 1, fib(1) == 1, fib(2) == 2, fib(3) == 3, fib(4) == 5, fib(5) == 8)
// https://en.wikipedia.org/wiki/Fibonacci_number
fun fib(n: Int): Int = TODO()
