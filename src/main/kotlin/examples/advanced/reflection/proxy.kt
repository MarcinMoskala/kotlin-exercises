package examples.advanced.reflection

import java.lang.reflect.Proxy

interface SampleClass {
    fun a(): Int
    fun b(value: Int): Int
}

fun main() {
    val proxyInstance: SampleClass = Proxy.newProxyInstance(
        SampleClass::class.java.getClassLoader(),
        arrayOf<Class<*>>(SampleClass::class.java)
    ) { proxy, method, methodArgs ->
        when(method.name) {
            "a" -> {
                println("A")
                42
            }
            "b" -> {
                val arg = methodArgs[0]
                println("B($arg)")
                arg
            }
            else -> error("Unsupported method: ${method.name}")
        }
    } as SampleClass
    
    println(proxyInstance.a()) // A 42
    println(proxyInstance.b(123)) // B(123) 123
}
