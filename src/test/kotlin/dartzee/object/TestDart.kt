package dartzee.`object`

import dartzee.db.CLOCK_TYPE_DOUBLES
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.db.CLOCK_TYPE_TREBLES
import dartzee.helper.AbstractTest
import dartzee.helper.makeDart
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.awt.Point

class TestDart: AbstractTest()
{
    @Test
    fun `Should correctly report a double`()
    {
        Dart(1, 1).isDouble() shouldBe false
        Dart(1, 2).isDouble() shouldBe true
        Dart(1, 3).isDouble() shouldBe false
    }

    @Test
    fun `Should correctly report a treble`()
    {
        Dart(1, 1).isTriple() shouldBe false
        Dart(1, 2).isTriple() shouldBe false
        Dart(1, 3).isTriple() shouldBe true
    }

    @Test
    fun `Should correctly report the total`()
    {
        Dart(20, 0).getTotal() shouldBe 0
        Dart(17, 2).getTotal() shouldBe 34
        Dart(19, 3).getTotal() shouldBe 57
        Dart(2, 1).getTotal() shouldBe 2
    }

    @Test
    fun `Should correctly report golf score`()
    {
        makeDart(1, 1, segmentType = SegmentType.OUTER_SINGLE, golfHole = 1).getGolfScore() shouldBe 4
        makeDart(1, 1, segmentType = SegmentType.OUTER_SINGLE, golfHole = 2).getGolfScore() shouldBe 5
        makeDart(3, 0, segmentType = SegmentType.MISS, golfHole = 3).getGolfScore() shouldBe 5

        val outerOne = makeDart(1, 1, SegmentType.OUTER_SINGLE)
        outerOne.getGolfScore(1) shouldBe 4
        outerOne.getGolfScore(2) shouldBe 5
    }

    @Test
    fun `Should report x and y coords correctly`()
    {
        val dart = Dart(1, 1)
        dart.getX().shouldBeNull()
        dart.getY().shouldBeNull()

        val dartTwo = Dart(1, 1, Point(7, 3))
        dartTwo.getX() shouldBe 7
        dartTwo.getY() shouldBe 3
    }

    @Test
    fun `Should report hit score correctly`()
    {
        Dart(19, 0).getHitScore() shouldBe 0
        Dart(19, 1).getHitScore() shouldBe 19
        Dart(19, 2).getHitScore() shouldBe 19
        Dart(19, 3).getHitScore() shouldBe 19
    }

    @Test
    fun `Should render correctly`()
    {
        Dart(19, 0).getRendered() shouldBe "0"
        Dart(19, 1).getRendered() shouldBe "19"
        Dart(20, 2).getRendered() shouldBe "D20"
        Dart(15, 3).getRendered() shouldBe "T15"

        "${Dart(15, 3)}" shouldBe "T15"
    }

    @Test
    fun `Should report the correct segment type to aim for`()
    {
        Dart(19, 1).getSegmentTypeToAimAt() shouldBe SegmentType.OUTER_SINGLE
        Dart(20, 2).getSegmentTypeToAimAt() shouldBe SegmentType.DOUBLE
        Dart(3, 3).getSegmentTypeToAimAt() shouldBe SegmentType.TREBLE
    }

    @Test
    fun `equals and hashcode`()
    {
        val dart = Dart(2, 3).also { it.ordinal = 1 }
        val dartMatch = Dart(2, 3).also { it.ordinal = 1 }
        val wrongScore = Dart(3, 3).also { it.ordinal = 1 }
        val wrongMult = Dart(2, 2).also { it.ordinal = 1 }
        val wrongOrdinal = Dart(2, 3).also { it.ordinal = 2 }

        dart.shouldBe(dartMatch)
        dart.hashCode() shouldBe dartMatch.hashCode()

        dart.shouldNotBe(wrongScore)
        dart.hashCode() shouldNotBe wrongScore.hashCode()

        dart.shouldNotBe(wrongMult)
        dart.hashCode() shouldNotBe wrongMult.hashCode()

        dart.shouldNotBe(wrongOrdinal)
        dart.hashCode() shouldNotBe wrongOrdinal.hashCode()
    }

    @Test
    fun `hit clock target - single`()
    {
        makeDart(1, 0, startingScore = 1).hitClockTarget(CLOCK_TYPE_STANDARD) shouldBe false
        makeDart(1, 1, startingScore = 1).hitClockTarget(CLOCK_TYPE_STANDARD) shouldBe true
        makeDart(1, 2, startingScore = 1).hitClockTarget(CLOCK_TYPE_STANDARD) shouldBe true
        makeDart(1, 3, startingScore = 1).hitClockTarget(CLOCK_TYPE_STANDARD) shouldBe true

        makeDart(1, 1, startingScore = 2).hitClockTarget(CLOCK_TYPE_STANDARD) shouldBe false
    }

    @Test
    fun `hit clock target - double`()
    {
        makeDart(1, 0, startingScore = 1).hitClockTarget(CLOCK_TYPE_DOUBLES) shouldBe false
        makeDart(1, 1, startingScore = 1).hitClockTarget(CLOCK_TYPE_DOUBLES) shouldBe false
        makeDart(1, 2, startingScore = 1).hitClockTarget(CLOCK_TYPE_DOUBLES) shouldBe true
        makeDart(1, 3, startingScore = 1).hitClockTarget(CLOCK_TYPE_DOUBLES) shouldBe false

        makeDart(1, 2, startingScore = 2).hitClockTarget(CLOCK_TYPE_DOUBLES) shouldBe false
    }

    @Test
    fun `hit clock target - treble`()
    {
        makeDart(1, 0, startingScore = 1).hitClockTarget(CLOCK_TYPE_TREBLES) shouldBe false
        makeDart(1, 1, startingScore = 1).hitClockTarget(CLOCK_TYPE_TREBLES) shouldBe false
        makeDart(1, 2, startingScore = 1).hitClockTarget(CLOCK_TYPE_TREBLES) shouldBe false
        makeDart(1, 3, startingScore = 1).hitClockTarget(CLOCK_TYPE_TREBLES) shouldBe true

        makeDart(1, 3, startingScore = 2).hitClockTarget(CLOCK_TYPE_TREBLES) shouldBe false
    }
}