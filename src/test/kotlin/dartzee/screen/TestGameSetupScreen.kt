package dartzee.screen

import dartzee.`object`.GameLauncher
import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelX01
import dartzee.bean.getAllPlayers
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.total.DartzeeTotalRulePrime
import dartzee.db.DARTZEE_TEMPLATE
import dartzee.db.DartsMatchEntity
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartzeeRuleDto
import dartzee.ruleDtosEq
import dartzee.screen.dartzee.DartzeeRuleSetupScreen
import dartzee.updateSelection
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.mockk.*
import org.junit.Test

class TestGameSetupScreen: AbstractTest()
{
    private val gameLauncher = mockk<GameLauncher>(relaxed = true)

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        InjectedThings.gameLauncher = gameLauncher
    }

    @Test
    fun `Should respond to changing game type`()
    {
        val screen = GameSetupScreen()
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelX01>()

        screen.gameTypeComboBox.updateSelection(GameType.GOLF)
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelGolf>()
    }

    @Test
    fun `Should perform player selector validation when attempting to launch a game`()
    {
        val screen = GameSetupScreen()
        screen.btnLaunch.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must select at least 1 player.")
    }

    @Test
    fun `Should initialise the player selector with players from the DB`()
    {
        insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()

        val players = screen.playerSelector.tablePlayersToSelectFrom.getAllPlayers()
        players.map { it.name } shouldBe listOf("Alice", "Bob", "Clive")
    }

    @Test
    fun `Should launch a single game with the selected players and game params`()
    {
        val alice = insertPlayer(name = "Alice")
        insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val screen = GameSetupScreen()
        screen.initialise()
        screen.playerSelector.init(listOf(alice, clive))

        val gameParamsPanel = screen.gameParamFilterPanel as GameParamFilterPanelX01
        gameParamsPanel.spinner.value = 701

        screen.btnLaunch.doClick()
        verify { gameLauncher.launchNewGame(listOf(alice, clive), GameType.X01, "701", null) }
    }

    @Test
    fun `Should toggle the right components when switching between match types`()
    {
        val screen = GameSetupScreen()
        screen.initialise()

        //Default - single game
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Game"

        //First to
        screen.rdbtnFirstTo.doClick()
        screen.lblWins.isVisible shouldBe true
        screen.spinnerWins.isVisible shouldBe true
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Match"

        //Points-based
        screen.rdbtnPoints.doClick()
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe true
        screen.spinnerGames.isVisible shouldBe true
        screen.matchConfigPanel.components.toList() shouldContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Match"

        //Back to single game
        screen.rdbtnSingleGame.doClick()
        screen.lblWins.isVisible shouldBe false
        screen.spinnerWins.isVisible shouldBe false
        screen.lblGames.isVisible shouldBe false
        screen.spinnerGames.isVisible shouldBe false
        screen.matchConfigPanel.components.toList() shouldNotContain screen.panelPointBreakdown
        screen.btnLaunch.text shouldBe "Launch Game"
    }

    @Test
    fun `Should update based on whether a Dartzee template is selected`()
    {
        insertDartzeeTemplate(name = "Template")

        val screen = GameSetupScreen()
        screen.gameTypeComboBox.updateSelection(GameType.DARTZEE)

        screen.btnLaunch.isVisible shouldBe false
        screen.btnNext.isVisible shouldBe true

        val dartzeeParamPanel = screen.gameParamFilterPanel as GameParamFilterPanelDartzee
        dartzeeParamPanel.comboBox.selectedIndex = 2

        screen.btnLaunch.isVisible shouldBe true
        screen.btnNext.isVisible shouldBe false
    }

    @Test
    fun `Should retrieve Dartzee rules when launching a Dartzee game from a template`()
    {
        val templateId = insertDartzeeTemplate().rowId

        val ruleOne = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleOdd(), DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(totalRule = DartzeeTotalRulePrime())

        ruleOne.toEntity(1, DARTZEE_TEMPLATE, templateId).saveToDatabase()
        ruleTwo.toEntity(2, DARTZEE_TEMPLATE, templateId).saveToDatabase()

        val screen = makeGameSetupScreenReadyToLaunch()
        screen.gameTypeComboBox.updateSelection(GameType.DARTZEE)

        val dartzeeParamPanel = screen.gameParamFilterPanel as GameParamFilterPanelDartzee
        dartzeeParamPanel.comboBox.selectedIndex = 2

        screen.btnLaunch.doClick()

        verify { gameLauncher.launchNewGame(any(), GameType.DARTZEE, templateId, ruleDtosEq(listOf(ruleOne, ruleTwo))) }
    }

    @Test
    fun `Should launch a first-to match with the right parameters`()
    {
        val slot = slot<DartsMatchEntity>()
        every { gameLauncher.launchNewMatch(capture(slot), any()) } just runs

        val scrn = makeGameSetupScreenReadyToLaunch()

        scrn.rdbtnFirstTo.doClick()
        scrn.spinnerWins.value = 7

        scrn.btnLaunch.doClick()

        verify { gameLauncher.launchNewMatch(any(), null)}

        val match = slot.captured
        match.gameType shouldBe GameType.X01
        match.gameParams shouldBe "501"
        match.mode shouldBe MatchMode.FIRST_TO
        match.games shouldBe 7
        match.matchParams shouldBe ""
    }

    @Test
    fun `Should launch a points based match with the right parameters`()
    {
        val slot = slot<DartsMatchEntity>()
        every { gameLauncher.launchNewMatch(capture(slot), any()) } just runs

        val scrn = makeGameSetupScreenReadyToLaunch()
        scrn.gameTypeComboBox.updateSelection(GameType.GOLF)

        scrn.rdbtnPoints.doClick()
        scrn.spinnerGames.value = 8
        scrn.spinners[0].value = 15
        scrn.spinners[1].value = 9
        scrn.spinners[2].value = 6
        scrn.spinners[3].value = 3
        scrn.spinners[4].value = 2
        scrn.spinners[5].value = 1

        scrn.btnLaunch.doClick()

        verify { gameLauncher.launchNewMatch(any(), null)}

        val match = slot.captured
        match.gameType shouldBe GameType.GOLF
        match.gameParams shouldBe "18"
        match.mode shouldBe MatchMode.POINTS
        match.games shouldBe 8
        match.matchParams shouldBe DartsMatchEntity.constructPointsXml(15, 9, 6, 3, 2, 1)
    }

    @Test
    fun `Should perform validation on Dartzee mode when trying to hit Next`()
    {
        val p1 = insertPlayer(strategy = 1)
        val p2 = insertPlayer(strategy = 1)

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)
        setupScreen.btnNext.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You cannot select AI opponents for Dartzee.")
    }

    @Test
    fun `Should switch to the DartzeeRuleSetupScreen on Next, passing through the right parameters`()
    {
        val p1 = insertPlayer(strategy = -1)
        val p2 = insertPlayer(strategy = -1)

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)
        setupScreen.btnNext.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<DartzeeRuleSetupScreen>()

        val dartzeeScreen = currentScreen as DartzeeRuleSetupScreen
        dartzeeScreen.players.shouldContainExactly(p1, p2)
        dartzeeScreen.match shouldBe null
        dartzeeScreen.btnNext.text shouldBe "Launch Game >"
    }

    @Test
    fun `Should switch to the DartzeeRuleSetupScreen for a match`()
    {
        val p1 = insertPlayer(strategy = -1)
        val p2 = insertPlayer(strategy = -1)

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        setupScreen.rdbtnFirstTo.doClick()
        setupScreen.gameTypeComboBox.updateSelection(GameType.DARTZEE)
        setupScreen.btnNext.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<DartzeeRuleSetupScreen>()

        val dartzeeScreen = currentScreen as DartzeeRuleSetupScreen
        dartzeeScreen.players.shouldContainExactly(p1, p2)
        dartzeeScreen.btnNext.text shouldBe "Launch Match >"

        val match = dartzeeScreen.match!!
        match.games shouldBe 2
        match.mode shouldBe MatchMode.FIRST_TO
        match.gameParams shouldBe ""
        match.gameType shouldBe GameType.DARTZEE
        match.players.shouldContainExactly(p1, p2)
    }

    private fun makeGameSetupScreenReadyToLaunch(): GameSetupScreen
    {
        val p1 = insertPlayer(strategy = -1)
        val p2 = insertPlayer(strategy = -1)

        val setupScreen = GameSetupScreen()
        setupScreen.initialise()
        setupScreen.playerSelector.init(listOf(p1, p2))

        return setupScreen
    }
}