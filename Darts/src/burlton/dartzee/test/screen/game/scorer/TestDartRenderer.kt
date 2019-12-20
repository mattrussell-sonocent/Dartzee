package burlton.dartzee.test.screen.game.scorer

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartHint
import burlton.dartzee.code.screen.game.scorer.DartRenderer
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import java.awt.Font

class TestDartRenderer: AbstractDartsTest()
{
    @Test
    fun `Should render DartHints in brackets, and regular darts without`()
    {
        val renderer = DartRenderer()

        renderer.getReplacementValue(DartHint(20, 3)) shouldBe "(T20)"
        renderer.getReplacementValue(Dart(20, 3)) shouldBe "T20"
    }

    @Test
    fun `Should render normal darts correctly, and be responsive to selection`()
    {
        val renderer = DartRenderer()

        renderer.setCellColours(Dart(20, 3), false)
        renderer.foreground shouldBe Color.BLACK
        renderer.font.style shouldBe Font.PLAIN

        renderer.setCellColours(Dart(20, 3), true)
        renderer.foreground shouldBe Color.WHITE
        renderer.font.style shouldBe Font.PLAIN
    }

    @Test
    fun `Should render dart hints in red italic, and be responsive to selection`()
    {
        val renderer = DartRenderer()

        renderer.setCellColours(DartHint(20, 3), false)
        renderer.foreground shouldBe Color.RED
        renderer.font.style shouldBe Font.ITALIC

        renderer.setCellColours(DartHint(20, 3), true)
        renderer.foreground shouldBe Color.CYAN
        renderer.font.style shouldBe Font.ITALIC
    }
}