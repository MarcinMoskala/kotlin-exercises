package effective.efficient

import org.junit.Assert.assertEquals
import java.io.DataInputStream
import java.io.File
import java.nio.ByteBuffer
import kotlin.random.Random

fun main() {
    val elements = 10_000_000
    val random = Random(123456789)
    val data = sequence {
        while (true) {
            val event = when ((0..2).random(random)) {
                0 -> BidEvent(
                    random.nextInt(1000),
                    if (random.nextInt(100) == 1) Double.NaN else random.nextInt(100_000).toDouble(),
                    if (random.nextInt(100) == 1) -1 else random.nextInt(100_000),
                    if (random.nextInt(100) == 1) -1 else random.nextLong()
                )

                1 -> AskEvent(
                    random.nextInt(1000),
                    if (random.nextInt(100) == 1) Double.NaN else random.nextInt(100_000).toDouble(),
                    if (random.nextInt(100) == 1) -1 else random.nextInt(100_000),
                    if (random.nextInt(100) == 1) -1 else random.nextLong()
                )

                else -> TradeEvent(
                    random.nextInt(1000),
                    if (random.nextInt(100) == 1) Double.NaN else random.nextInt(100_000).toDouble(),
                    if (random.nextInt(100) == 1) -1 else random.nextInt(100_000),
                    if (random.nextInt(100) == 1) -1 else random.nextLong()
                )
            }
            yield(event)
        }
    }

    val result = data.take(elements)

    val file = File("market.txt")
        .also { it.delete() }
        .also { it.createNewFile() }
    result.forEach {
        val array = when (it) {
            is BidEvent -> byteArrayOf(0) + it.ticker!!.toByteArray() + (it.price
                ?: Double.NaN).toByteArray() + (it.size ?: -1).toByteArray() + (it.time ?: -1).toByteArray()

            is AskEvent -> byteArrayOf(1) + it.ticker!!.toByteArray() + (it.price
                ?: Double.NaN).toByteArray() + (it.size ?: -1).toByteArray() + (it.time ?: -1).toByteArray()

            is TradeEvent -> byteArrayOf(2) + it.ticker!!.toByteArray() + (it.price
                ?: Double.NaN).toByteArray() + (it.size ?: -1).toByteArray() + (it.time ?: -1).toByteArray()
        }
        file.appendBytes(array)
    }

    val read = sequence {
        val input = DataInputStream(file.inputStream())
        while (true) {
            val event = when (input.read()) {
                0 -> BidEvent(
                    input.readInt(),
                    input.readDouble(),
                    input.readInt(),
                    input.readLong()
                )

                1 -> AskEvent(
                    input.readInt(),
                    input.readDouble(),
                    input.readInt(),
                    input.readLong()
                )

                2 -> TradeEvent(
                    input.readInt(),
                    input.readDouble(),
                    input.readInt(),
                    input.readLong()
                )

                else -> throw IllegalArgumentException()
            }
            yield(event)
        }
    }.take(elements)
}

fun DataInputStream.readText(characters: Int): String {
    val buffer = ByteArray(characters)
    read(buffer)
    return String(buffer)
}

fun Int.toByteArray(): ByteArray =
    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

fun Long.toByteArray(): ByteArray =
    ByteBuffer.allocate(Long.SIZE_BYTES).putLong(this).array()

fun Double.toByteArray(): ByteArray =
    ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(this).array()
