package dartzee.db

import dartzee.`object`.SegmentType
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.logging.CODE_INSTANTIATION_ERROR
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.KEY_SQL
import dartzee.utils.DatabaseUtil
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings.logger
import java.sql.*
import java.util.*
import java.util.regex.Pattern

abstract class AbstractEntity<E : AbstractEntity<E>>
{
    //DB Fields
    var rowId: String = ""
    var dtCreation = getSqlDateNow()
    var dtLastUpdate = DateStatics.END_OF_TIME

    //other variables
    var retrievedFromDb = false

    /**
     * Abstract fns
     */
    abstract fun getTableName(): String
    abstract fun getCreateTableSqlSpecific(): String

    /**
     * Default implementations
     */
    open fun getColumnsAllowedToBeUnset() = listOf<String>()
    open fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>) {}
    open fun cacheValuesWhileResultSetActive() {}

    /**
     * Helpers
     */
    private fun getColumnCount() = getColumns().size

    private fun getCreateTableColumnSql() = "RowId VARCHAR(36) PRIMARY KEY, DtCreation Timestamp NOT NULL, DtLastUpdate Timestamp NOT NULL, ${getCreateTableSqlSpecific()}"

    fun getColumns(): MutableList<String>
    {
        val columnCreateSql = getCreateTableColumnSql()
        val cols = columnCreateSql.split(",")

        return cols.map{ getColumnNameFromCreateSql(it) }.toMutableList()
    }

    fun getColumnsExcluding(vararg columnsToExclude: String): MutableList<String>
    {
        val columns = getColumns()
        columnsToExclude.forEach { columns.remove(it) }
        return columns
    }

    fun getTableNameUpperCase() = getTableName().toUpperCase()

    fun factoryFromResultSet(rs: ResultSet): E
    {
        val ret = factory()!!

        ret.rowId = rs.getString("RowId")
        ret.dtCreation = rs.getTimestamp("DtCreation")
        ret.dtLastUpdate = rs.getTimestamp("DtLastUpdate")
        ret.retrievedFromDb = true

        getColumnsExcluding("RowId", "DtCreation", "DtLastUpdate").forEach{
            val rsValue = getFieldFromResultSet(rs, it)
            ret.setField(it, rsValue)
        }

        ret.cacheValuesWhileResultSetActive()

        return ret
    }

    @Suppress("UNCHECKED_CAST")
    private fun factory(): E? =
        try
        {
            javaClass.getDeclaredConstructor().newInstance() as E
        }
        catch (t: Throwable)
        {
            logger.error(CODE_INSTANTIATION_ERROR, "Failed to instantiate ${javaClass.simpleName}", t)
            null
        }


    fun columnCanBeUnset(columnName: String) = getColumnsAllowedToBeUnset().contains(columnName)

    open fun assignRowId(): String
    {
        rowId = UUID.randomUUID().toString()
        return rowId
    }

    fun retrieveEntity(whereSql: String): E?
    {
        val entities = retrieveEntities(whereSql)
        if (entities.size > 1)
        {
            logger.error(CODE_SQL_EXCEPTION,
                    "Retrieved ${entities.size} rows from ${getTableName()}. Expected 1. WhereSQL [$whereSql]",
                    Throwable(),
                    KEY_SQL to whereSql)
        }

        return if (entities.isEmpty())
        {
            null
        }
        else entities.first()
    }

    fun retrieveEntities(whereSql: String = "", alias: String = ""): MutableList<E>
    {
        var queryWithFrom = "FROM ${getTableName()} $alias"
        if (!whereSql.isEmpty())
        {
            queryWithFrom += " WHERE $whereSql"
        }

        return retrieveEntitiesWithFrom(queryWithFrom, alias)
    }

    fun retrieveEntitiesWithFrom(whereSqlWithFrom: String, alias: String): MutableList<E>
    {
        val query = "SELECT " + getColumnsForSelectStatement(alias) + " " + whereSqlWithFrom

        val ret = mutableListOf<E>()

        try
        {
            DatabaseUtil.executeQuery(query).use { rs ->
                while (rs.next())
                {
                    val entity = factoryFromResultSet(rs)
                    ret.add(entity)
                }
            }
        }
        catch (sqle: SQLException)
        {
            logger.logSqlException(query, "", sqle)
        }

        return ret
    }

    fun retrieveForId(rowId: String, stackTraceIfNotFound: Boolean = true): E?
    {
        val entities = retrieveEntities("RowId = '$rowId'")
        if (entities.isEmpty())
        {
            if (stackTraceIfNotFound)
            {
                logger.error(CODE_SQL_EXCEPTION,
                        "Failed to find ${getTableName()} for ID [$rowId]",
                        Throwable(),
                        KEY_SQL to "RowId = '$rowId'")
            }

            return null
        }

        if (entities.size > 1)
        {
            logger.error(CODE_SQL_EXCEPTION,
                    "Found ${entities.size} ${getTableName()} rows for ID [$rowId]",
                    Throwable(),
                    KEY_SQL to "RowId = '$rowId'")
        }

        return entities[0]
    }

    fun deleteFromDatabase(): Boolean
    {
        val sql = "DELETE FROM ${getTableName()} WHERE RowId = '$rowId'"
        return DatabaseUtil.executeUpdate(sql)
    }

    fun deleteAll() = DatabaseUtil.executeUpdate("DELETE FROM ${getTableName()}")

    fun deleteWhere(whereSql: String): Boolean
    {
        val sql = "DELETE FROM ${getTableName()} WHERE $whereSql"
        return DatabaseUtil.executeUpdate(sql)
    }

    fun saveToDatabase(dtLastUpdate: Timestamp = getSqlDateNow())
    {
        this.dtLastUpdate = dtLastUpdate

        if (retrievedFromDb)
        {
            updateDatabaseRow()
        }
        else
        {
            insertIntoDatabase()
        }
    }

    private fun updateDatabaseRow()
    {
        val genericUpdate = buildUpdateQuery()
        var updateQuery = genericUpdate

        val conn = DatabaseUtil.borrowConnection()
        try
        {
            conn.prepareStatement(updateQuery).use { psUpdate ->
                updateQuery = writeValuesToStatement(psUpdate, 1, updateQuery)
                updateQuery = writeString(psUpdate, getColumnCount(), rowId, updateQuery)

                val timer = DurationTimer()
                psUpdate.executeUpdate()

                logger.logSql(updateQuery, genericUpdate, timer.getDuration())

                val updateCount = psUpdate.updateCount
                if (updateCount == 0)
                {
                    logger.error(CODE_SQL_EXCEPTION,
                            "0 rows updated for statement $updateQuery",
                            Throwable(),
                            KEY_SQL to updateQuery)
                }
            }
        }
        catch (sqle: SQLException)
        {
            logger.logSqlException(updateQuery, genericUpdate, sqle)
        }
        finally
        {
            DatabaseUtil.returnConnection(conn)
        }
    }

    private fun buildUpdateQuery(): String
    {
        //Some fun String manipulation
        var columns = getColumnsForSelectStatement()
        columns = columns.replaceFirst("RowId, ", "")
        columns = columns.replace(",", "=?,")
        columns += "=?"

        return "UPDATE ${getTableName()} SET $columns WHERE RowId=?"
    }

    private fun insertIntoDatabase()
    {
        val genericInsert = "INSERT INTO ${getTableName()} VALUES ${getInsertBlockForStatement()}"
        var insertQuery = genericInsert

        val conn = DatabaseUtil.borrowConnection()
        try
        {
            conn.prepareStatement(insertQuery).use { psInsert ->
                insertQuery = writeValuesToInsertStatement(insertQuery, psInsert)

                val timer = DurationTimer()
                psInsert.executeUpdate()
                logger.logSql(insertQuery, genericInsert, timer.getDuration())

                //Set this so we can call save() again on the same object and get the right behaviour
                retrievedFromDb = true
            }
        }
        catch (sqle: SQLException)
        {
            logger.logSqlException(insertQuery, genericInsert, sqle)
        }
        finally
        {
            DatabaseUtil.returnConnection(conn)
        }
    }

    fun writeValuesToInsertStatement(emptyStatement: String, psInsert: PreparedStatement, entityNumber: Int = 0): String
    {
        val adjustment = entityNumber * getColumnCount()

        var insertQuery = emptyStatement
        insertQuery = writeString(psInsert, 1 + adjustment, rowId, insertQuery)
        insertQuery = writeValuesToStatement(psInsert, 2 + adjustment, insertQuery)

        return insertQuery
    }

    fun writeValuesToStatement(ps: PreparedStatement, startIx: Int, emptyStatement: String): String
    {
        var ix = startIx
        var statementStr = emptyStatement
        getColumnsExcluding("RowId").forEach {
            statementStr = writeValue(ps, ix++, it, statementStr)
        }

        return statementStr
    }

    fun getInsertBlockForStatement() = "(${getColumns().joinToString{"?"}})"

    open fun createTable(): Boolean
    {
        val createdTable = DatabaseUtil.createTableIfNotExists(getTableName(), getCreateTableColumnSql())
        if (createdTable)
        {
            createIndexes()
        }

        return createdTable
    }

    fun createIndexes()
    {
        //Also create the indexes
        val indexes = mutableListOf<List<String>>()
        addListsOfColumnsForIndexes(indexes)

        indexes.forEach{
            createIndex(it)
        }
    }

    private fun createIndex(columns: List<String>)
    {
        val columnList = columns.joinToString()
        val indexName = columnList.replace(", ", "_")

        val statement = "CREATE INDEX $indexName ON ${getTableName()}($columnList)"
        DatabaseUtil.executeUpdate(statement)
    }

    fun getColumnsForSelectStatement(alias: String = ""): String
    {
        var cols = getColumns().toList()
        if (!alias.isEmpty())
        {
            cols = cols.map{ "$alias.$it" }
        }

        return cols.joinToString()
    }

    private fun getColumnNameFromCreateSql(col: String): String
    {
        var colSanitised = col
        colSanitised = colSanitised.trim()
        colSanitised = colSanitised.replace("(", "")
        colSanitised = colSanitised.replace(")", "")

        return colSanitised.split(" ")[0]
    }

    fun getField(fieldName: String): Any?
    {
        val getter = javaClass.getMethod("get$fieldName")
        return getter.invoke(this)
    }
    fun setField(fieldName: String, value: Any?)
    {
        val getMethod = javaClass.getMethod("get$fieldName")
        val setMethod = javaClass.getDeclaredMethod("set$fieldName", getMethod.returnType)

        setMethod.invoke(this, value)
    }
    fun getFieldType(fieldName: String): Class<*>
    {
        val getter = javaClass.getMethod("get$fieldName")
        return getter.returnType
    }

    /**
     * Write to statement methods
     */
    private fun writeLong(ps: PreparedStatement, ix: Int, value: Long, statementStr: String): String
    {
        ps.setLong(ix, value)
        return swapInValue(statementStr, value)
    }

    private fun writeInt(ps: PreparedStatement, ix: Int, value: Int, statementStr: String): String
    {
        ps.setInt(ix, value)
        return swapInValue(statementStr, value)
    }

    private fun writeDouble(ps: PreparedStatement, ix: Int, value: Double, statementStr: String): String
    {
        ps.setDouble(ix, value)
        return swapInValue(statementStr, value)
    }

    private fun writeString(ps: PreparedStatement, ix: Int, value: String, statementStr: String): String
    {
        ps.setString(ix, value)
        return swapInValue(statementStr, "'$value'")
    }

    private fun writeTimestamp(ps: PreparedStatement, ix: Int, value: Timestamp, statementStr: String): String
    {
        ps.setTimestamp(ix, value)
        return swapInValue(statementStr, "'$value'")
    }

    private fun writeBlob(ps: PreparedStatement, ix: Int, value: Blob, statementStr: String): String
    {
        ps.setBlob(ix, value)
        val blobStr = "Blob (dataLength: " + value.length() + ")"
        return swapInValue(statementStr, blobStr)
    }

    private fun writeBoolean(ps: PreparedStatement, ix: Int, value: Boolean, statementStr: String): String
    {
        ps.setBoolean(ix, value)
        return swapInValue(statementStr, value)
    }

    private fun swapInValue(statementStr: String, value: Any): String
    {
        return statementStr.replaceFirst(Pattern.quote("?").toRegex(), "" + value)
    }

    private fun writeValue(ps: PreparedStatement, ix: Int, columnName: String, statementStr: String): String
    {
        val value = getField(columnName)
        val type = getFieldType(columnName)
        return when (type)
        {
            String::class.java -> writeString(ps, ix, value as String, statementStr)
            Long::class.java -> writeLong(ps, ix, value as Long, statementStr)
            Int::class.java -> writeInt(ps, ix, value as Int, statementStr)
            Boolean::class.java -> writeBoolean(ps, ix, value as Boolean, statementStr)
            Timestamp::class.java -> writeTimestamp(ps, ix, value as Timestamp, statementStr)
            Blob::class.java -> writeBlob(ps, ix, value as Blob, statementStr)
            Double::class.java -> writeDouble(ps, ix, value as Double, statementStr)
            else -> writeString(ps, ix, "$value", statementStr)
        }
    }

    private fun getFieldFromResultSet(rs: ResultSet, columnName: String): Any?
    {
        val type = getFieldType(columnName)
        return when(type)
        {
            String::class.java -> rs.getString(columnName)
            Long::class.java -> rs.getLong(columnName)
            Int::class.java -> rs.getInt(columnName)
            Boolean::class.java -> rs.getBoolean(columnName)
            Timestamp::class.java -> rs.getTimestamp(columnName)
            Blob::class.java -> rs.getBlob(columnName)
            Double::class.java -> rs.getDouble(columnName)
            GameType::class.java -> GameType.valueOf(rs.getString(columnName))
            MatchMode::class.java -> MatchMode.valueOf(rs.getString(columnName))
            SegmentType::class.java -> SegmentType.valueOf(rs.getString(columnName))
            else -> null
        }
    }

    private fun getValueForLogging(value: Any): String
    {
        return when (value.javaClass)
        {
            String::class.java, Timestamp::class.java -> "'$value'"
            Blob::class.java -> "BLOB:${(value as Blob).length()}"
            else -> "$value"
        }
    }
}
