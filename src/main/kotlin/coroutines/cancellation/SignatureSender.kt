package coroutines.cancellation.signaturesender

import kotlinx.coroutines.*

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.currentTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class SignatureSender(
    private val signatureApi: SignatureApi,
    private val signatureCalculator: SignatureCalculator,
    private val fileReader: FileReader,
    private val logger: Logger,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun sendSignature(file: File) {
        try {
            val content = fileReader.readFile(file) // blocking
            val signature = signatureCalculator.calculateSignature(content) // CPU-intensive function
            signatureApi.sendSignature(signature) // suspending
        } catch (e: Exception) {
            logger.logError("Error while sending signature", e)
        } finally {
            fileReader.deleteFile(file)
        }
    }
}

interface SignatureApi {
    suspend fun sendSignature(signature: String)
}

interface SignatureCalculator {
    fun calculateSignature(content: String): String
}

interface FileReader {
    fun readFile(file: File): String
    fun deleteFile(file: File)
}

interface Logger {
    fun logError(message: String, e: Exception)
}

class SignatureSenderTest {
    @Test
    fun `should send signature and delete file`() = runTest {
        // given
        var sentSignature: String? = null
        var deletedFiles = listOf<File>()
        val signatureApi = object : SignatureApi {
            override suspend fun sendSignature(signature: String) {
                sentSignature = signature
            }
        }
        val signatureCalculator = object : SignatureCalculator {
            override fun calculateSignature(content: String): String {
                return "calculated-signature"
            }
        }
        val fileReader = object : FileReader {
            override fun readFile(file: File): String {
                return "file-content"
            }

            override fun deleteFile(file: File) {
                deletedFiles = deletedFiles + file
            }
        }
        val logger = object : Logger {
            override fun logError(message: String, e: Exception) {
                // no-op
            }
        }
        val dispatcher = coroutineContext[CoroutineDispatcher]!!
        val signatureSender = SignatureSender(signatureApi, signatureCalculator, fileReader, logger, dispatcher)
        val testFile = File("test-file")

        // when
        signatureSender.sendSignature(testFile)

        // then
        assertEquals("calculated-signature", sentSignature)
        assert(deletedFiles.contains(testFile))
    }

    @Test
    fun `should log exceptions`() = runTest {
        // given
        val loggedMessages = mutableListOf<String>()
        val signatureApi = object : SignatureApi {
            override suspend fun sendSignature(signature: String) {
                throw Exception("API error")
            }
        }
        val signatureCalculator = object : SignatureCalculator {
            override fun calculateSignature(content: String): String {
                return "calculated-signature"
            }
        }
        val fileReader = object : FileReader {
            override fun readFile(file: File): String {
                return "file-content"
            }

            override fun deleteFile(file: File) {
                // no-op
            }
        }
        val logger = object : Logger {
            override fun logError(message: String, e: Exception) {
                loggedMessages.add("$message: ${e.message}")
            }
        }
        val dispatcher = coroutineContext[CoroutineDispatcher]!!
        val signatureSender = SignatureSender(signatureApi, signatureCalculator, fileReader, logger, dispatcher)
        val testFile = File("test-file")

        // when
        signatureSender.sendSignature(testFile)

        // then
        assert(loggedMessages.any { it.contains("Error while sending signature: API error") })
    }

    @Test
    fun `should not block caller thread`() = runTest {
        // given
        var blockedThreadsNames = listOf<String>()
        val signatureApi = object : SignatureApi {
            override suspend fun sendSignature(signature: String) {
                delay(1000)
            }
        }
        val signatureCalculator = object : SignatureCalculator {
            override fun calculateSignature(content: String): String {
                blockedThreadsNames += Thread.currentThread().name
                return "calculated-signature"
            }
        }
        val fileReader = object : FileReader {
            override fun readFile(file: File): String {
                blockedThreadsNames += Thread.currentThread().name
                return "file-content"
            }

            override fun deleteFile(file: File) {
                // no-op
            }
        }
        val logger = object : Logger {
            override fun logError(message: String, e: Exception) {
                // no-op
            }
        }
        val signatureSender = SignatureSender(signatureApi, signatureCalculator, fileReader, logger, Dispatchers.IO)
        val callerThreadName = "test"
        val testFile = File("test-file")

        // when
        withContext(newSingleThreadContext(callerThreadName)) {
            signatureSender.sendSignature(testFile)
        }

        // then
        assert(blockedThreadsNames.size == 2)
        assert(callerThreadName !in blockedThreadsNames)
    }

    @Test
    fun `not consume CancellationException`() = runTest {
        // given
        val signatureApi = object : SignatureApi {
            override suspend fun sendSignature(signature: String) {
                delay(1000)
            }
        }
        val signatureCalculator = object : SignatureCalculator {
            override fun calculateSignature(content: String): String {
                return "calculated-signature"
            }
        }
        val fileReader = object : FileReader {
            override fun readFile(file: File): String {
                return "file-content"
            }

            override fun deleteFile(file: File) {
                // no-op
            }
        }
        val loggedExceptions = mutableListOf<Exception>()
        val logger = object : Logger {
            override fun logError(message: String, e: Exception) {
                loggedExceptions.add(e)
            }
        }
        val signatureSender = SignatureSender(signatureApi, signatureCalculator, fileReader, logger, Dispatchers.IO)
        val testFile = File("test-file")

        // when
        var result: Result<Unit>? = null
        val job = launch {
            result = runCatching {
                signatureSender.sendSignature(testFile)
            }
        }
        delay(500)
        job.cancelAndJoin()

        // then
        assert(result?.exceptionOrNull() is CancellationException)
        assert(loggedExceptions.isEmpty())
    }

    @Test
    fun `should allow cancellation between blocking and CPU-intensive call`() {
        runTest {
            // given
            var job: Job? = null
            var calculateSignatureCalled = false
            val signatureApi = object : SignatureApi {
                override suspend fun sendSignature(signature: String) {
                    delay(1000)
                }
            }
            val signatureCalculator = object : SignatureCalculator {
                override fun calculateSignature(content: String): String {
                    calculateSignatureCalled = true
                    return "calculated-signature"
                }
            }
            val fileReader = object : FileReader {
                override fun readFile(file: File): String {
                    job?.cancel()
                    return "file-content"
                }

                override fun deleteFile(file: File) {
                    // no-op
                }
            }
            val logger = object : Logger {
                override fun logError(message: String, e: Exception) {
                    // no-op
                }
            }
            val signatureSender = SignatureSender(signatureApi, signatureCalculator, fileReader, logger, Dispatchers.IO)
            val callerThreadName = "test"
            val testFile = File("test-file")

            // when
            job = launch {
                signatureSender.sendSignature(testFile)
            }
            job.join()

            // then
            assert(!calculateSignatureCalled) { "Should allow cancellation between blocking and CPU-intensive call" }
            assertEquals(0, currentTime)
        }
    }
}
