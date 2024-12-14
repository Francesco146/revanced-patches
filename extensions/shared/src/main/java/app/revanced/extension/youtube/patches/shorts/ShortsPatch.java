package app.revanced.extension.youtube.patches.shorts;

import static app.revanced.extension.shared.utils.ResourceUtils.getString;
import static app.revanced.extension.shared.utils.Utils.getChildView;
import static app.revanced.extension.shared.utils.Utils.hideViewUnderCondition;
import static app.revanced.extension.youtube.utils.ExtendedUtils.validateValue;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import app.revanced.extension.shared.utils.Logger;
import com.google.android.libraries.youtube.rendering.ui.pivotbar.PivotBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.revanced.extension.shared.utils.ResourceUtils;
import app.revanced.extension.shared.utils.Utils;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;
import app.revanced.extension.youtube.utils.VideoUtils;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class ShortsPatch {
    private static final boolean ENABLE_TIME_STAMP = Settings.ENABLE_TIME_STAMP.get();
    public static final boolean HIDE_SHORTS_NAVIGATION_BAR = Settings.HIDE_SHORTS_NAVIGATION_BAR.get();

    private static final int META_PANEL_BOTTOM_MARGIN;
    private static final double NAVIGATION_BAR_HEIGHT_PERCENTAGE;

    static {
        if (HIDE_SHORTS_NAVIGATION_BAR) {
            ShortsPlayerState.getOnChange().addObserver((ShortsPlayerState state) -> {
                setNavigationBarLayoutParams(state);
                return null;
            });
        }
        final int bottomMargin = validateValue(
                Settings.META_PANEL_BOTTOM_MARGIN,
                0,
                64,
                "revanced_shorts_meta_panel_bottom_margin_invalid_toast"
        );

        META_PANEL_BOTTOM_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) bottomMargin, Utils.getResources().getDisplayMetrics());

        final int heightPercentage = validateValue(
                Settings.SHORTS_NAVIGATION_BAR_HEIGHT_PERCENTAGE,
                0,
                100,
                "revanced_shorts_navigation_bar_height_percentage_invalid_toast"
        );

        NAVIGATION_BAR_HEIGHT_PERCENTAGE = heightPercentage / 100d;
    }

    public static Enum<?> repeat;
    public static Enum<?> singlePlay;
    public static Enum<?> endScreen;

    public static Enum<?> changeShortsRepeatState(Enum<?> currentState) {
        switch (Settings.CHANGE_SHORTS_REPEAT_STATE.get()) {
            case 1 -> currentState = repeat;
            case 2 -> currentState = singlePlay;
            case 3 -> currentState = endScreen;
        }

        return currentState;
    }

    public static boolean disableResumingStartupShortsPlayer() {
        return Settings.DISABLE_RESUMING_SHORTS_PLAYER.get();
    }

    public static boolean enableShortsTimeStamp(boolean original) {
        return ENABLE_TIME_STAMP || original;
    }

    public static int enableShortsTimeStamp(int original) {
        return ENABLE_TIME_STAMP ? 10010 : original;
    }

    public static void setShortsMetaPanelBottomMargin(View view) {
        if (!ENABLE_TIME_STAMP)
            return;

        if (!(view.getLayoutParams() instanceof RelativeLayout.LayoutParams lp))
            return;

        lp.setMargins(0, 0, 0, META_PANEL_BOTTOM_MARGIN);
        lp.setMarginEnd(ResourceUtils.getDimension("reel_player_right_dyn_bar_width"));
    }

    public static void setShortsTimeStampChangeRepeatState(View view) {
        if (!ENABLE_TIME_STAMP)
            return;
        if (!Settings.TIME_STAMP_CHANGE_REPEAT_STATE.get())
            return;
        if (view == null)
            return;

        view.setLongClickable(true);
        view.setOnLongClickListener(view1 -> {
            VideoUtils.showShortsRepeatDialog(view1.getContext());
            return true;
        });
    }

    public static void hideShortsCommentsButton(View view) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_COMMENTS_BUTTON.get(), view);
    }

    public static boolean hideShortsDislikeButton() {
        return Settings.HIDE_SHORTS_DISLIKE_BUTTON.get();
    }

    public static ViewGroup hideShortsInfoPanel(ViewGroup viewGroup) {
        return Settings.HIDE_SHORTS_INFO_PANEL.get() ? null : viewGroup;
    }

    public static boolean hideShortsLikeButton() {
        return Settings.HIDE_SHORTS_LIKE_BUTTON.get();
    }

    public static boolean hideShortsPaidPromotionLabel() {
        return Settings.HIDE_SHORTS_PAID_PROMOTION_LABEL.get();
    }

    public static void hideShortsPaidPromotionLabel(TextView textView) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_PAID_PROMOTION_LABEL.get(), textView);
    }

    public static void hideShortsRemixButton(View view) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_REMIX_BUTTON.get(), view);
    }

    public static void hideShortsShareButton(View view) {
        hideViewUnderCondition(Settings.HIDE_SHORTS_SHARE_BUTTON.get(), view);
    }

    public static boolean hideShortsSoundButton() {
        return Settings.HIDE_SHORTS_SOUND_BUTTON.get();
    }

    private static final int zeroPaddingDimenId =
            ResourceUtils.getDimenIdentifier("revanced_zero_padding");

    public static int getShortsSoundButtonDimenId(int dimenId) {
        return Settings.HIDE_SHORTS_SOUND_BUTTON.get()
                ? zeroPaddingDimenId
                : dimenId;
    }

    public static int hideShortsSubscribeButton(int original) {
        return Settings.HIDE_SHORTS_SUBSCRIBE_BUTTON.get() ? 0 : original;
    }

    // YouTube 18.29.38 ~ YouTube 19.28.42
    public static boolean hideShortsPausedHeader() {
        return Settings.HIDE_SHORTS_PAUSED_HEADER.get();
    }

    // YouTube 19.29.42 ~
    public static boolean hideShortsPausedHeader(boolean original) {
        return Settings.HIDE_SHORTS_PAUSED_HEADER.get() || original;
    }

    public static boolean hideShortsToolBar(boolean original) {
        return !Settings.HIDE_SHORTS_TOOLBAR.get() && original;
    }

    /**
     * BottomBarContainer is the parent view of {@link PivotBar},
     * And can be hidden using {@link View#setVisibility} only when it is initialized.
     * <p>
     * If it was not hidden with {@link View#setVisibility} when it was initialized,
     * it should be hidden with {@link FrameLayout.LayoutParams}.
     * <p>
     * When Shorts is opened, {@link FrameLayout.LayoutParams} should be changed to 0dp,
     * When Shorts is closed, {@link FrameLayout.LayoutParams} should be changed to the original.
     */
    private static WeakReference<View> bottomBarContainerRef = new WeakReference<>(null);

    private static FrameLayout.LayoutParams originalLayoutParams;
    private static final FrameLayout.LayoutParams zeroLayoutParams =
            new FrameLayout.LayoutParams(0, 0);

    public static void setNavigationBar(View view) {
        if (!HIDE_SHORTS_NAVIGATION_BAR) {
            return;
        }
        bottomBarContainerRef = new WeakReference<>(view);
        if (!(view.getLayoutParams() instanceof FrameLayout.LayoutParams lp)) {
            return;
        }
        if (originalLayoutParams == null) {
            originalLayoutParams = lp;
        }
    }

    public static int setNavigationBarHeight(int original) {
        return HIDE_SHORTS_NAVIGATION_BAR
                ? (int) Math.round(original * NAVIGATION_BAR_HEIGHT_PERCENTAGE)
                : original;
    }

    private static void setNavigationBarLayoutParams(@NonNull ShortsPlayerState shortsPlayerState) {
        final View navigationBar = bottomBarContainerRef.get();
        if (navigationBar == null) {
            return;
        }
        if (!(navigationBar.getLayoutParams() instanceof FrameLayout.LayoutParams lp)) {
            return;
        }
        navigationBar.setLayoutParams(
                shortsPlayerState.isClosed()
                        ? originalLayoutParams
                        : zeroLayoutParams
        );
    }

    /**
     * Injection point for the "More" button in the Shorts player
     */
    public static void showShortsToolbarMenu(String enumString, View toolbarView) {
        if (!Settings.HOOK_MORE_BUTTON.get())
            return;
        if (!isMoreButton(enumString))
            return;
        ImageView imageView = getChildView((ViewGroup) toolbarView, view -> view instanceof ImageView);
        if (imageView == null)
            return;

        imageView.setOnLongClickListener(button -> {
            showMoreButtonDialog(toolbarView.getContext());
            return true;
        });
    }

    private static void showMoreButtonDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString("revanced_hook_more_button_dialog_title"));

        String copyLink = getString("revanced_hook_more_button_option_copy_link_title");
        String copyLinkTimestamp = getString("revanced_hook_more_button_option_copy_link_timestamp_title");
        String downloadShort = getString("revanced_hook_more_button_option_download_short_title");
        String openInNormalPlayer = getString("revanced_hook_more_button_option_open_in_normal_player_title");

        List<String> optionsList = new ArrayList<>();
        Map<String, Runnable> actions = new HashMap<>();

        if (Settings.COPY_URL_SHORT_TOOLBAR_MENU.get()) {
            optionsList.add(copyLink);
            actions.put(copyLink, () -> VideoUtils.copyUrl(false));
        }

        if (Settings.COPY_URL_WITH_TIMESTAMP_SHORT_TOOLBAR_MENU.get()) {
            optionsList.add(copyLinkTimestamp);
            actions.put(copyLinkTimestamp, () -> VideoUtils.copyUrl(true));
        }

        if (Settings.DOWNLOAD_SHORT_TOOLBAR_MENU.get()) {
            optionsList.add(downloadShort);
            actions.put(downloadShort, VideoUtils::launchVideoExternalDownloader);
        }

        if (Settings.OPEN_IN_NORMAL_PLAYER_SHORT_TOOLBAR_MENU.get()) {
            optionsList.add(openInNormalPlayer);
            actions.put(openInNormalPlayer, VideoUtils::openVideo);
        }

        String[] options = optionsList.toArray(new String[0]);
        builder.setItems(options, (dialog, which) -> {
            String selectedOption = options[which];
            Runnable action = actions.get(selectedOption);
            if (action != null)
                action.run();
            else
                Logger.printDebug(() -> "No action found for " + selectedOption);
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private static boolean isMoreButton(String enumString) {
        return StringUtils.equalsAny(
                enumString,
                "MORE_VERT",
                "MORE_VERT_BOLD"
        );
    }

}
