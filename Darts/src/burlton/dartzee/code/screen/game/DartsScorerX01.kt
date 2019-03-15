package burlton.dartzee.code.screen.game

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartHint
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.sumScore
import burlton.desktopcore.code.bean.AbstractTableRenderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class DartsScorerX01 : DartsScorerPausable()
{
    private val lblStartingScore = JLabel("X01")

    fun getLatestScoreRemaining(): Int
    {
        val model = tableScores.model

        val rowCount = model.rowCount
        return if (rowCount == 0)
        {
            Integer.parseInt(lblStartingScore.text)
        }
        else model.getValueAt(rowCount - 1, SCORE_COLUMN) as Int
    }

    init
    {
        lblStartingScore.horizontalAlignment = SwingConstants.CENTER
        lblStartingScore.font = Font("Trebuchet MS", Font.PLAIN, 16)
        panelNorth.add(lblStartingScore, BorderLayout.CENTER)
    }


    override fun initImpl(gameParams: String)
    {
        val startingScore = Integer.parseInt(gameParams)
        lblStartingScore.text = "$startingScore"

        tableScores.getColumn(SCORE_COLUMN).cellRenderer = ScorerRenderer()
        for (i in 0 until SCORE_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }
    }

    override fun playerIsFinished() = getLatestScoreRemaining() == 0

    /**
     * How many darts have been thrown?
     *
     * 3 * (rows - 1) + #(darts in the last row)
     */
    override fun getTotalScore(): Int
    {
        val rowCount = model.rowCount
        if (rowCount == 0)
        {
            return 0
        }

        var dartCount = Math.max((model.rowCount - 1) * 3, 0)

        //We now use this mid-game
        if (rowIsComplete(rowCount - 1) && !playerIsFinished())
        {
            return dartCount + 3
        }

        dartCount += getDartsForRow(rowCount - 1).size
        return dartCount
    }

    fun getDartsForRow(row: Int): List<Dart>
    {
        val ret = mutableListOf<Dart>()
        for (i in 0 until SCORE_COLUMN)
        {
            val drt = model.getValueAt(row, i) as Dart?
            if (drt != null && drt !is DartHint)
            {
                ret.add(drt)
            }
        }

        return ret
    }

    override fun rowIsComplete(rowNumber: Int) = model.getValueAt(rowNumber, SCORE_COLUMN) != null

    override fun clearCurrentRound()
    {
        val rowCount = model.rowCount
        if (rowCount == 0)
        {
            return
        }

        //If we've come into here by clicking 'pause', the latest round might be a completed one.
        //Only clear the round if it's 'unconfirmed'.
        val value = model.getValueAt(rowCount - 1, SCORE_COLUMN)
        if (value == null)
        {
            model.removeRow(rowCount - 1)
        }
    }

    override fun getNumberOfColumns() = SCORE_COLUMN + 1

    fun finaliseRoundScore(startingScore: Int, bust: Boolean)
    {
        removeHints()

        val row = model.rowCount - 1

        if (bust)
        {
            model.setValueAt(startingScore, row, SCORE_COLUMN)
        }
        else
        {
            val dartScore = sumScore(getDartsForRow(row))
            model.setValueAt(startingScore - dartScore, row, SCORE_COLUMN)
        }
    }

    override fun addDart(drt: Dart)
    {
        removeHints()

        super.addDart(drt)
    }

    fun addHint(drt: DartHint)
    {
        super.addDart(drt)
    }

    private fun removeHints()
    {
        val row = model.rowCount - 1
        if (row < 0)
        {
            return
        }

        for (i in 0 until SCORE_COLUMN)
        {
            if (model.getValueAt(row, i) is DartHint)
            {
                model.setValueAt(null, row, i)
            }
        }
    }


    /**
     * Inner classes
     */
    private class DartRenderer : AbstractTableRenderer<Dart>()
    {
        override fun getReplacementValue(drt: Dart?): Any
        {
            return when(drt)
            {
                null -> ""
                is DartHint -> "($drt)"
                else -> "$drt"
            }
        }

        override fun setCellColours(typedValue: Dart?, isSelected: Boolean)
        {
            foreground = if (isSelected)
            {
                if (typedValue is DartHint) Color.CYAN else Color.WHITE
            }
            else
            {
                if (typedValue is DartHint) Color.RED else Color.BLACK
            }

            val style = if (typedValue is DartHint) Font.ITALIC else Font.PLAIN

            font = Font(font.name, style, font.size)
        }

        override fun allowNulls(): Boolean
        {
            return true
        }
    }

    private inner class ScorerRenderer : DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            horizontalAlignment = SwingConstants.CENTER
            font = Font("Trebuchet MS", Font.BOLD, 15)
            val modelRow = table!!.convertRowIndexToModel(row)

            setColours(table, modelRow)
            return this
        }

        private fun setColours(table: JTable, modelRow: Int)
        {
            if (getDartsForRow(modelRow).isEmpty())
            {
                foreground = null
                background = null
                return
            }

            val tm = table.model
            val totalScore = (getScoreAt(tm, modelRow, 0)
                    + getScoreAt(tm, modelRow, 1)
                    + getScoreAt(tm, modelRow, 2))

            val fg = DartsColour.getScorerForegroundColour(totalScore.toDouble())
            val bg = DartsColour.getScorerBackgroundColour(totalScore.toDouble())

            foreground = fg
            background = bg
        }

        private fun getScoreAt(tm: TableModel, row: Int, col: Int): Int
        {
            val value = tm.getValueAt(row, col) as Dart? ?: return 0
            if (value is DartHint)
            {
                return 0
            }

            return value.getTotal()
        }
    }

    companion object
    {
        private const val SCORE_COLUMN = 3

        /**
         * Static methods
         */
        fun factory(parent: GamePanelX01): DartsScorerX01
        {
            val scorer = DartsScorerX01()
            scorer.setParent(parent)
            return scorer
        }
    }
}