package dartzee.utils

import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import dartzee.logging.*
import dartzee.utils.InjectedThings.logger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetProvider
import kotlin.system.exitProcess

const val TABLE_ALREADY_EXISTS = "X0Y32"

/**
 * Generic derby helper methods
 */
class DatabaseUtil
{
    companion object
    {
        val DATABASE_FILE_PATH = System.getProperty("user.dir") + "\\Databases"

        private val hsConnections = mutableListOf<Connection>()
        private val connectionPoolLock = Any()
        private var connectionCreateCount = 0

        fun initialiseConnectionPool(initialCount: Int)
        {
            synchronized(connectionPoolLock)
            {
                hsConnections.clear()
                for (i in 0 until initialCount)
                {
                    val conn = createDatabaseConnection()
                    hsConnections.add(conn)
                }
            }
        }

        fun borrowConnection() : Connection
        {
            synchronized(connectionPoolLock)
            {
                if (hsConnections.isEmpty())
                {
                    return createDatabaseConnection()
                }

                return hsConnections.removeAt(0)
            }
        }

        fun returnConnection(connection: Connection)
        {
            synchronized(connectionPoolLock)
            {
                hsConnections.add(connection)
            }
        }

        private fun createDatabaseConnection(): Connection
        {
            connectionCreateCount++

            val connection = createDatabaseConnection(dbName = DartsClient.derbyDbName)
            logger.info(CODE_NEW_CONNECTION, "Created new connection. Total created: $connectionCreateCount, pool size: ${hsConnections.size}")
            return connection
        }

        private fun createDatabaseConnection(dbFilePath: String = DATABASE_FILE_PATH, dbName: String): Connection
        {
            val p = System.getProperties()
            p.setProperty("derby.system.home", dbFilePath)
            p.setProperty("derby.language.logStatementText", "${DartsClient.devMode}")
            p.setProperty("derby.language.logQueryPlan", "${DartsClient.devMode}")

            val props = Properties()
            props["user"] = "administrator"
            props["password"] = "wallace"

            return DriverManager.getConnection(dbName, props)
        }

        fun executeUpdates(statements: List<String>): Boolean
        {
            statements.forEach{
                if (!executeUpdate(it))
                {
                    return false
                }
            }

            return true
        }

        fun executeUpdate(statement: String, log: Boolean = true): Boolean
        {
            try
            {
                executeUpdateUncaught(statement, log)
            }
            catch (sqle: SQLException)
            {
                logger.logSqlException(statement, "", sqle)
                return false
            }

            return true
        }

        private fun executeUpdateUncaught(statement: String, log: Boolean = true)
        {
            val timer = DurationTimer()
            val conn = borrowConnection()
            try
            {
                conn.createStatement().use{
                    s -> s.execute(statement)
                }
            }
            finally
            {
                returnConnection(conn)
            }

            if (log)
            {
                logger.logSql(statement, "", timer.getDuration())
            }
        }

        fun executeQuery(sb: StringBuilder): ResultSet
        {
            return executeQuery(sb.toString())
        }

        fun executeQuery(query: String): ResultSet
        {
            val timer = DurationTimer()
            var crs: CachedRowSet? = null

            val conn = borrowConnection()
            try
            {
                conn.createStatement().use { s ->
                    s.executeQuery(query).use { rs ->
                        crs = RowSetProvider.newFactory().createCachedRowSet()
                        crs!!.populate(rs)
                    }
                }
            }
            catch (sqle: SQLException)
            {
                logger.logSqlException(query, "", sqle)
            }
            finally
            {
                returnConnection(conn)
            }

            logger.logSql(query, "", timer.getDuration())

            //Return an empty one if something's gone wrong
            return crs ?: RowSetProvider.newFactory().createCachedRowSet()
        }

        fun executeQueryAggregate(sb: StringBuilder): Int
        {
            return executeQueryAggregate(sb.toString())
        }

        fun executeQueryAggregate(sql: String): Int
        {
            executeQuery(sql).use { rs ->
                return if (rs.next()) rs.getInt(1) else -1
            }
        }

        fun doDuplicateInstanceCheck()
        {
            try
            {
                createDatabaseConnection()
            }
            catch (sqle: SQLException)
            {
                val next = sqle.nextException
                if (next != null
                 && next.message!!.contains("Another instance of Derby may have already booted the database"))
                {
                    logger.warn(CODE_DATABASE_IN_USE, "Failed multiple instance check, exiting.")
                    DialogUtil.showError("Database already in use - Dartzee will now exit.")
                    exitProcess(1)
                }
                else
                {
                    logger.logSqlException("", "", sqle)
                }
            }

        }

        fun createTableIfNotExists(tableName: String, columnSql: String): Boolean
        {
            val statement = "CREATE TABLE $tableName($columnSql)"

            try
            {
                executeUpdateUncaught(statement)
                logger.info(CODE_TABLE_CREATED, "Created $tableName")
            }
            catch (sqle: SQLException)
            {
                val state = sqle.sqlState
                if (state == TABLE_ALREADY_EXISTS)
                {
                    logger.info(CODE_TABLE_EXISTS, "$tableName already exists")
                }
                else
                {
                    logger.logSqlException(statement, "", sqle)
                }

                return false
            }

            return true
        }

        fun createTempTable(tableName: String, colStr: String): String?
        {
            val millis = System.currentTimeMillis()
            val fullTableName = "zzTmp_$tableName$millis"

            val success = createTableIfNotExists(fullTableName, colStr)
            return if (success)
            {
                fullTableName
            }
            else null
        }

        fun dropTable(tableName: String?): Boolean
        {
            val sql = "DROP TABLE $tableName"
            return executeUpdate(sql)
        }

        fun testConnection(dbPath: String): Boolean
        {
            try
            {
                createDatabaseConnection(dbPath, DartsClient.derbyDbName)
            }
            catch (t: Throwable)
            {
                logger.error(CODE_TEST_CONNECTION_ERROR, "Failed to establish test connection for path $dbPath", t)
                return false
            }

            return true
        }

        fun shutdownDerby(): Boolean
        {
            try
            {
                createDatabaseConnection(dbName = "jdbc:derby:;shutdown=true")
            }
            catch (sqle: SQLException)
            {
                val msg = sqle.message ?: ""
                if (msg.contains("shutdown"))
                {
                    //Derby ALWAYS throws an exception on shutdown.
                    return true
                }

                logger.logSqlException("jdbc:derby:;shutdown=true", "jdbc:derby:;shutdown=true", sqle)
            }

            return false
        }

        fun deleteRowsFromTable(tableName: String, rowIds: List<String>): Boolean
        {
            var success = true
            rowIds.chunked(50).forEach {
                val idStr = it.joinToString{rowId -> "'$rowId'"}
                val sql = "DELETE FROM $tableName WHERE RowId IN ($idStr)"
                success = executeUpdate(sql)
            }

            return success
        }
    }
}
