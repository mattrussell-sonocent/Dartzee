package burlton.dartzee.code.screen.reporting

import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.reporting.ReportParameters
import burlton.dartzee.code.reporting.ReportResultWrapper
import burlton.dartzee.code.reporting.runReport
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.sql.Timestamp
import javax.swing.JButton
import javax.swing.JPanel

class ReportingResultsScreen : EmbeddedScreen()
{
    private var rp: ReportParameters? = null
    private var cachedRows: List<Array<Any?>>? = null

    private val btnConfigureColumns = JButton("Configure Columns...")
    private val tableResults = ScrollTableDartsGame()

    init
    {
        add(tableResults, BorderLayout.CENTER)

        val panel = JPanel()
        add(panel, BorderLayout.NORTH)

        panel.add(btnConfigureColumns)
        btnConfigureColumns.addActionListener(this)
    }

    override fun getScreenName() = "Game Report"

    override fun initialise()
    {
        buildTable(true)
    }

    private fun buildTable(runSql: Boolean)
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Game")
        model.addColumn("Type")
        model.addColumn("Players")
        model.addColumn("Start Date")
        model.addColumn("Finish Date")
        model.addColumn("Match")

        if (runSql)
        {
            val wrappers = runReport(rp)
            cachedRows = ReportResultWrapper.getTableRowsFromWrappers(wrappers)
        }

        for (row in cachedRows!!)
        {
            model.addRow(row)
        }

        tableResults.setRowName("game")
        tableResults.model = model
        tableResults.setColumnWidths("60;160;-1;DT;DT;100")
        tableResults.sortBy(0, false)

        setRenderersAndComparators()
        stripOutRemovedColumns()
    }

    private fun setRenderersAndComparators()
    {
        tableResults.setRenderer(3, TableUtil.TIMESTAMP_RENDERER)
        tableResults.setRenderer(4, TableUtil.TIMESTAMP_RENDERER)

        tableResults.setComparator(3) { t1: Timestamp, t2: Timestamp -> t1.compareTo(t2) }
        tableResults.setComparator(3) { t1: Timestamp, t2: Timestamp -> t1.compareTo(t2) }
    }

    private fun stripOutRemovedColumns()
    {
        val dlg = ScreenCache.getConfigureReportColumnsDialog()

        val columns = tableResults.columnCount
        for (i in columns - 1 downTo 0)
        {
            val columnName = tableResults.getColumnName(i)
            if (!dlg.includeColumn(columnName))
            {
                tableResults.removeColumn(i)
            }
        }
    }

    fun setReportParameters(rp: ReportParameters)
    {
        this.rp = rp
    }

    override fun getBackTarget() = ScreenCache.getScreen(ReportingSetupScreen::class.java)

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnConfigureColumns -> {
                val dlg = ScreenCache.getConfigureReportColumnsDialog()
                dlg.setLocationRelativeTo(this)
                dlg.isVisible = true

                buildTable(false)
            }
            else -> super.actionPerformed(arg0)
        }
    }
}