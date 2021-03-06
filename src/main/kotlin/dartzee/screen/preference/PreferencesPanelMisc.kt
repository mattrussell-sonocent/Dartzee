package dartzee.screen.preference

import dartzee.bean.SliderAiSpeed
import dartzee.core.bean.NumberField
import dartzee.core.util.setFontSize
import dartzee.utils.*
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class PreferencesPanelMisc : AbstractPreferencesPanel(), ActionListener, PropertyChangeListener, ChangeListener
{
    override val title = "Misc"

    private val panelCenter = JPanel()
    private val lblDefaultAiSpeed = JLabel("Default AI speed")
    val slider = SliderAiSpeed(false)
    val chckbxAiAutomaticallyFinish = JCheckBox("AI automatically finish")
    val chckbxCheckForUpdates = JCheckBox("Automatically check for updates")
    private val lblRowsToShow = JLabel("Rows to show on Leaderboards")
    val nfLeaderboardSize = NumberField(10, 200)
    val chckbxShowAnimations = JCheckBox("Play sounds/animations")

    init
    {
        add(panelCenter, BorderLayout.CENTER)
        nfLeaderboardSize.columns = 10
        panelCenter.layout = MigLayout("", "[][grow][]", "[][][][][][]")

        lblDefaultAiSpeed.setFontSize(16)
        lblRowsToShow.setFontSize(16)
        nfLeaderboardSize.setFontSize(16)
        chckbxAiAutomaticallyFinish.setFontSize(16)
        chckbxCheckForUpdates.setFontSize(16)
        chckbxShowAnimations.setFontSize(16)

        panelCenter.add(lblDefaultAiSpeed, "cell 0 0")
        panelCenter.add(slider, "cell 1 0")
        panelCenter.add(lblRowsToShow, "cell 0 1,alignx leading")
        panelCenter.add(nfLeaderboardSize, "cell 1 1,alignx leading")
        panelCenter.add(chckbxAiAutomaticallyFinish, "flowx,cell 0 2")
        panelCenter.add(chckbxCheckForUpdates, "flowx,cell 0 3")
        panelCenter.add(chckbxShowAnimations, "cell 0 4")

        slider.addChangeListener(this)
        nfLeaderboardSize.addPropertyChangeListener(this)
        chckbxAiAutomaticallyFinish.addActionListener(this)
        chckbxCheckForUpdates.addActionListener(this)
        chckbxShowAnimations.addActionListener(this)
    }

    override fun refreshImpl(useDefaults: Boolean)
    {
        slider.value = PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED, useDefaults)
        nfLeaderboardSize.value = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE, useDefaults)
        chckbxAiAutomaticallyFinish.isSelected = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, useDefaults)
        chckbxCheckForUpdates.isSelected = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, useDefaults)
        chckbxShowAnimations.isSelected = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, useDefaults)
    }

    override fun saveImpl()
    {
        val aiSpd = slider.value
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, aiSpd)

        val leaderboardSize = nfLeaderboardSize.getNumber()
        PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, leaderboardSize)

        val aiAuto = chckbxAiAutomaticallyFinish.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, aiAuto)

        val checkForUpdates = chckbxCheckForUpdates.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, checkForUpdates)

        val showAnimations = chckbxShowAnimations.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, showAnimations)
    }

    override fun hasOutstandingChanges() =
            slider.value != PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED)
                || nfLeaderboardSize.value != PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE)
                || chckbxAiAutomaticallyFinish.isSelected != PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE)
                || chckbxCheckForUpdates.isSelected != PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES)
                || chckbxShowAnimations.isSelected != PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS)


    override fun stateChanged(e: ChangeEvent?) = stateChanged()
    override fun propertyChange(evt: PropertyChangeEvent?) = stateChanged()
    override fun actionPerformed(e: ActionEvent?) = stateChanged()
}
