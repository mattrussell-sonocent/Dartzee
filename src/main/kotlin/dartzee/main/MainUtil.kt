package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.CoreRegistry.INSTANCE_STRING_DEVICE_ID
import dartzee.core.util.CoreRegistry.INSTANCE_STRING_USER_NAME
import dartzee.core.util.CoreRegistry.instance
import dartzee.core.util.DialogUtil
import dartzee.logging.*
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings.logger
import java.util.*
import javax.swing.UIManager

fun setLookAndFeel()
{
    if (!DartsClient.isAppleOs())
    {
        setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
    }
}

fun setLookAndFeel(laf: String)
{
    try
    {
        UIManager.setLookAndFeel(laf)
    }
    catch (e: Throwable)
    {
        logger.error(CODE_LOOK_AND_FEEL_ERROR, "Failed to load laf $laf", e)
        DialogUtil.showError("Failed to load Look & Feel 'Nimbus'.")
    }

    logger.info(CODE_LOOK_AND_FEEL_SET, "Set look and feel to $laf")
}

fun setLoggingContextFields()
{
    logger.addToContext(KEY_USERNAME, getUsername())
    logger.addToContext(KEY_APP_VERSION, DARTS_VERSION_NUMBER)
    logger.addToContext(KEY_OPERATING_SYSTEM, DartsClient.operatingSystem)
    logger.addToContext(KEY_DEVICE_ID, getDeviceId())
    logger.addToContext(KEY_DEV_MODE, DartsClient.devMode)
}

fun getDeviceId() = instance.get(INSTANCE_STRING_DEVICE_ID, null) ?: setDeviceId()
private fun setDeviceId(): String
{
    val deviceId = UUID.randomUUID().toString()
    instance.put(INSTANCE_STRING_DEVICE_ID, deviceId)
    return deviceId
}

fun getUsername() = instance.get(INSTANCE_STRING_USER_NAME, null) ?: setUsername()
private fun setUsername(): String
{
    logger.info(CODE_USERNAME_UNSET, "No username found, prompting for one now")

    var username: String? = null
    while (username == null || username.isEmpty())
    {
        username = DialogUtil.showInput("Enter your name", "Please enter your name (for debugging purposes).\nThis will only be asked for once.")
    }

    logger.info(CODE_USERNAME_SET, "$username has set their username", KEY_USERNAME to username)
    instance.put(INSTANCE_STRING_USER_NAME, username)
    return username
}