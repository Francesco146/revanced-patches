package app.revanced.patches.youtube.layout.tooltip

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patches.youtube.layout.tooltip.fingerprints.TooltipContentFullscreenFingerprint
import app.revanced.patches.youtube.layout.tooltip.fingerprints.TooltipContentViewFingerprint
import app.revanced.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.patch.BaseBytecodePatch
import app.revanced.util.resultOrThrow

@Suppress("unused")
object TooltipContentViewPatch : BaseBytecodePatch(
    name = "Hide tooltip content",
    description = "Hides the tooltip box that appears on first install.",
    dependencies = setOf(
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ),
    compatiblePackages = COMPATIBLE_PACKAGE,
    fingerprints = setOf(
        TooltipContentFullscreenFingerprint,
        TooltipContentViewFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        arrayOf(
            TooltipContentFullscreenFingerprint,
            TooltipContentViewFingerprint
        ).forEach { fingerprint ->
            fingerprint.resultOrThrow().mutableMethod.addInstruction(
                0,
                "return-void"
            )
        }

        SettingsPatch.updatePatchStatus(this)
    }
}
