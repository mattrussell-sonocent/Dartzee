package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEqualTo
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeTotalRuleEqualTo: AbstractDartzeeRuleTest<DartzeeTotalRuleEqualTo>()
{
    override fun factory() = DartzeeTotalRuleEqualTo()

    @Test
    fun `Total validation`()
    {
        val rule = DartzeeTotalRuleEqualTo()
        rule.target = 55

        rule.isValidTotal(54) shouldBe false
        rule.isValidTotal(55) shouldBe true
        rule.isValidTotal(56) shouldBe false
    }
}