package examples

open class Animal(val name: String)
class Dog(name: String): Animal(name)

fun consumeAnimal(animal: Animal) {
    println("Consuming animal: ${animal.name}")
}
fun consumeDog(dog: Dog) {
    println("Consuming dog: ${dog.name}")
}

fun main() {
    val dog = Dog("Rex")
    consumeDog(dog)
    consumeAnimal(dog)
    
    val animal = Animal("Fido")
    consumeAnimal(animal)
    // consumeDog(animal) // Error: Type mismatch
}
