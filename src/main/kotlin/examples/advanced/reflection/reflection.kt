package examples.advanced.reflection

import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.jvm.*

//fun printABC() {
//    println("ABC")
//}
//
//fun double(i: Int): Int = i * 2
//
//class Complex(val real: Double, val imaginary: Double) {
//    fun plus(number: Number): Complex = Complex(
//        real = real + number.toDouble(),
//        imaginary = imaginary
//    )
//}
//
//fun Complex.double(): Complex =
//    Complex(real * 2, imaginary * 2)
//
//fun Complex?.isNullOrZero(): Boolean =
//    this == null || (this.real == 0.0 && this.imaginary == 0.0)
//
//class Box<T>(var value: T) {
//    fun get(): T = value
//}
//
//fun <T> Box<T>.set(value: T) {
//    this.value = value
//}
//
//fun main() {
//    val f1 = ::printABC
//    val f2 = ::double
//    val f3 = Complex::plus
//    val f4 = Complex::double
//    val f5 = Complex?::isNullOrZero
//    val f6 = Box<Int>::get
//    val f7 = Box<String>::set
//}

//fun main() {
//    val f1: KFunction0<Unit> = ::printABC
//    val f2: KFunction1<Int, Int> = ::double
//    val f3: KFunction2<Complex, Number, Complex> = Complex::plus
//    val f4: KFunction1<Complex, Complex> = Complex::double
//    val f5: KFunction1<Complex?, Boolean> = Complex?::isNullOrZero
//    val f6: KFunction1<Box<Int>, Int> = Box<Int>::get
//    val f7: KFunction2<Box<String>, String, Unit>=Box<String>::set
//}

//fun main() {
//    val c = Complex(1.0, 2.0)
//    val f3: KFunction1<Number, Complex> = c::plus
//    val f4: KFunction0<Complex> = c::double
//    val f5: KFunction0<Boolean> = c::isNullOrZero
//    val b = Box(123)
//    val f6: KFunction0<Int> = b::get
//    val f7: KFunction1<Int, Unit> = b::set
//}

//fun main() {
//    val f1: KFunction<Unit> = ::printABC
//    val f2: KFunction<Int> = ::double
//    val f3: KFunction<Complex> = Complex::plus
//    val f4: KFunction<Complex> = Complex::double
//    val f5: KFunction<Boolean> = Complex?::isNullOrZero
//    val f6: KFunction<Int> = Box<Int>::get
//    val f7: KFunction<Unit> = Box<String>::set
//    val c = Complex(1.0, 2.0)
//    val f8: KFunction<Complex> = c::plus
//    val f9: KFunction<Complex> = c::double
//    val f10: KFunction<Boolean> = c::isNullOrZero
//    val b = Box(123)
//    val f11: KFunction<Int> = b::get
//    val f12: KFunction<Unit> = b::set
//}

//fun add(i: Int, j: Int) = i + j
//
//fun main() {
//    val f: KFunction2<Int, Int, Int> = ::add
//    println(f(1, 2)) // 3
//    println(f.invoke(1, 2)) // 3
//}

//inline infix operator fun String.times(times: Int) =
//    this.repeat(times)
//
//fun main() {
//    val f: KFunction<String> = String::times
//    println(f.isInline)   // true
//    println(f.isExternal) // false
//    println(f.isOperator) // true
//    println(f.isInfix)    // true
//    println(f.isSuspend)  // false
//}

//operator fun String.times(times: Int) = this.repeat(times)
//
//fun main() {
//    val f: KCallable<String> = String::times
//    println(f.name) // times
//    println(f.parameters.map { it.name }) // [null, times]
//    println(f.returnType) // kotlin.String
//    println(f.typeParameters) // []
//    println(f.visibility) // PUBLIC
//    println(f.isFinal) // true
//    println(f.isOpen) // false
//    println(f.isAbstract) // false
//    println(f.isSuspend) // false
//}

//fun add(i: Int, j: Int) = i + j
//
//fun main() {
//    val f: KCallable<Int> = ::add
//    println(f.call(1, 2)) // 3
//    println(f.call("A", "B")) // IllegalArgumentException
//}

//fun sendEmail(
//    email: String,
//    title: String = "",
//    message: String = ""
//) {
//    println(
//        """
//       Sending to $email
//       Title: $title
//       Message: $message
//   """.trimIndent()
//    )
//}
//
//fun main() {
//    val f: KCallable<Unit> = ::sendEmail
//
//    f.callBy(mapOf(f.parameters[0] to "ABC"))
//    // Sending to ABC
//    // Title:
//    // Message:
//
//    val params = f.parameters.associateBy { it.name }
//    f.callBy(
//        mapOf(
//            params["title"]!! to "DEF",
//            params["message"]!! to "GFI",
//            params["email"]!! to "ABC",
//        )
//    )
//    // Sending to ABC
//    // Title: DEF
//    // Message: GFI
//
//    f.callBy(mapOf()) // throws IllegalArgumentException
//}

//fun callWithFakeArgs(callable: KCallable<*>) {
//    val arguments = callable.parameters
//        .filterNot { it.isOptional }
//        .associateWith { fakeValueFor(it) }
//    callable.callBy(arguments)
//}
//
//fun fakeValueFor(parameter: KParameter) =
//    when (parameter.type) {
//        typeOf<String>() -> "Fake ${parameter.name}"
//        typeOf<Int>() -> 123
//        else -> error("Unsupported type")
//    }
//
//fun sendEmail(
//    email: String,
//    title: String,
//    message: String = ""
//) {
//    println(
//        """
//       Sending to $email
//       Title: $title
//       Message: $message
//   """.trimIndent()
//    )
//}
//fun printSum(a: Int, b: Int) {
//    println(a + b)
//}
//fun Int.printProduct(b: Int) {
//    println(this * b)
//}
//
//fun main() {
//    callWithFakeArgs(::sendEmail)
//    // Sending to Fake email
//    // Title: Fake title
//    // Message:
//    callWithFakeArgs(::printSum) // 246
//    callWithFakeArgs(Int::printProduct) // 15129
//}

//val lock: Any = Any()
//var str: String = "ABC"
//
//class Box(
//    var value: Int = 0
//) {
//    val Int.addedToBox
//        get() = Box(value + this)
//}
//
//fun main() {
//    val p1: KProperty0<Any> = ::lock
//    println(p1) // val lock: kotlin.Any
//    val p2: KMutableProperty0<String> = ::str
//    println(p2) // var str: kotlin.String
//    val p3: KMutableProperty1<Box, Int> = Box::value
//    println(p3) // var Box.value: kotlin.Int
//    val p4: KProperty2<Box, *, *> = Box::class
//        .memberExtensionProperties
//        .first()
//    println(p4) // val Box.(kotlin.Int.)addedToBox: Box
//}

//class Box(
//    var value: Int = 0
//)
//
//fun main() {
//    val box = Box()
//    val p: KMutableProperty1<Box, Int> = Box::value
//    println(p.get(box)) // 0
//    p.set(box, 999)
//    println(p.get(box)) // 999
//}

//class A
//
//fun main() {
//    val class1: KClass<A> = A::class
//    println(class1) // class A
//    
//    val a: A = A()
//    val class2: KClass<out A> = a::class
//    println(class2) // class A
//}

//open class A
//class B : A()
//
//fun main() {
//    val a: A = B()
//    val clazz: KClass<out A> = a::class
//    println(clazz) // class B
//}

//class D {
//    class E
//}
//
//fun main() {
//    val clazz = D.E::class
//    println(clazz.simpleName) // E
//    println(clazz.qualifiedName) // a.b.c.D.E
//}

//fun main() {
//    val o = object {}
//    val clazz = o::class
//    println(clazz.simpleName) // null
//    println(clazz.qualifiedName) // null
//}

//sealed class UserMessages
//
//private data class UserId(val id: String) {
//    companion object {
//        val ZERO = UserId("")
//    }
//}
//
//internal fun interface Filter<T> {
//    fun check(value: T): Boolean
//}
//
//fun main() {
//    println(UserMessages::class.visibility) // PUBLIC
//    println(UserMessages::class.isSealed) // true
//    println(UserMessages::class.isOpen) // false
//    println(UserMessages::class.isFinal) // false
//    println(UserMessages::class.isAbstract) // false
//
//    println(UserId::class.visibility) // PRIVATE
//    println(UserId::class.isData) // true
//    println(UserId::class.isFinal) // true
//
//    println(UserId.Companion::class.isCompanion) // true
//    println(UserId.Companion::class.isInner) // false
//
//    println(Filter::class.visibility) // INTERNAL
//    println(Filter::class.isFun) // true
//}

//open class Parent {
//    val a = 12
//    fun b() {}
//}
//
//class Child : Parent() {
//    val c = 12
//    fun d() {}
//}
//
//fun Child.e() {}
//
//fun main() {
//    println(Child::class.members.map { it.name })
//    // [c, d, a, b, equals, hashCode, toString]
//    println(Child::class.functions.map { it.name })
//    // [d, b, equals, hashCode, toString]
//    println(Child::class.memberProperties.map { it.name })
//    // [c, a]
//
//    println(Child::class.declaredMembers.map { it.name })
//    // [c, d]
//    println(Child::class.declaredFunctions.map { it.name }) 
//    // [d]
//    println(Child::class.declaredMemberProperties.map { it.name })
//    // [c]
//}

//class User(val name: String) {
//    constructor(user: User) : this(user.name)
//    constructor(json: UserJson) : this(json.name)
//}
//
//class UserJson(val name: String)
//
//fun main() {
//    val constructors: Collection<KFunction<User>> =
//        User::class.constructors
//    
//    println(constructors.size) // 3
//    constructors.forEach(::println)
//    // fun <init>(playground.User): playground.User
//    // fun <init>(playground.UserJson): playground.User
//    // fun <init>(kotlin.String): playground.User
//}

//interface I1
//interface I2
//open class A : I1
//class B : A(), I2
//
//fun main() {
//    val a = A::class
//    val b = B::class
//    println(a.superclasses) // [class I1, class kotlin.Any]
//    println(b.superclasses) // [class A, class I2]
//    println(a.supertypes) // [I1, kotlin.Any]
//    println(b.supertypes) // [A, I2]
//}

//interface I1
//interface I2
//open class A : I1
//class B : A(), I2
//
//fun main() {
//    val a = A()
//    val b = B()
//    println(A::class.isInstance(a)) // true
//    println(B::class.isInstance(a)) // false
//    println(I1::class.isInstance(a)) // true
//    println(I2::class.isInstance(a)) // false
//    
//    println(A::class.isInstance(b)) // true
//    println(B::class.isInstance(b)) // true
//    println(I1::class.isInstance(b)) // true
//    println(I2::class.isInstance(b)) // true
//}

//fun main() {
//    println(List::class.typeParameters) // [out E]
//    println(Map::class.typeParameters) // [K, out V]
//}

//class A {
//    class B
//    inner class C
//}
//
//fun main() {
//    println(A::class.nestedClasses) // [class A$B, class A$C]
//}

//sealed class LinkedList<out T>
//
//class Node<out T>(
//    val head: T,
//    val next: LinkedList<T>
//) : LinkedList<T>()
//
//object Empty : LinkedList<Nothing>()
//
//fun main() {
//    println(LinkedList::class.sealedSubclasses)
//    // [class Node, class Empty]
//}

//sealed class LinkedList<out T>
//
//data class Node<out T>(
//    val head: T,
//    val next: LinkedList<T>
//) : LinkedList<T>()
//
//data object Empty : LinkedList<Nothing>()
//
//fun main() {
//    println(Node::class.objectInstance) // null
//    println(Empty::class.objectInstance) // Empty
//}

//fun main() {
//    val t1: KType = typeOf<Int?>()
//    println(t1) // kotlin.Int?
//    val t2: KType = typeOf<List<Int?>>()
//    println(t2) // kotlin.collections.List<kotlin.Int?>
//    val t3: KType = typeOf<() -> Map<Int, Char?>>()
//    println(t3)
//    // () -> kotlin.collections.Map<kotlin.Int, kotlin.Char?>
//}

//class A {
//    val a = 123
//    fun b() {}
//}
//
//fun main() {
//    val c1: Class<A> = A::class.java
//    val c2: Class<A> = A().javaClass
//    
//    val f1: Field? = A::a.javaField
//    val f2: Method? = A::a.javaGetter
//    val f3: Method? = A::b.javaMethod
//    
//    val kotlinClass: KClass<A> = c1.kotlin
//    val kotlinProperty: KProperty<*>? = f1?.kotlinProperty
//    val kotlinFunction: KFunction<*>? = f3?.kotlinFunction
//}

//class A {
//    private var value = 0
//    private fun printValue() {
//        println(value)
//    }
//    override fun toString(): String =
//        "A(value=$value)"
//}
//
//fun main() {
//    val a = A()
//    val c = A::class
//    
//    // We change value to 999
//    val prop = c.declaredMemberProperties
//        .find { it.name == "value" } as? KMutableProperty1<A, Int>
//    prop?.isAccessible = true
//    prop?.set(a, 999)
//    println(a) // A(value=999)
//    println(prop?.get(a)) // 999
//    
//    // We call printValue function
//    val func: KFunction<*>? = c.declaredMemberFunctions
//        .find { it.name == "printValue" }
//    func?.isAccessible = true
//    func?.call(a) // 999
//}
