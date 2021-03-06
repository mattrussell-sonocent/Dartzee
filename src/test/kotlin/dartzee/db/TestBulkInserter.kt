package dartzee.db

import dartzee.helper.*
import dartzee.logging.CODE_BULK_SQL
import dartzee.logging.CODE_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import dartzee.utils.DatabaseUtil
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldBeSortedWith
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotBeSortedWith
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestBulkInserter: AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()

        DatabaseUtil.executeUpdate("CREATE TABLE InsertTest (RowId INT)")
    }

    override fun afterEachTest()
    {
        super.afterEachTest()

        DatabaseUtil.dropTable("InsertTest")
    }

    @Test
    fun `Should do nothing if passed no entities to insert`()
    {
        clearLogs()
        BulkInserter.insert()
        getLogRecords().shouldBeEmpty()
    }

    @Test
    fun `Should stack trace and do nothing if any entities are retrievedFromDb`()
    {
        wipeTable("Player")

        val playerOne = PlayerEntity()
        val playerTwo = insertPlayer()

        BulkInserter.insert(playerOne, playerTwo)

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.message shouldBe "Attempting to bulk insert Player entities, but some are already in the database"
        getCountFromTable("Player") shouldBe 1
    }

    @Test
    fun `Should log SQLExceptions if something goes wrong inserting entities`()
    {
        wipeTable("Player")

        val playerOne = factoryPlayer("Pete")
        val playerTwo = factoryPlayer("Leah")

        playerOne.rowId = playerTwo.rowId

        BulkInserter.insert(playerOne, playerTwo)

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.errorObject?.message shouldContain "duplicate key value"

        getCountFromTable("Player") shouldBe 0
    }

    @Test
    fun `Should insert the right number of rows per INSERT statement`()
    {
        val rows = prepareRows(200)

        checkInsertBatching(rows, 1, 200)
        checkInsertBatching(rows, 20, 10)
        checkInsertBatching(rows, 21, 10)
    }
    private fun checkInsertBatching(rows: List<String>, rowsPerInsert: Int, expectedNumberOfBatches: Int)
    {
        wipeTable("InsertTest")
        clearLogs()

        BulkInserter.insert("InsertTest", rows, 1000, rowsPerInsert)

        getLogRecords() shouldHaveSize(expectedNumberOfBatches)
        getCountFromTable("InsertTest") shouldBe rows.size
    }

    @Test
    fun `Should only run 1 thread for a small number of rows`()
    {
        val rows = prepareRows(50)

        BulkInserter.insert("InsertTest", rows, 50, 1)

        retrieveValues() shouldBeSortedWith{i: Int, j: Int -> i.compareTo(j)}
        getCountFromTable("InsertTest") shouldBe 50
    }

    @Test
    fun `Should run multi-threaded if required`()
    {
        val rows = prepareRows(50)

        BulkInserter.insert("InsertTest", rows, 5, 1)

        retrieveValues() shouldNotBeSortedWith{i: Int, j: Int -> i.compareTo(j)}
        getCountFromTable("InsertTest") shouldBe 50
    }

    @Test
    fun `Should temporarily suppress logging for a large number of rows`()
    {
        val rows = prepareRows(501)
        clearLogs()

        BulkInserter.insert("InsertTest", rows, 300, 50)

        getLogRecords().size shouldBe 1
        val log = getLastLog()
        log.loggingCode shouldBe CODE_BULK_SQL
        log.message shouldBe "Inserting 501 rows into InsertTest (2 threads @ 50 rows per insert)"
        getCountFromTable("InsertTest") shouldBe 501

        val moreRows = prepareRows(10)
        BulkInserter.insert("InsertTest", moreRows, 300, 50)

        val newLog = getLastLog()
        newLog.loggingCode shouldBe CODE_SQL
        newLog.message shouldContain "INSERT INTO InsertTest VALUES"
    }


    private fun retrieveValues(): List<Int>
    {
        val rows = mutableListOf<Int>()
        DatabaseUtil.executeQuery("SELECT RowId FROM InsertTest").use{ rs ->
            while (rs.next())
            {
                rows.add(rs.getInt(1))
            }
        }

        return rows
    }

    private fun prepareRows(numberToGenerate: Int): List<String>
    {
        val rows = mutableListOf<String>()
        for (i in 1..numberToGenerate)
        {
            rows.add("($i)")
        }

        return rows
    }
}