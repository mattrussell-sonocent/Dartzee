package dartzee.achievements.rtc

import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_GAMES_WON
import dartzee.achievements.AbstractAchievementGamesWon
import dartzee.game.GameType
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementClockGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_GAMES_WON
    override val gameType = GameType.ROUND_THE_CLOCK
    override val name = "Clock Winner"
    override val desc = "Total number of wins in Round the Clock"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_CLOCK_GAMES_WON
}