package burlton.dartzee.code.achievements.x01

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BEST_GAME
import burlton.dartzee.code.achievements.AbstractAchievementBestGame
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementX01BestGame : AbstractAchievementBestGame()
{
    override val achievementRef = ACHIEVEMENT_REF_X01_BEST_GAME
    override val name = "Leg-up"
    override val desc = "Best game of 501"
    override val gameType = GAME_TYPE_X01
    override val gameParams = "501"

    override val redThreshold = 99
    override val orangeThreshold = 60
    override val yellowThreshold = 42
    override val greenThreshold = 30
    override val blueThreshold = 24
    override val pinkThreshold = 12
    override val maxValue = 9

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_X01_BEST_GAME
}