package dartzee.`object`

import dartzee.db.CLOCK_TYPE_DOUBLES
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.db.CLOCK_TYPE_TREBLES
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestSegmentType: AbstractTest()
{
    @Test
    fun `Should return the correct multiplier based on segment type`()
    {
        SegmentType.DOUBLE.getMultiplier() shouldBe 2
        SegmentType.TREBLE.getMultiplier() shouldBe 3
        SegmentType.INNER_SINGLE.getMultiplier() shouldBe 1
        SegmentType.OUTER_SINGLE.getMultiplier() shouldBe 1
        SegmentType.MISS.getMultiplier() shouldBe 0
        SegmentType.MISSED_BOARD.getMultiplier() shouldBe 0
    }

    @Test
    fun `Should return the right golf score based on segment type`()
    {
        SegmentType.DOUBLE.getGolfScore() shouldBe 1
        SegmentType.TREBLE.getGolfScore() shouldBe 2
        SegmentType.INNER_SINGLE.getGolfScore() shouldBe 3
        SegmentType.OUTER_SINGLE.getGolfScore() shouldBe 4
        SegmentType.MISS.getGolfScore() shouldBe 5
        SegmentType.MISSED_BOARD.getGolfScore() shouldBe 5
    }

    @Test
    fun `Should get the right segment type to aim for in RTC`()
    {
        getSegmentTypeForClockType(CLOCK_TYPE_STANDARD) shouldBe SegmentType.OUTER_SINGLE
        getSegmentTypeForClockType(CLOCK_TYPE_DOUBLES) shouldBe SegmentType.DOUBLE
        getSegmentTypeForClockType(CLOCK_TYPE_TREBLES) shouldBe SegmentType.TREBLE
    }

}