package burlton.dartzee.code.db.sanity

import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.screen.TableModelDialog
import burlton.desktopcore.code.util.DialogUtil
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.table.DefaultTableModel

abstract class AbstractSanityCheckResult
{
    abstract fun getResultsModel(): DefaultTableModel
    abstract fun getDescription(): String
    abstract fun getCount(): Int

    fun getResultsDialog(): TableModelDialog
    {
        val t = getScrollTable()
        t.model = getResultsModel()

        val deleteAction = getDeleteAction(t)

        if (deleteAction != null)
        {
            t.addKeyAction(KeyEvent.VK_DELETE, "Delete", deleteAction)
        }

        return TableModelDialog(getDescription(), t)
    }

    open fun getDeleteAction(t: ScrollTable): AbstractAction?
    {
        return null
    }

    open fun getScrollTable() = ScrollTable()

    open fun autoFix()
    {
        DialogUtil.showError("No auto-fix available.")
    }

    override fun toString() = getDescription()
}
