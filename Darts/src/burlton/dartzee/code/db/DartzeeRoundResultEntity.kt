package burlton.dartzee.code.db

import burlton.dartzee.code.dartzee.DartzeeRoundResult

class DartzeeRoundResultEntity: AbstractEntity<DartzeeRoundResultEntity>()
{
    var playerId: String = ""
    var participantId: String = ""
    var roundNumber: Int = -1
    var ruleNumber: Int = -1
    var success: Boolean = false
    var successScore: Int = -1

    override fun getTableName() = "DartzeeRoundResult"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "ParticipantId VARCHAR(36) NOT NULL, "
                + "RoundNumber INT NOT NULL, "
                + "RuleNumber INT NOT NULL, "
                + "Success BOOLEAN NOT NULL, "
                + "SuccessScore INT NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "ParticipantId", "RoundNumber"))
    }

    fun toDto(): DartzeeRoundResult = DartzeeRoundResult(ruleNumber, success, false, successScore)

    companion object
    {
        fun factoryAndSave(dto: DartzeeRoundResult, pt: ParticipantEntity, roundNumber: Int): DartzeeRoundResultEntity
        {
            val entity = DartzeeRoundResultEntity()
            entity.assignRowId()
            entity.ruleNumber = dto.ruleNumber
            entity.success = dto.success
            entity.playerId = pt.playerId
            entity.participantId = pt.rowId
            entity.roundNumber = roundNumber
            entity.successScore = dto.successScore

            entity.saveToDatabase()
            return entity
        }
    }
}