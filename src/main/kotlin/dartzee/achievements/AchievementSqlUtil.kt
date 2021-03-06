package dartzee.achievements

import dartzee.`object`.*

const val LAST_ROUND_FROM_PARTICIPANT = "CEIL(CAST(pt.FinalScore AS DECIMAL)/3)"

fun getGolfSegmentCases(): String
{
    val sb = StringBuilder()
    sb.append(" WHEN drt.SegmentType = '${SegmentType.DOUBLE}' THEN 1")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.TREBLE}' THEN 2")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.INNER_SINGLE}' THEN 3")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.OUTER_SINGLE}' THEN 4")
    sb.append(" ELSE 5")

    return sb.toString()
}