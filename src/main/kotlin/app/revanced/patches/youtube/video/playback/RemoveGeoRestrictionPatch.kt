package app.revanced.patches.youtube.video.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.video.playback.fingerprints.GetOneSieHeadersFingerprint
import app.revanced.patches.youtube.video.playback.fingerprints.GetWatchHeadersFingerprint
import app.revanced.patches.youtube.video.playback.fingerprints.TestFingerprint
import app.revanced.util.getStringInstructionIndex
import app.revanced.util.getTargetIndexWithMethodReferenceNameOrThrow
import app.revanced.util.patch.BaseBytecodePatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
object RemoveGeoRestrictionPatch : BaseBytecodePatch(
    name = "Geo Restriction Bypass",
    description = "Adds options to bypass YouTube geo-restrictions.",
    dependencies = setOf(SettingsPatch::class),
    compatiblePackages = COMPATIBLE_PACKAGE,
    fingerprints = setOf(
        GetOneSieHeadersFingerprint,
        GetWatchHeadersFingerprint,
        TestFingerprint
    )
) {
    private const val INTEGRATIONS_GEO_RESTRICTION_CLASS_DESCRIPTOR =
        "$VIDEO_PATH/georestrictions/RemoveGeoRestrictionPatch;"

    override fun execute(context: BytecodeContext) {
        arrayOf(
            GetOneSieHeadersFingerprint,
            GetWatchHeadersFingerprint
        ).forEach { fingerprint ->
            fingerprint.resultOrThrow().let {
                it.mutableMethod.apply {

                    val insertIndex = getTargetIndexWithMethodReferenceNameOrThrow("put")
                    val targetMapRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC
                    val insertKeyRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD
                    val insertValueRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerE

                    addInstructionsWithLabels(
                        insertIndex + 1,
                        """
                        invoke-static {}, $INTEGRATIONS_GEO_RESTRICTION_CLASS_DESCRIPTOR->shouldChangeLocation()Z
                        move-result v$insertKeyRegister
                        if-eqz v$insertKeyRegister, :shouldNotChangeLocation
                        const-string v$insertKeyRegister, "X-Forwarded-For"
                        invoke-static {}, $INTEGRATIONS_GEO_RESTRICTION_CLASS_DESCRIPTOR->getIp()Ljava/lang/String;
                        move-result-object v$insertValueRegister
                        invoke-interface {v$targetMapRegister, v$insertKeyRegister, v$insertValueRegister}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                        """,
                        ExternalLabel(
                            "shouldNotChangeLocation", getInstruction(insertIndex + 1)
                        )
                    )
                }
            }
        }

        TestFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val insertIndex = getStringInstructionIndex("Content-Length") + 3
                val targetMapRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC
                val insertKeyRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD
                val insertValueRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerE

                addInstructionsWithLabels(
                    insertIndex + 1,
                    """
                    invoke-static {}, $INTEGRATIONS_GEO_RESTRICTION_CLASS_DESCRIPTOR->shouldChangeLocation()Z
                    move-result v$insertKeyRegister
                    if-eqz v$insertKeyRegister, :shouldNotChangeLocation
                    const-string v$insertKeyRegister, "X-Forwarded-For"
                    invoke-static {}, $INTEGRATIONS_GEO_RESTRICTION_CLASS_DESCRIPTOR->getIp()Ljava/lang/String;
                    move-result-object v$insertValueRegister
                    invoke-virtual {v$targetMapRegister, v$insertKeyRegister, v$insertValueRegister}, Lorg/chromium/net/UrlRequest${'$'}Builder;->addHeader(Ljava/lang/String;Ljava/lang/String;)Lorg/chromium/net/UrlRequest${'$'}Builder;
                    """,
                    ExternalLabel(
                        "shouldNotChangeLocation", getInstruction(insertIndex + 1)
                    )
                )
            }
        }
    }
}