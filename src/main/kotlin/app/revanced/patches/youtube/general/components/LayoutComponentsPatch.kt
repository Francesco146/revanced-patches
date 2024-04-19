package app.revanced.patches.youtube.general.components

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.litho.LithoFilterPatch
import app.revanced.patches.youtube.general.components.fingerprints.AccountListFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.AccountListParentFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.AccountMenuFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.AccountSwitcherAccessibilityLabelFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.BottomUiContainerFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.CreateSearchSuggestionsFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.FloatingMicrophoneFingerprint
import app.revanced.patches.youtube.general.components.fingerprints.TrendingSearchConfigFingerprint
import app.revanced.patches.youtube.utils.fingerprints.AccountMenuParentFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL_CLASS_DESCRIPTOR
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.AccountSwitcherAccessibility
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.toolbar.ToolBarHookPatch
import app.revanced.patches.youtube.utils.viewgroup.ViewGroupMarginLayoutParamsHookPatch
import app.revanced.util.getTargetIndex
import app.revanced.util.getTargetIndexWithMethodReferenceName
import app.revanced.util.getTargetIndexWithReference
import app.revanced.util.getTargetIndexWithReferenceReversed
import app.revanced.util.getWideLiteralInstructionIndex
import app.revanced.util.literalInstructionBooleanHook
import app.revanced.util.patch.BaseBytecodePatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
object LayoutComponentsPatch : BaseBytecodePatch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",
    dependencies = setOf(
        LithoFilterPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class,
        ToolBarHookPatch::class,
        ViewGroupMarginLayoutParamsHookPatch::class
    ),
    compatiblePackages = COMPATIBLE_PACKAGE,
    fingerprints = setOf(
        AccountListParentFingerprint,
        AccountMenuParentFingerprint,
        AccountSwitcherAccessibilityLabelFingerprint,
        BottomUiContainerFingerprint,
        CreateSearchSuggestionsFingerprint,
        FloatingMicrophoneFingerprint,
        TrendingSearchConfigFingerprint
    )
) {
    private const val CUSTOM_FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/CustomFilter;"
    private const val LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/LayoutComponentsFilter;"

    override fun execute(context: BytecodeContext) {

        // region patch for hide account menu

        // for you tab
        AccountListParentFingerprint.resultOrThrow().let { parentResult ->
            AccountListFingerprint.resolve(context, parentResult.classDef)

            AccountListFingerprint.resultOrThrow().let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.startIndex + 3
                    val targetInstruction = getInstruction<FiveRegisterInstruction>(targetIndex)

                    addInstruction(
                        targetIndex,
                        "invoke-static {v${targetInstruction.registerC}, v${targetInstruction.registerD}}, " +
                                "$GENERAL_CLASS_DESCRIPTOR->hideAccountList(Landroid/view/View;Ljava/lang/CharSequence;)V"
                    )
                }
            }
        }

        // for tablet and old clients
        AccountMenuFingerprint.resolve(
            context,
            AccountMenuParentFingerprint.resultOrThrow().classDef
        )
        AccountMenuFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.startIndex + 2
                val targetInstruction = getInstruction<FiveRegisterInstruction>(targetIndex)

                addInstruction(
                    targetIndex,
                    "invoke-static {v${targetInstruction.registerC}, v${targetInstruction.registerD}}, " +
                            "$GENERAL_CLASS_DESCRIPTOR->hideAccountMenu(Landroid/view/View;Ljava/lang/CharSequence;)V"
                )
            }
        }

        // endregion

        // region patch for hide cast button

        val buttonClass = context.findClass("MediaRouteButton")
            ?: throw PatchException("MediaRouteButton class not found.")

        buttonClass.mutableClass.methods.find { it.name == "setVisibility" }?.apply {
            addInstructions(
                0, """
                    invoke-static {p1}, $GENERAL_CLASS_DESCRIPTOR->hideCastButton(I)I
                    move-result p1
                    """
            )
        } ?: throw PatchException("setVisibility method not found.")

        // endregion

        // region patch for hide floating microphone

        FloatingMicrophoneFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex
                val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex + 1, """
                        invoke-static {v$register}, $GENERAL_CLASS_DESCRIPTOR->hideFloatingMicrophone(Z)Z
                        move-result v$register
                        """
                )
            }
        }

        // endregion

        // region patch for hide handle

        AccountSwitcherAccessibilityLabelFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val constIndex = getWideLiteralInstructionIndex(AccountSwitcherAccessibility)
                val insertIndex = getTargetIndex(constIndex, Opcode.IF_EQZ)
                val setVisibilityIndex = getTargetIndexWithMethodReferenceName(insertIndex, "setVisibility")
                val visibilityRegister = getInstruction<FiveRegisterInstruction>(setVisibilityIndex).registerD

                addInstructions(
                    insertIndex, """
                        invoke-static {v$visibilityRegister}, $GENERAL_CLASS_DESCRIPTOR->hideHandle(I)I
                        move-result v$visibilityRegister
                        """
                )
            }
        }

        // endregion

        // region patch for hide search term thumbnail

        CreateSearchSuggestionsFingerprint.resultOrThrow().let { result ->
            result.mutableMethod.apply {
                val relativeIndex = getWideLiteralInstructionIndex(40)
                val replaceIndex = getTargetIndexWithReferenceReversed(
                    relativeIndex,
                    "Landroid/widget/ImageView;->setVisibility(I)V"
                ) - 1

                val jumpIndex = getTargetIndexWithReference(
                    relativeIndex,
                    "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"
                ) + 4

                val replaceIndexInstruction = getInstruction<TwoRegisterInstruction>(replaceIndex)
                val replaceIndexReference =
                    getInstruction<ReferenceInstruction>(replaceIndex).reference

                addInstructionsWithLabels(
                    replaceIndex + 1, """
                        invoke-static { }, $GENERAL_CLASS_DESCRIPTOR->hideSearchTermThumbnail()Z
                        move-result v${replaceIndexInstruction.registerA}
                        if-nez v${replaceIndexInstruction.registerA}, :hidden
                        iget-object v${replaceIndexInstruction.registerA}, v${replaceIndexInstruction.registerB}, $replaceIndexReference
                        """, ExternalLabel("hidden", getInstruction(jumpIndex))
                )
                removeInstruction(replaceIndex)
            }
        }

        // endregion

        // region patch for hide snack bar

        BottomUiContainerFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $GENERAL_CLASS_DESCRIPTOR->hideSnackBar()Z
                        move-result v0
                        if-eqz v0, :show
                        return-void
                        """, ExternalLabel("show", getInstruction(0))
                )
            }
        }

        // endregion

        // region patch for hide toolbar button

        ToolBarHookPatch.injectCall("$GENERAL_CLASS_DESCRIPTOR->hideToolBarButton")

        // endregion

        // region patch for hide trending searches

        TrendingSearchConfigFingerprint.literalInstructionBooleanHook(
            45399984,
            "$GENERAL_CLASS_DESCRIPTOR->hideTrendingSearches(Z)Z"
        )

        // endregion


        LithoFilterPatch.addFilter(CUSTOM_FILTER_CLASS_DESCRIPTOR)
        LithoFilterPatch.addFilter(LAYOUT_COMPONENTS_FILTER_CLASS_DESCRIPTOR)

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: GENERAL",
                "SETTINGS: HIDE_LAYOUT_COMPONENTS"
            )
        )

        SettingsPatch.updatePatchStatus(this)
    }
}
