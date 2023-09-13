package essentials.exceptions

fun handleInput() {
    print("Enter the first number: ")
    val num1 = readln().toInt()
    print("Enter an operator (+, -, *, /): ")
    val operator = readln()
    print("Enter the second number: ")
    val num2 = readln().toInt()

    val result = when (operator) {
        "+" -> num1 + num2
        "-" -> num1 - num2
        "*" -> num1 * num2
        "/" -> num1 / num2
        else -> throw IllegalOperatorException(operator)
    }

    println("Result: $result")
}

class IllegalOperatorException(operator: String) :
    Exception("Unknown operator: $operator")

fun main() {
    while (true) {
        // Wrap below function call with try-catching block,
        // and handle possible exceptions.
        handleInput()
    }
}
