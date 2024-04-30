package app.revanced.patches.music.misc.spoofappversion

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.music.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.copyXmlNode
import app.revanced.util.patch.BaseResourcePatch

@Suppress("unused")
object SpoofAppVersionPatch : BaseResourcePatch(
    name = "Spoof app version",
    description = "Adds options to spoof the YouTube Music client version. " +
            "This can remove the radio mode restriction in Canadian regions or disable real-time lyrics.",
    dependencies = setOf(
        SettingsPatch::class,
        SpoofAppVersionBytecodePatch::class
    ),
    compatiblePackages = COMPATIBLE_PACKAGE
) {
    override fun execute(context: ResourceContext) {

        /**
         * Copy arrays
         */
        context.copyXmlNode("music/spoofappversion/host", "values/arrays.xml", "resources")

        SettingsPatch.addSwitchPreference(
            CategoryType.MISC,
            "revanced_spoof_app_version",
            "false"
        )
        SettingsPatch.addPreferenceWithIntent(
            CategoryType.MISC,
            "revanced_spoof_app_version_target",
            "revanced_spoof_app_version"
        )

    }
}