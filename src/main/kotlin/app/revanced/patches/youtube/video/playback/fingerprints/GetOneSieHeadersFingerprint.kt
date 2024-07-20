package app.revanced.patches.youtube.video.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object GetOneSieHeadersFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf("Content-Type", "application/x-protobuf"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "<init>"
                && methodDef.parameterTypes.firstOrNull() == "Ljava/lang/String;"
    }
)