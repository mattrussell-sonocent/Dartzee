package burlton.dartzee.code.screen.game

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.screen.ScreenCache
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JFrame

abstract class AbstractDartsGameScreen: JFrame(), WindowListener
{
    var haveLostFocus = false

    open fun getScreenHeight() = 675
    protected fun setScreenSize(playerCount: Int)
    {
        val newSize = Dimension(520 + (playerCount * SCORER_WIDTH), getScreenHeight())
        size = newSize
        isResizable = false
    }

    /**
     * Abstract fns
     */
    abstract fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    abstract fun fireAppearancePreferencesChanged()

    /**
     * Hook for when a GameId has been clicked and the screen is already visible.
     */
    open fun displayGame(gameId: String)
    {
        toFront()
        state = Frame.NORMAL
    }
    open fun startNextGameIfNecessary()
    {
        //Do nothing by default
    }

    /**
     * WindowListener
     */
    override fun windowClosed(arg0: WindowEvent)
    {
        ScreenCache.removeDartsGameScreen(this)
    }
    override fun windowDeactivated(arg0: WindowEvent)
    {
        haveLostFocus = true
    }

    override fun windowActivated(arg0: WindowEvent){}
    override fun windowClosing(arg0: WindowEvent){}
    override fun windowDeiconified(arg0: WindowEvent){}
    override fun windowIconified(arg0: WindowEvent){}
    override fun windowOpened(arg0: WindowEvent){}
}