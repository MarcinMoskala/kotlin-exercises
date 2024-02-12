import kotlin.reflect.KProperty

private class LoggingProperty<T>(
    var value: T
) {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        println("${prop.name} from $thisRef getter returned $value")
        return value
    }

    operator fun setValue(thisRef: Any?, prop: KProperty<*>, newValue: T) {
        println("${prop.name} from $thisRef changed from $value to $newValue")
        value = newValue
    }
}
class ABC {
    var token: String? by LoggingProperty(null)
}
var attempts: Int by LoggingProperty(0)

fun main() {
    val a = ABC()
    a.token = "AAA" // token changed from null to AAA
    val res = a.token // token getter returned AAA
    println(res) // AAA
    attempts++
    // attempts getter returned 0
    // attempts changed from 0 to 1
}
