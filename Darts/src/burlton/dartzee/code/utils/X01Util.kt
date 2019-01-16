package burlton.dartzee.code.utils

import burlton.core.code.util.StringUtil
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.ai.AbstractDartsModel


fun isBust(score: Int, lastDart: Dart): Boolean
{
    return (score < 0
            || score == 1
            || score == 0 && !lastDart.isDouble())
}

/**
 * Apply the Mercy Rule if:
 * - It has been enabled for this AI
 * - The starting score was odd and < the threshold (configurable per AI)
 * - The current score is even, meaing we have bailed ourselves out in some way
 */
fun shouldStopForMercyRule(model: AbstractDartsModel, startingScore: Int, currentScore: Int): Boolean
{
    val mercyThreshold = model.mercyThreshold
    return if (mercyThreshold == -1) {
        false
    } else startingScore < mercyThreshold
            && startingScore % 2 != 0
            && currentScore % 2 == 0

}

/**
 * 50, 40, 38, 36, 34, ... , 8, 4, 2
 */
fun isCheckoutDart(drt: Dart): Boolean
{
    val startingScore = drt.startingScore
    return isCheckoutScore(startingScore)

}
fun isCheckoutScore(score: Int): Boolean
{
    return if (score == 50)
    {
        true
    }
    else score % 2 == 0 //Even
            && score <= 40
}

fun isFinishRound(round: MutableList<Dart>): Boolean
{
    val drt = round.last()
    return drt.isDouble() && drt.getTotal() == drt.startingScore
}

/**
 * Refactored out of GameWrapper for use in game stats panel
 */
fun getScoringDarts(allDarts: MutableList<Dart>?, scoreCutOff: Int): MutableList<Dart>
{
    return if (allDarts == null)
    {
        mutableListOf()
    }
    else
        allDarts.filter { d -> d.startingScore > scoreCutOff }.toMutableList()

}

fun calculateThreeDartAverage(darts: MutableList<Dart>, scoreCutOff: Int): Double
{
    val scoringDarts = getScoringDarts(darts, scoreCutOff)
    if (scoringDarts.isEmpty())
    {
        return -1.0
    }

    val amountScored = sumScore(scoringDarts).toDouble()

    return amountScored / scoringDarts.size * 3
}

fun sumScore(darts: MutableList<Dart>): Int
{
    return darts.stream().mapToInt { d -> d.getTotal() }.sum()
}

/**
 * Shanghai: T20, D20, 20 in any order.
 *
 * Check there are 3 darts, all are 20s, the sum is 120 and there is at least one single
 */
fun isShanghai(darts: MutableList<Dart>): Boolean
{
    return darts.size == 3
      && sumScore(darts) == 120
      && darts.stream().allMatch{it.score == 20}
      && darts.stream().anyMatch {it.multiplier == 1}
}

/**
 * Sorts the array of darts deterministically and then returns a String representation.
 *
 * (5, T20, 1) -> "T20, 5, 1".
 */
fun getSortedDartStr(darts: MutableList<Dart>): String
{
    val sortedDarts = darts.sortedWith(compareByDescending<Dart>{it.getTotal()}.thenByDescending{it.multiplier})
    return StringUtil.toDelims(sortedDarts, ", ")
}
