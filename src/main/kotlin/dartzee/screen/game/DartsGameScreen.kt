package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.db.GameEntity
import dartzee.screen.ScreenCache

/**
 * DartsGameScreen
 * Simple screen which wraps up a single game panel
 */
class DartsGameScreen(game: GameEntity, totalPlayers: Int) : AbstractDartsGameScreen(totalPlayers, game.gameType)
{
    var gamePanel: DartsGamePanel<*, *, *> = DartsGamePanel.factory(this, game)

    init
    {
        //Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        //Initialise some basic properties of the tab, such as visibility of components etc
        gamePanel.initBasic(totalPlayers)

        title = gamePanel.gameTitle

        //Add the single game tab and set visible
        contentPane.add(gamePanel)
    }

    override fun fireAppearancePreferencesChanged()
    {
        gamePanel.fireAppearancePreferencesChanged()
    }

    override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    {
        gamePanel.achievementUnlocked(playerId, achievement)
    }
}