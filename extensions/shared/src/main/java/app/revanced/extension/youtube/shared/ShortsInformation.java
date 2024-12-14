package app.revanced.extension.youtube.shared;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.utils.Logger;
import app.revanced.extension.shared.utils.Utils;
import app.revanced.extension.youtube.patches.utils.AlwaysRepeatPatch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static app.revanced.extension.shared.utils.ResourceUtils.getString;
import static app.revanced.extension.shared.utils.Utils.getFormattedTimeStamp;

/**
 * Hooking class for the current playing Short.
 */
@SuppressWarnings("all")
public final class ShortsInformation {
    private static String shortId = "";

    public static void setShortId(@NonNull String newlyLoadedVideoId) {
        if (shortId.equals(newlyLoadedVideoId))
            return;

        shortId = newlyLoadedVideoId;
    }

    /**
     * Id of the last video opened.  Includes Shorts.
     *
     * @return The id of the short, or an empty string if:
     * - No shorts are playing
     * - Incognito mode is enabled
     */
    @Nullable
    public static String getShortId() {
        return shortId;
    }
}
