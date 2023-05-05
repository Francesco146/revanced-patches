package app.revanced.patches.youtube.layout.seekbar.seekbarcolor.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.annotation.YouTubeCompatibility
import app.revanced.patches.shared.fingerprints.ControlsOverlayStyleFingerprint
import app.revanced.patches.youtube.layout.seekbar.seekbarcolor.bytecode.fingerprints.*
import app.revanced.patches.youtube.misc.litho.patch.LithoThemePatch
import app.revanced.patches.youtube.misc.resourceid.patch.SharedResourceIdPatch
import app.revanced.util.integrations.Constants.INTEGRATIONS_PATH
import app.revanced.util.integrations.Constants.SEEKBAR
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("custom-seekbar-color-bytecode-patch")
@DependsOn(
    [
        LithoThemePatch::class,
        SharedResourceIdPatch::class
    ]
)
@YouTubeCompatibility
@Version("0.0.1")
class SeekbarColorBytecodePatch : BytecodePatch(
    listOf(
        ControlsOverlayParentFingerprint,
        ControlsOverlayStyleFingerprint,
        SeekbarColorFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SeekbarColorFingerprint.result?.mutableMethod?.let { method ->
            with (method.implementation!!.instructions) {
                val insertIndex = this.indexOfFirst {
                    (it as? WideLiteralInstruction)?.wideLiteral == SharedResourceIdPatch.timeBarPlayedDarkLabelId
                } + 2

                val insertRegister = (elementAt(insertIndex) as OneRegisterInstruction).registerA

                method.addInstructions(
                    insertIndex + 1, """
                        invoke-static {v$insertRegister}, $SEEKBAR->enableCustomSeekbarColorDarkMode(I)I
                        move-result v$insertRegister
                        """
                )
            }
        } ?: return SeekbarColorFingerprint.toErrorResult()

        ControlsOverlayStyleFingerprint.result?.let { parentResult ->
            ProgressColorFingerprint.also { it.resolve(context, parentResult.classDef) }.result?.mutableMethod?.addInstructions(
                0, """
                    invoke-static {p1}, $SEEKBAR->enableCustomSeekbarColor(I)I
                    move-result p1
                    """
            ) ?: return ProgressColorFingerprint.toErrorResult()
        } ?: return ControlsOverlayStyleFingerprint.toErrorResult()

        ControlsOverlayParentFingerprint.result?.let { parentResult ->
            ControlsOverlayFingerprint.also { it.resolve(context, parentResult.classDef) }.result?.mutableMethod?.addInstruction(
                0,
                "sput-object p1, $INTEGRATIONS_PATH/utils/ReVancedUtils;->context:Landroid/content/Context;"
            ) ?: return ControlsOverlayFingerprint.toErrorResult()
        } ?: return ControlsOverlayParentFingerprint.toErrorResult()

        LithoThemePatch.injectCall("$SEEKBAR->resumedProgressBarColor(I)I")

        return PatchResultSuccess()
    }
}