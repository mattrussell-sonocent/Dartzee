package dartzee.screen.preference

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeDisabled
import com.github.alexburlton.swingtest.shouldBeEnabled
import dartzee.helper.AbstractRegistryTest
import org.junit.Test
import javax.swing.JButton

abstract class AbstractPreferencePanelTest<T: AbstractPreferencesPanel>: AbstractRegistryTest()
{
    abstract fun checkUiFieldValuesAreDefaults(panel: T)
    abstract fun checkUiFieldValuesAreNonDefaults(panel: T)
    abstract fun setUiFieldValuesToNonDefaults(panel: T)
    abstract fun checkPreferencesAreSetToNonDefaults()
    abstract fun factory(): T

    @Test
    fun `should restore defaults appropriately`()
    {
        val panel = factory()

        setUiFieldValuesToNonDefaults(panel)

        panel.refresh(true)

        checkUiFieldValuesAreDefaults(panel)
    }

    @Test
    fun `should set fields to their defaults when first loaded`()
    {
        clearPreferences()

        val panel = factory()
        panel.refresh(false)

        checkUiFieldValuesAreDefaults(panel)
    }

    @Test
    fun `should save preferences appropriately`()
    {
        clearPreferences()

        val panel = factory()
        setUiFieldValuesToNonDefaults(panel)
        panel.clickChild<JButton>("Apply")

        checkPreferencesAreSetToNonDefaults()
    }

    @Test
    fun `should display stored preferences correctly`()
    {
        val panel = factory()
        setUiFieldValuesToNonDefaults(panel)
        panel.clickChild<JButton>("Apply")

        panel.refresh(true)
        panel.refresh(false)

        checkUiFieldValuesAreNonDefaults(panel)
    }

    @Test
    fun `apply button should be disabled by default`()
    {
        val panel = factory()
        panel.refresh(false)

        panel.getChild<JButton>("Apply").shouldBeDisabled()
    }

    @Test
    fun `apply button should respond to UI changes correctly`()
    {
        clearPreferences()

        val panel = factory()
        panel.refresh(false)
        panel.getChild<JButton>("Apply").shouldBeDisabled()

        panel.clickChild<JButton>("Restore Defaults")
        panel.getChild<JButton>("Apply").shouldBeDisabled()

        setUiFieldValuesToNonDefaults(panel)
        panel.getChild<JButton>("Apply").shouldBeEnabled()

        panel.clickChild<JButton>("Restore Defaults")
        panel.getChild<JButton>("Apply").shouldBeDisabled()

        setUiFieldValuesToNonDefaults(panel)
        panel.clickChild<JButton>("Apply")
        panel.getChild<JButton>("Apply").shouldBeDisabled()

        panel.clickChild<JButton>("Restore Defaults")
        panel.getChild<JButton>("Apply").shouldBeEnabled()

        setUiFieldValuesToNonDefaults(panel)
        panel.getChild<JButton>("Apply").shouldBeDisabled()
    }
}