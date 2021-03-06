package dartzee.core.util

import java.awt.Component
import java.awt.Container
import java.awt.Font
import java.awt.Window
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ChangeListener

/**
 * Recurses through all child components, returning an ArrayList of all children of the appropriate type
 */
inline fun <reified T> Container.getAllChildComponentsForType(): List<T>
{
    val ret = mutableListOf<T>()

    val components = components
    addComponents(ret, components, T::class.java)

    return ret
}

@Suppress("UNCHECKED_CAST")
fun <T> addComponents(ret: MutableList<T>, components: Array<Component>, desiredClazz: Class<T>)
{
    for (comp in components)
    {
        if (desiredClazz.isInstance(comp))
        {
            ret.add(comp as T)
        }

        if (comp is Container)
        {
            val subComponents = comp.components
            addComponents(ret, subComponents, desiredClazz)
        }
    }
}

fun Container.addActionListenerToAllChildren(listener: ActionListener)
{
    val children = getAllChildComponentsForType<JComponent>()
    children.forEach {
        if (it is JComboBox<*>)
        {
            if (!it.actionListeners.contains(listener))
            {
                it.addActionListener(listener)
            }
        }

        if (it is AbstractButton)
        {
            if (!it.actionListeners.contains(listener))
            {
                it.addActionListener(listener)
            }
        }
    }
}

fun Container.addChangeListenerToAllChildren(listener: ChangeListener)
{
    val children = getAllChildComponentsForType<JComponent>()
    children.forEach {
        if (it is JSpinner)
        {
            if (!it.changeListeners.contains(listener))
            {
                it.addChangeListener(listener)
            }
        }
    }
}

fun Container.enableChildren(enable: Boolean)
{
    getAllChildComponentsForType<Component>().forEach{
        it.isEnabled = enable
    }
}

fun Container.containsComponent(component: Component): Boolean
{
    val list = getAllChildComponentsForType<Component>()
    return list.contains(component)
}

fun createButtonGroup(vararg buttons: AbstractButton)
{
    if (buttons.isEmpty())
    {
        throw Exception("Trying to create empty ButtonGroup.")
    }

    val bg = ButtonGroup()
    buttons.forEach {
        bg.add(it)
    }

    //Enable the first button passed in by default
    buttons[0].isSelected = true
}

fun Container.getParentWindow(): Window?
{
    val myParent = parent ?: return null

    return if (myParent is Window) myParent else myParent.getParentWindow()
}

fun Component.setFontSize(size: Int)
{
    font = Font(font.name, font.style, size)
}