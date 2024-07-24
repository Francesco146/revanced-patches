package app.revanced.patches.youtube.shorts.components.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ElementsTopBarContainer
import app.revanced.util.fingerprint.LiteralValueFingerprint

object ShortsToolBarCreationFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/view/View;",
    literalSupplier = { ElementsTopBarContainer }
)