package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.DartzeeRuleSummaryPanel
import burlton.dartzee.code.utils.getAllPossibleSegments
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.getOuterSegments
import burlton.dartzee.test.helper.makeDart
import burlton.dartzee.test.helper.makeRoundResultEntities
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartzeeRuleSummaryPanel: AbstractDartsTest()
{
    @Test
    fun `Should show the high score panel by default`()
    {
        val summaryPanel = makeSummaryPanel()
        summaryPanel.components.toList().shouldContainExactly(summaryPanel.panelHighScore)
    }

    @Test
    fun `Should swap out the high score panel and update the carousel for roundnumber greater than 1`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val results = makeRoundResultEntities(DartzeeRoundResult(2, true, 35))
        val darts = listOf(makeDart(19, 1, SEGMENT_TYPE_OUTER_SINGLE), makeDart(7, 1, SEGMENT_TYPE_DOUBLE), makeDart(2, 1, SEGMENT_TYPE_INNER_SINGLE))
        summaryPanel.update(results, darts, 103, 2)

        summaryPanel.components.toList().shouldContainExactly(carousel)
        verify { carousel.update(results, darts, 103) }
    }

    @Test
    fun `Should return all valid segments if on the scoring round`()
    {
        val summaryPanel = makeSummaryPanel()

        summaryPanel.getValidSegments() shouldBe getAllPossibleSegments()
    }

    @Test
    fun `Should return the carousel segments once past round one`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)
        every { carousel.getValidSegments() } returns getOuterSegments()

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        summaryPanel.update(listOf(), listOf(), 103, 2)

        summaryPanel.getValidSegments() shouldBe getOuterSegments()
    }

    @Test
    fun `Should swap in the carousel and call gameFinished`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        summaryPanel.gameFinished()

        summaryPanel.components.toList().shouldContainExactly(carousel)
        verify { carousel.gameFinished() }
    }

    private fun makeSummaryPanel() = DartzeeRuleSummaryPanel(mockk(relaxed = true))
}