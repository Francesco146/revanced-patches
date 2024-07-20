package app.revanced.patches.youtube.video.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object TestFingerprint : MethodFingerprint(
    returnType = "Lcom/google/common/util/concurrent/ListenableFuture;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("Content-Type", "Content-Length"),
    parameters = emptyList(),
)