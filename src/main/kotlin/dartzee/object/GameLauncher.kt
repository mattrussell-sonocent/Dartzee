package dartzee.`object`

import dartzee.core.util.Debug
import dartzee.core.util.DialogUtil
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.*
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.dartzee.DartzeeMatchScreen
import dartzee.screen.game.golf.GolfMatchScreen
import dartzee.screen.game.rtc.RoundTheClockMatchScreen
import dartzee.screen.game.x01.X01MatchScreen
import dartzee.utils.insertDartzeeRules

object GameLauncher
{
    fun launchNewMatch(match: DartsMatchEntity, dartzeeDtos: List<DartzeeRuleDto>? = null)
    {
        val scrn = factoryMatchScreen(match, match.players)

        val game = GameEntity.factoryAndSave(match)

        insertDartzeeRules(game, dartzeeDtos)

        val panel = scrn.addGameToMatch(game)
        panel.startNewGame(match.players)
    }

    fun launchNewGame(players: List<PlayerEntity>, gameType: Int, gameParams: String, dartzeeDtos: List<DartzeeRuleDto>? = null)
    {
        //Create and save a game
        val gameEntity = GameEntity.factoryAndSave(gameType, gameParams)

        insertDartzeeRules(gameEntity, dartzeeDtos)

        //Construct the screen and factory a tab
        val scrn = DartsGameScreen(gameEntity, players.size)
        scrn.isVisible = true
        scrn.gamePanel.startNewGame(players)
    }

    fun loadAndDisplayGame(gameId: String)
    {
        val existingScreen = ScreenCache.getDartsGameScreen(gameId)
        if (existingScreen != null)
        {
            existingScreen.displayGame(gameId)
            return
        }

        //Screen isn't currently visible, so look for the game on the DB
        val gameEntity = GameEntity().retrieveForId(gameId, false)
        if (gameEntity == null)
        {
            DialogUtil.showError("Game $gameId does not exist.")
            return
        }

        val matchId = gameEntity.dartsMatchId
        if (matchId.isEmpty())
        {
            loadAndDisplaySingleGame(gameEntity)
        }
        else
        {
            loadAndDisplayMatch(matchId, gameId)
        }
    }

    private fun loadAndDisplaySingleGame(gameEntity: GameEntity)
    {
        //We've found a game, so construct a screen and initialise it
        val playerCount = gameEntity.getParticipantCount()
        val scrn = DartsGameScreen(gameEntity, playerCount)
        scrn.isVisible = true

        //Now try to load the game
        try
        {
            scrn.gamePanel.loadGame()
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
            DialogUtil.showError("Failed to load Game #" + gameEntity.rowId)
            scrn.dispose()
            ScreenCache.removeDartsGameScreen(scrn)
        }

    }

    private fun loadAndDisplayMatch(matchId: String, originalGameId: String)
    {
        val allGames = GameEntity.retrieveGamesForMatch(matchId)

        val firstGame = allGames.first()
        val lastGame = allGames[allGames.size - 1]

        val match = DartsMatchEntity().retrieveForId(matchId)
        match!!.cacheMetadataFromGame(lastGame)

        val scrn = factoryMatchScreen(match, firstGame.retrievePlayersVector())

        try
        {
            allGames.forEach {
                val panel = scrn.addGameToMatch(it)
                panel.loadGame()
            }

            scrn.displayGame(originalGameId)
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
            DialogUtil.showError("Failed to load Match #$matchId")
            scrn.dispose()
            ScreenCache.removeDartsGameScreen(scrn)
        }

        scrn.updateTotalScores()
    }

    private fun factoryMatchScreen(match: DartsMatchEntity, players: List<PlayerEntity>) =
        when (match.gameType)
        {
            GAME_TYPE_X01 -> X01MatchScreen(match, players)
            GAME_TYPE_ROUND_THE_CLOCK -> RoundTheClockMatchScreen(match, players)
            GAME_TYPE_GOLF -> GolfMatchScreen(match, players)
            GAME_TYPE_DARTZEE -> DartzeeMatchScreen(match, players)
            else -> X01MatchScreen(match, players)
        }
}