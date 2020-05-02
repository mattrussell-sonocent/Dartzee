package dartzee.screen.reporting

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.util.TableUtil
import dartzee.reporting.ReportParameters
import dartzee.reporting.ReportResultWrapper
import dartzee.reporting.runReport
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.sql.Timestamp
import javax.swing.JButton
import javax.swing.JPanel

class ReportingResultsScreen : EmbeddedScreen()
{
    var rp: ReportParameters? = null
    private var cachedRows = emptyList<Array<Any>>()

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
        buildTable(true, emptyList())
    }

    private fun buildTable(runSql: Boolean, excludedColumns: List<String>)
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

        model.addRows(cachedRows)

        tableResults.setRowName("game")
        tableResults.model = model
        tableResults.setColumnWidths("60;160;-1;DT;DT;100")
        tableResults.sortBy(0, false)

        setRenderersAndComparators()
        stripOutRemovedColumns(excludedColumns)
    }

    private fun setRenderersAndComparators()
    {
        tableResults.setRenderer(3, TableUtil.TimestampRenderer())
        tableResults.setRenderer(4, TableUtil.TimestampRenderer())

        tableResults.setComparator(3, compareBy<Timestamp> { it })
        tableResults.setComparator(4, compareBy<Timestamp> { it })
    }

    private fun stripOutRemovedColumns(excludedColumns: List<String>)
    {
        val columns = tableResults.columnCount
        for (i in columns - 1 downTo 0)
        {
            val columnName = tableResults.getColumnName(i)
            if (excludedColumns.contains(columnName))
            {
                tableResults.removeColumn(i)
            }
        }
    }

    override fun getBackTarget() = ScreenCache.get<ReportingSetupScreen>()

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnConfigureColumns -> {
                val dlg = ConfigureReportColumnsDialog()
                dlg.setLocationRelativeTo(this)
                dlg.isVisible = true

                buildTable(false, dlg.excludedColumns())
            }
            else -> super.actionPerformed(arg0)
        }
    }
}
