package app.revanced.patches.music.misc.backgroundplay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patches.music.misc.backgroundplay.fingerprints.BackgroundPlaybackFingerprint
import app.revanced.patches.music.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.util.patch.BaseBytecodePatch
import app.revanced.util.resultOrThrow

@Suppress("unused")
object BackgroundPlayPatch : BaseBytecodePatch(
    name = "Background play",
    description = "Enables playing music in the background.",
    compatiblePackages = COMPATIBLE_PACKAGE,
    fingerprints = setOf(BackgroundPlaybackFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        BackgroundPlaybackFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        const/4 v0, 0x1
                        return v0
                        """
                )
            }
        }

    }
}