package app.revanced.patches.youtube.shorts.components

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.shorts.components.fingerprints.ShortsToolBarCreationFingerprint
import app.revanced.patches.youtube.shorts.components.fingerprints.ShortsToolBarFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SHORTS_CLASS_DESCRIPTOR
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ElementsTopBarContainer
import app.revanced.util.getWideLiteralInstructionIndex
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

object ShortsToolBarPatch : BytecodePatch(
    setOf(ShortsToolBarFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        ShortsToolBarFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex
                val insertRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $SHORTS_CLASS_DESCRIPTOR->hideShortsToolBar(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        }
        ShortsToolBarCreationFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val literalIndex = getWideLiteralInstructionIndex(ElementsTopBarContainer)
                val viewIndex = indexOfFirstInstructionOrThrow(literalIndex) {
                    opcode == Opcode.CHECK_CAST
                            && (this as? ReferenceInstruction)?.reference?.toString() == "Landroid/view/ViewGroup;"
                }
                val viewRegister = getInstruction<OneRegisterInstruction>(viewIndex).registerA
                addInstruction(
                    viewIndex + 1,
                    "invoke-static {v$viewRegister}, $SHORTS_CLASS_DESCRIPTOR->addShortsToolBarButton(Landroid/view/ViewGroup;)V"
                )
            }
        }
    }
}
