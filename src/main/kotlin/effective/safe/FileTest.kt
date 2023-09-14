package effective.safe

import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileTest {

    @Test
    fun `All texts in the texts should be under 100 words`() {
        val folder = File("texts")
        assertTrue(folder.exists())
        assertTrue(folder.isDirectory)
        val files: List<File> = assertNotNull(folder.listFiles()?.toList())
        TODO()
    }
}
