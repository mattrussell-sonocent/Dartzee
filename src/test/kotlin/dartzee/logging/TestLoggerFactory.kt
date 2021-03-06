package dartzee.logging

import dartzee.helper.AbstractTest
import dartzee.logging.LoggerFactory.readCredentials
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class TestLoggerFactory: AbstractTest()
{
    private val rsrcPath = File(javaClass.getResource("/ChangeLog").file).absolutePath
    private val newRsrcPath = rsrcPath.replace("ChangeLog", "foo")
    private val testFile: File = File(newRsrcPath)
    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        System.setOut(PrintStream(newOut))
    }

    override fun afterEachTest()
    {
        super.afterEachTest()

        testFile.delete()
        System.setOut(originalOut)
    }

    @Test
    fun `Should print an error and return null if file does not exist`()
    {
        clearLogs()

        val credentials = readCredentials("foo")
        credentials shouldBe null

        getLogRecords().shouldBeEmpty()
        newOut.toString().shouldContain("java.lang.IllegalStateException: javaClass.getResource(\"/\$resourceName\") must not be null")
    }

    @Test
    fun `Should print an error and return null for invalid file contents`()
    {
        testFile.writeText("foo")
        clearLogs()

        val credentials = readCredentials("foo")
        credentials shouldBe null

        getLogRecords().shouldBeEmpty()
        newOut.toString().shouldContain("Failed to read in AWS credentials: java.lang.IndexOutOfBoundsException")
    }

}