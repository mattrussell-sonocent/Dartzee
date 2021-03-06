package dartzee.db

import dartzee.ai.AbstractDartsModel
import dartzee.core.util.DateStatics.Companion.END_OF_TIME
import dartzee.core.util.getEndOfTimeSqlString
import javax.swing.ImageIcon

class PlayerEntity:AbstractEntity<PlayerEntity>()
{
    //DB Fields
    var name = ""
    var strategy = -1
    var strategyXml = ""
    var dtDeleted = END_OF_TIME
    var playerImageId = ""

    override fun getTableName() = "Player"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("Name varchar(25) NOT NULL, "
                + "Strategy int NOT NULL, "
                + "StrategyXml varchar(1000) NOT NULL, "
                + "DtDeleted timestamp NOT NULL, "
                + "PlayerImageId VARCHAR(36) NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        val nameIndex = listOf("Name")
        val strategyDtDeletedIndex = listOf("Strategy", "DtDeleted")

        indexes.add(nameIndex)
        indexes.add(strategyDtDeletedIndex)
    }

    override fun toString() = name

    /**
     * Helpers
     */
    fun isHuman() = (strategy == -1)
    fun isAi() = (strategy > -1)
    fun getModel(): AbstractDartsModel
    {
        val model = AbstractDartsModel.factoryForType(strategy)!!
        model.readXml(strategyXml)
        return model
    }

    fun getAvatar() = PlayerImageEntity.retrieveImageIconForId(playerImageId)
    fun getFlag() = getPlayerFlag(isHuman())

    companion object
    {
        val ICON_AI = ImageIcon(PlayerEntity::class.java.getResource("/flags/aiFlag.png"))
        val ICON_HUMAN = ImageIcon(PlayerEntity::class.java.getResource("/flags/humanFlag.png"))

        fun getPlayerFlag(human:Boolean) = if (human) ICON_HUMAN else ICON_AI

        /**
         * Retrieval methods
         */
        fun retrievePlayers(startingSql: String): List<PlayerEntity>
        {
            var whereSql = startingSql
            if (!startingSql.isEmpty())
            {
                whereSql += " AND "
            }
            whereSql += "DtDeleted = " + getEndOfTimeSqlString()

            return PlayerEntity().retrieveEntities(whereSql)
        }
        fun retrieveForName(name:String): PlayerEntity?
        {
            val whereSql = "Name = '$name' AND DtDeleted = ${getEndOfTimeSqlString()}"
            val players = PlayerEntity().retrieveEntities(whereSql)
            return if (players.isEmpty()) null else players[0]
        }

        fun factoryAndSaveHuman(name: String, avatarId: String): PlayerEntity
        {
            val entity = factoryCreate()

            entity.name = name
            entity.playerImageId = avatarId
            entity.saveToDatabase()
            entity.retrievedFromDb = true

            return entity
        }
        fun factoryCreate(): PlayerEntity
        {
            val entity = PlayerEntity()
            entity.assignRowId()

            return entity
        }
    }
}
