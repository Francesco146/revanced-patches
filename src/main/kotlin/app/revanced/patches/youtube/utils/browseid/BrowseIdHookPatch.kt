package app.revanced.patches.youtube.utils.browseid

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.litho.LithoFilterPatch
import app.revanced.patches.shared.litho.fingerprints.PathBuilderFingerprint
import app.revanced.patches.youtube.utils.browseid.fingerprints.BrowseIdClassFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SHARED_PATH
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.getStringInstructionIndex
import app.revanced.util.getTargetIndex
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    dependencies = [
        LithoFilterPatch::class,
        SharedResourceIdPatch::class
    ]
)
object BrowseIdHookPatch : BytecodePatch(
    setOf(
        BrowseIdClassFingerprint,
        PathBuilderFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$SHARED_PATH/BrowseId;"

    override fun execute(context: BytecodeContext) {

        /**
         * This class handles BrowseId.
         * Pass an instance of this class to integrations to use Java Reflection.
         */
        BrowseIdClassFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val targetIndex = getStringInstructionIndex("VL") - 1
                val targetReference = getInstruction<ReferenceInstruction>(targetIndex).reference
                val targetClass = context.findClass((targetReference as FieldReference).definingClass)!!.mutableClass

                targetClass.methods.find { method -> method.name == "<init>" }
                    ?.apply {
                        val browseIdFieldIndex = getTargetIndex(Opcode.IPUT_OBJECT)
                        val browseIdFieldName =
                            (getInstruction<ReferenceInstruction>(browseIdFieldIndex).reference as FieldReference).name

                        addInstructions(
                            1, """
                                const-string v0, "$browseIdFieldName"
                                invoke-static {p0, v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->initialize(Ljava/lang/Object;Ljava/lang/String;)V
                                """
                        )
                    } ?: throw PatchException("BrowseIdClass not found!")
            }
        }

        /**
         * Set BrowseId to integrations.
         */
        PathBuilderFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->setBrowseIdFromField()V"
                )
            }
        }
    }
}