package burlton.dartzee.code.achievements.x01

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_NO_MERCY
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.LAST_ROUND_FROM_PARTICIPANT
import burlton.dartzee.code.db.GAME_TYPE_X01
import java.net.URL

class AchievementX01NoMercy: AbstractAchievement()
{
    override val name = "No Mercy"
    override val desc = "Finishes from 3, 5, 7 or 9 in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_NO_MERCY

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 4
    override val blueThreshold = 5
    override val pinkThreshold = 7
    override val maxValue = 10

    override fun populateForConversion(playerIds: String)
    {
        val sb = StringBuilder()
        sb.append("SELECT drt.*, p.Name, g.LocalId")
        sb.append("FROM Game g, Participant pt, Dart drt, Player p")
        sb.append("WHERE pt.GameId = g.RowId")
        sb.append("AND pt.PlayerId = p.RowId")
        sb.append("AND g.GameType = $GAME_TYPE_X01")
        sb.append("AND pt.FinalScore > -1")
        sb.append("AND $LAST_ROUND_FROM_PARTICIPANT = drt.RoundNumber")
        sb.append("AND pt.RowId = drt.ParticipantId")
        sb.append("AND drt.PlayerId = pt.PlayerId")
        sb.append("AND drt.Ordinal = 1")
        sb.append("AND drt.StartingScore IN (3, 5, 7, 9)")
    }

    override fun getIconURL(): URL
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}