package app.revanced.patches.youtube.video.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object GetWatchHeadersFingerprint : MethodFingerprint(
    returnType = "Ljava/util/Map;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.DECLARED_SYNCHRONIZED,
    parameters = emptyList(),
    strings = listOf("Content-Type", "application/x-protobuf")
)
