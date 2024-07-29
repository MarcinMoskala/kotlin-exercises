package examples.ref

fun zeroComplex(): Complex = Complex(0.0, 0.0)
fun makeComplex(real: Double = 0.0, imaginary: Double = 0.0) = Complex(real, imaginary)

data class Complex(val real: Double, val imaginary: Double) {
    fun doubled(): Complex = Complex(this.real * 2, this.imaginary * 2)
    fun times(num: Int) = Complex(real * num, imaginary * num)
    companion object {
        fun zero() = Complex(0.0, 0.0)
    }
}

fun Complex.plus(other: Complex): Complex = Complex(real + other.real, imaginary + other.imaginary)
fun Int.toComplex() = Complex(this.toDouble(), 0.0)
fun produceComplex(producer: () -> Complex) = producer()

fun main() {
    val c1 = Complex(1.0, 2.0)
    val c2 = Complex(3.0, 4.0)

}
