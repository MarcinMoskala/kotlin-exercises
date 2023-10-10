package essentials.classes

class Product(
    val name: String,
    val price: Double,
    initialQuantity: Int
) {
    var quantity: Int = initialQuantity
        set(value) {
            field = if (value >= 0) value else 0
        }

    fun calculateTotalValue(): Double {
        return price * quantity
    }

    fun restock(additionalQuantity: Int) {
        if (additionalQuantity > 0) {
            quantity += additionalQuantity
        }
    }
}

fun main() {
    val laptop = Product("Laptop", 999.99, 5)

    println(laptop.name) // Laptop
    println(laptop.quantity) // 5
    println(laptop.calculateTotalValue()) // 4999.95

    laptop.restock(3)

    println(laptop.quantity) // 8
    println(laptop.calculateTotalValue()) // 7999.92

    laptop.quantity = -2

    println(laptop.quantity) // 0
    println(laptop.calculateTotalValue()) // 0.0

    laptop.quantity = 10

    println(laptop.quantity) // 10
    println(laptop.calculateTotalValue()) // 9999.9
}
