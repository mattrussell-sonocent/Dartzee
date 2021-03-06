package dartzee.screen.game

import dartzee.core.util.getSqlDateNow
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.Dartboard
import dartzee.screen.game.scorer.DartsScorerPausable
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PreferenceUtil

abstract class GamePanelPausable<S : DartsScorerPausable>(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int):
        DartsGamePanel<S, Dartboard, DefaultPlayerState<S>>(parent, game, totalPlayers)
{
    private var aiShouldPause = false

    /**
     * Abstract methods
     */
    abstract fun currentPlayerHasFinished(): Boolean

    override fun factoryDartboard() = Dartboard()
    override fun factoryState(pt: ParticipantEntity, scorer: S) = DefaultPlayerState(pt, scorer)

    override fun saveDartsAndProceed()
    {
        activeScorer.updatePlayerResult()

        saveDartsToDatabase()

        //This player has finished. The game isn't necessarily over though...
        if (currentPlayerHasFinished())
        {
            handlePlayerFinish()
        }

        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)

        val activePlayers = getActiveCount()
        if (activePlayers > 1 || (activePlayers == 1 && totalPlayers == 1))
        {
            //We always keep going if there's more than 1 active person in play
            nextTurn()
        }
        else if (activePlayers == 1)
        {
            activeScorer = getCurrentScorer()

            //Finish the game and set the last player's finishing position if we haven't already
            finishGameIfNecessary()

            if (!activeScorer.getPaused())
            {
                nextTurn()
            }
        }
        else
        {
            allPlayersFinished()
        }
    }

    override fun handlePlayerFinish(): Int
    {
        val finishPos = super.handlePlayerFinish()
        activeScorer.finalisePlayerResult(finishPos)
        return finishPos
    }

    override fun shouldAIStop(): Boolean
    {
        if (aiShouldPause)
        {
            aiShouldPause = false
            return true
        }

        return false
    }

    private fun finishGameIfNecessary()
    {
        if (gameEntity.isFinished())
        {
            return
        }

        val loser = getCurrentParticipant()
        loser.finishingPosition = totalPlayers
        loser.saveToDatabase()

        gameEntity.dtFinish = getSqlDateNow()
        gameEntity.saveToDatabase()

        parentWindow.startNextGameIfNecessary()

        //Display this player's result. If they're an AI and we have the preference, then
        //automatically play on.
        activeScorer.finalisePlayerResult(totalPlayers)
        if (loser.isAi() && PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE))
        {
            activeScorer.toggleResume()
        }
    }

    fun pauseLastPlayer()
    {
        if (!activeScorer.human && cpuThread != null)
        {
            aiShouldPause = true
            cpuThread!!.join()
        }

        //Now the player has definitely stopped, reset the round
        resetRound()

        //Set the current round number back to the previous round
        currentRoundNumber--
        updateLastRoundNumber(currentPlayerNumber, currentRoundNumber)

        dartboard.stopListening()
    }

    fun unpauseLastPlayer()
    {
        //If we've come through game load, we'll have disabled this.
        aiShouldPause = false
        slider.isEnabled = true

        nextTurn()
    }
}
