package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.bean.DartzeeRuleSelector
import burlton.dartzee.code.dartzee.generateRuleDescription
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartzeeRuleCreationDialog : SimpleDialog(), ChangeListener
{
    var dartzeeRule: DartzeeRuleEntity? = null

    private val verificationPanel = DartzeeRuleVerificationPanel()
    private val panelCenter = JPanel()
    private val panelRuleStrength = JPanel()
    private val panelDarts = JPanel()
    private val rdbtnPanelDartScoreType = RadioButtonPanel()
    val rdbtnAllDarts = JRadioButton("All Darts")
    val dartOneSelector = DartzeeRuleSelector("Dart 1")
    val dartTwoSelector = DartzeeRuleSelector("Dart 2")
    val dartThreeSelector = DartzeeRuleSelector("Dart 3")
    val cbInOrder = JCheckBox("In Order")
    val targetSelector = DartzeeRuleSelector("Target")
    val rdbtnAtLeastOne = JRadioButton("At least one dart")
    val rdbtnNoDarts = JRadioButton("No darts")
    private val panelTotal = JPanel()
    val totalSelector = DartzeeRuleSelector("Total", true, true)
    private val panelRuleName = JPanel()
    val tfName = JTextField()
    val btnGenerateName = JButton()

    init
    {
        title = "Add Dartzee Rule"
        setSize(750, 600)
        setLocationRelativeTo(ScreenCache.getMainScreen())
        isModal = true

        add(panelRuleName, BorderLayout.NORTH)
        add(panelCenter, BorderLayout.CENTER)
        add(verificationPanel, BorderLayout.EAST)

        panelCenter.layout = MigLayout("", "[grow]", "[grow][grow][grow]")
        panelCenter.add(panelRuleStrength, "cell 0 1, growx")
        panelCenter.add(panelDarts, "cell 0 2, growx")
        panelCenter.add(panelTotal, "cell 0 3, growx")

        panelDarts.border = TitledBorder("")
        panelDarts.layout = MigLayout("", "[][]", "[][][][]")
        rdbtnPanelDartScoreType.add(rdbtnAllDarts)
        rdbtnPanelDartScoreType.add(rdbtnAtLeastOne)
        rdbtnPanelDartScoreType.add(rdbtnNoDarts)
        panelDarts.add(rdbtnPanelDartScoreType, "spanx")
        panelDarts.validate()

        panelTotal.layout = MigLayout("", "[]", "[]")
        panelTotal.border = TitledBorder("")

        panelTotal.add(totalSelector, "cell 0 0")

        panelRuleName.layout = BorderLayout(0, 0)
        panelRuleName.border = TitledBorder("")
        panelRuleName.add(tfName, BorderLayout.CENTER)
        panelRuleName.add(btnGenerateName, BorderLayout.EAST)
        tfName.preferredSize = Dimension(30, 50)
        btnGenerateName.preferredSize = Dimension(40, 50)

        tfName.horizontalAlignment = JTextField.CENTER
        tfName.font = Font(tfName.font.name, tfName.font.style, 24)
        tfName.isEditable = false

        rdbtnPanelDartScoreType.addActionListener(this)
        dartOneSelector.addActionListener(this)
        dartTwoSelector.addActionListener(this)
        dartThreeSelector.addActionListener(this)
        targetSelector.addActionListener(this)
        totalSelector.addActionListener(this)
        cbInOrder.addActionListener(this)

        cbInOrder.isSelected = true

        updateComponents()
    }

    fun populate(rule: DartzeeRuleEntity)
    {
        this.dartzeeRule = rule
        title = "Amend Dartzee Rule"

        if (rule.dart1Rule.isEmpty())
        {
            rdbtnNoDarts.isSelected = true
        }
        else if (rule.dart2Rule.isEmpty())
        {
            rdbtnAtLeastOne.isSelected = true

            targetSelector.populate(rule.dart1Rule)
        }
        else
        {
            cbInOrder.isSelected = rule.inOrder

            dartOneSelector.populate(rule.dart1Rule)
            dartTwoSelector.populate(rule.dart2Rule)
            dartThreeSelector.populate(rule.dart3Rule)
        }

        if (!rule.totalRule.isEmpty())
        {
            totalSelector.populate(rule.totalRule)
        }

        updateComponents()
        repaint()
    }

    override fun stateChanged(e: ChangeEvent?)
    {
        updateComponents()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (arg0.source !in listOf(btnOk, btnCancel))
        {
            updateComponents()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    override fun okPressed()
    {
        if (!valid())
        {
            return
        }

        val rule = dartzeeRule ?: DartzeeRuleEntity()

        populateRuleFromComponents(rule)

        dartzeeRule = rule

        dispose()
    }

    private fun populateRuleFromComponents(rule: DartzeeRuleEntity)
    {
        if (rdbtnAllDarts.isSelected)
        {
            rule.dart1Rule = dartOneSelector.getSelection().toDbString()
            rule.dart2Rule = dartTwoSelector.getSelection().toDbString()
            rule.dart3Rule = dartThreeSelector.getSelection().toDbString()
            rule.inOrder = cbInOrder.isSelected
        }
        else
        {
            rule.dart1Rule = if (rdbtnAtLeastOne.isSelected) targetSelector.getSelection().toDbString() else ""
            rule.dart2Rule = ""
            rule.dart3Rule = ""
        }

        if (totalSelector.isEnabled)
        {
            rule.totalRule = totalSelector.getSelection().toDbString()
        }
        else
        {
            rule.totalRule = ""
        }
    }

    private fun valid(): Boolean
    {
        if (rdbtnNoDarts.isSelected && !totalSelector.isEnabled)
        {
            DialogUtil.showError("You cannot create an empty rule")
            return false
        }

        if (rdbtnAtLeastOne.isSelected)
        {
            return targetSelector.valid()
        }
        else
        {
            return dartOneSelector.valid() && dartTwoSelector.valid() && dartThreeSelector.valid()
        }
    }

    private fun updateComponents()
    {
        if (rdbtnAllDarts.isSelected)
        {
            panelDarts.remove(targetSelector)
            panelDarts.add(dartOneSelector, "cell 0 1")
            panelDarts.add(dartTwoSelector, "cell 0 2")
            panelDarts.add(dartThreeSelector, "cell 0 3")
            panelDarts.add(cbInOrder, "cell 0 4")
        }
        else
        {
            panelDarts.remove(dartOneSelector)
            panelDarts.remove(dartTwoSelector)
            panelDarts.remove(dartThreeSelector)
            panelDarts.remove(cbInOrder)

            if (rdbtnAtLeastOne.isSelected)
            {
                panelDarts.add(targetSelector, "cell 0 1")
            }
            else
            {
                panelDarts.remove(targetSelector)
            }
        }

        repaint()
        panelDarts.revalidate()

        SwingUtilities.invokeLater{
            val rule = DartzeeRuleEntity().also { populateRuleFromComponents(it) }
            val ruleName = rule.generateRuleDescription()
            tfName.text = ruleName

            verificationPanel.updateRule(rule)
        }
    }
}