package dartzee.screen

import dartzee.clickComponent
import dartzee.helper.AbstractTest
import dartzee.helper.assertExits
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import javax.swing.JButton

class TestMenuScreen: AbstractTest()
{
    @Test
    fun `Should exit when exit pressed`()
    {
        val scrn = MenuScreen()

        assertExits(0) {
            scrn.clickComponent<JButton>("Exit")
        }
    }

    @Test
    fun `Should go to the game setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickComponent<JButton>("New Game")
        ScreenCache.currentScreen().shouldBeInstanceOf<GameSetupScreen>()
    }

    @Test
    fun `Should go to the player management screen`()
    {
        val scrn = MenuScreen()
        scrn.clickComponent<JButton>("Manage Players")
        ScreenCache.currentScreen().shouldBeInstanceOf<PlayerManagementScreen>()
    }

    @Test
    fun `Should go to the reporting setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickComponent<JButton>("Game Report")
        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingSetupScreen>()
    }

    @Test
    fun `Should go to the leaderboards screen`()
    {
        val scrn = MenuScreen()
        scrn.clickComponent<JButton>("Leaderboards")
        ScreenCache.currentScreen().shouldBeInstanceOf<LeaderboardsScreen>()
    }

    @Test
    fun `Should go to the utilities screen`()
    {
        val scrn = MenuScreen()
        scrn.clickComponent<JButton>("Utilities")
        ScreenCache.currentScreen().shouldBeInstanceOf<UtilitiesScreen>()
    }
}