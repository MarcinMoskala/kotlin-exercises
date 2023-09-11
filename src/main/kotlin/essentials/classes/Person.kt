package essentials.classes

interface Person {
    val name: String
    val age: Int
    val canBuyAlcohol: Boolean

    fun helloText(): String

    fun cheerText(person: Person): String
}

fun main(args: Array<String>) {
//    val businessman: Person = Businessman("John", 30)
//    val student: Person = Student("Alice", 20)
//
//    println(businessman.helloText())
//    println(student.helloText())
//
//    println(businessman.cheerText(student))
//    println(student.cheerText(businessman))
//
//    fun sayIfCanBuyAlcohol(person: Person) {
//        val modal = if (person.canBuyAlcohol) "can" else "can't"
//        println("${person.name} $modal buy alcohol")
//    }
//
//    sayIfCanBuyAlcohol(businessman)
//    sayIfCanBuyAlcohol(student)
}

class PersonTest {

//    @Test
//    fun businessmanImplementsPerson() {
//        assertTrue("Businessman needs to be a person", Businessman("AAA", 30) is Person)
//    }
//
//    @Test
//    fun studentImplementsPerson() {
//        assertTrue("Student needs to be a person", Student("AAA", 30) is Person)
//    }
//
//    @Test
//    fun personCanBuyAlcoholIfOver21() {
//        assertTrue("Adult businessman can buy alcohol", Businessman("AAA", 30).canBuyAlcohol)
//        assertTrue("Adult businessman can buy alcohol", Businessman("AAA", 21).canBuyAlcohol)
//        assertTrue("Young businessman can' buy alcohol", !Businessman("AAA", 19).canBuyAlcohol)
//        assertTrue("Young businessman can' buy alcohol", !Businessman("AAA", 17).canBuyAlcohol)
//        assertTrue("Adult student can buy alcohol", Student("AAA", 30).canBuyAlcohol)
//        assertTrue("Adult student can buy alcohol", Student("AAA", 21).canBuyAlcohol)
//        assertTrue("Young student can' buy alcohol", !Student("AAA", 19).canBuyAlcohol)
//        assertTrue("Young student can' buy alcohol", !Student("AAA", 17).canBuyAlcohol)
//    }
//
//    @Test
//    fun testBusinessmanHelloText() {
//        assertEquals("Good morning", Businessman("AAA", 30).helloText())
//    }
//
//    @Test
//    fun testStudentHelloText() {
//        assertEquals("Hi", Student("AAA", 30).helloText())
//    }
//
//    @Test
//    fun testStudentGreetText() {
//        val name1 = "Some name"
//        val name2 = "Another name"
//        val student = Student(name1, 12)
//        val businessman = Businessman(name2, 12)
//        assertEquals("Hey $name2, I am $name1", Student(name1, 30).cheerText(businessman))
//        assertEquals("Hey $name1, I am $name2", Student(name2, 30).cheerText(student))
//    }
//
//    @Test
//    fun testBusinessmanGreetText() {
//        val name1 = "Some name"
//        val name2 = "Another name"
//        val student = Student(name1, 12)
//        val businessman = Businessman(name2, 12)
//        assertEquals("Hello, my name is $name1, nice to see you $name2", Businessman(name1, 30).cheerText(businessman))
//        assertEquals("Hello, my name is $name2, nice to see you $name1", Businessman(name2, 30).cheerText(student))
//    }
}
