package advanced.delegates.mutablelazy

import org.junit.Assert
import org.junit.Test
import kotlin.properties.ReadWriteProperty
import kotlin.system.measureTimeMillis

fun <T> mutableLazy(
    initializer: () -> T
): ReadWriteProperty<Any?, T> = TODO()

class MutableLazyTest {

    @Test
    fun `Do not initialize if initialized`() {
        val time = measureTimeMillis {
            var game: Game? by mutableLazy { readGameFromSave() }
            game = Game()
            print(game)
        }
        assert(time in 0..100)
    }

    @Test
    fun `Initializes if not initialized`() {
        val time = measureTimeMillis {
            val game: Game? by mutableLazy { readGameFromSave() }
            print(game)
        }
        assert(time in 450..550)
    }

    @Test
    fun `Do not initialize again if already initialized`() {
        val time = measureTimeMillis {
            val game: Game? by mutableLazy { readGameFromSave() }
            print(game)
            print(game)
            print(game)
        }
        assert(time in 450..550)
    }

    @Test
    fun `MutableLazy should accept nullable values`() {
        val lazy by mutableLazy<String?> { null }
        Assert.assertNull(lazy)

        var lazy2 by mutableLazy<String?> { "A" }
        lazy2 = null
        Assert.assertNull(lazy2)
    }

    private class Game()

    private fun readGameFromSave(): Game? {
        Thread.sleep(500)
        return Game()
    }
}
