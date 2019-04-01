package burlton.dartzee.test.utils

import burlton.core.code.util.AbstractClient
import burlton.core.code.util.Debug
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestDatabaseUtil: AbstractDartsTest()
{
    @Test
    fun `Should create a new connection if the pool is depleted`()
    {
        DatabaseUtil.initialiseConnectionPool(1)
        Debug.waitUntilLoggingFinished()
        Debug.clearLogs()

        //Should borrow from the pool when non-empty
        val conn = DatabaseUtil.borrowConnection()
        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldBeEmpty()

        //Should create a new one now that there are none left
        val conn2 = DatabaseUtil.borrowConnection()
        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldContain("CREATED new connection")

        DatabaseUtil.returnConnection(conn2)
        DatabaseUtil.returnConnection(conn)

        //Should have returned the connection successfully
        Debug.clearLogs()
        DatabaseUtil.borrowConnection()
        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldBeEmpty()
    }

    @Test
    fun `Should execute all updates and log them to the console`()
    {
        val updates = listOf("CREATE TABLE zzUpdateTest(str VARCHAR(50))", "INSERT INTO zzUpdateTest VALUES ('5')")

        DatabaseUtil.executeUpdates(updates) shouldBe true

        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldContain("CREATE TABLE zzUpdateTest(str VARCHAR(50));")
        Debug.getLogs().shouldContain("INSERT INTO zzUpdateTest VALUES ('5');")

        DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM zzUpdateTest") shouldBe 1

        DatabaseUtil.dropTable("zzUpdateTest")
    }

    @Test
    fun `Should abort if any updates fail`()
    {
        val updates = listOf("bollucks", "CREATE TABLE zzUpdateTest(str VARCHAR(50))")

        DatabaseUtil.executeUpdates(updates) shouldBe false
        DatabaseUtil.createTableIfNotExists("zzUpdateTest", "str VARCHAR(50)") shouldBe true

        DatabaseUtil.dropTable("zzUpdateTest")
    }

    @Test
    fun `Should log SQLExceptions for failed updates`()
    {
        val update = "CREATE TABLE zzUpdateTest(str INVALID(50))"
        DatabaseUtil.executeUpdate(update) shouldBe false

        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldContain("Caught SQLException for query: $update")
        Debug.getLogs().shouldContain("Syntax error: Encountered \"(\"")
    }

    @Test
    fun `Should execute queries and log them to the console`()
    {
        val updates = listOf("CREATE TABLE zzQueryTest(str VARCHAR(50))",
                "INSERT INTO zzQueryTest VALUES ('RowOne')",
                "INSERT INTO zzQueryTest VALUES ('RowTwo')")

        DatabaseUtil.executeUpdates(updates)

        val retrievedValues = mutableListOf<String>()
        DatabaseUtil.executeQuery("SELECT * FROM zzQueryTest").use { rs ->
            while (rs.next())
            {
                retrievedValues.add(rs.getString(1))
            }
        }

        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldContain("SELECT * FROM zzQueryTest")
        retrievedValues.shouldContainExactly("RowOne", "RowTwo")

        DatabaseUtil.dropTable("zzQueryTest")
    }

    @Test
    fun `Should log SQLExceptions (and show an error) for failed queries`()
    {
        Debug.setLogToSystemOut(true)

        val query = "SELECT * FROM zzQueryTest"
        DatabaseUtil.executeQuery(query)
        Debug.waitUntilLoggingFinished()

        Debug.getLogs().shouldContain("Table/View 'ZZQUERYTEST' does not exist.")
        Debug.getLogs().shouldContain("Caught SQLException for query: $query")

        dialogFactory.errorsShown.shouldHaveSize(1)
    }

    @Test
    fun `Should log an exception (but not show an error) for queries that take too long`()
    {
        AbstractClient.sqlToleranceQuery = -1

        val query = "SELECT * FROM Game"
        DatabaseUtil.executeQuery(query)

        Debug.waitUntilLoggingFinished()
        Debug.getLogs().shouldContain("SQL query took longer than ${AbstractClient.sqlToleranceQuery} millis: $query")
        dialogFactory.errorsShown.shouldBeEmpty()

        wipeTable("Game")
        AbstractClient.sqlToleranceQuery = AbstractClient.SQL_TOLERANCE_QUERY
    }
}