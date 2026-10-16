package br.com.joaop.pocketarcade;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

final class UiMotion {
    private UiMotion() { }

    static boolean enabled() {
        return Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled();
    }

    static void reveal(View view) {
        reveal(view, 240);
    }

    static void reveal(View view, long durationMillis) {
        view.animate().cancel();
        view.animate().setListener(null);
        view.setAlpha(1f);
        view.setTranslationY(0f);
        if (!enabled()) return;
        view.setAlpha(0f);
        view.setTranslationY(8f * view.getResources().getDisplayMetrics().density);
        view.animate().alpha(1f).translationY(0f).setDuration(durationMillis)
                .setInterpolator(new FastOutSlowInInterpolator()).start();
    }

    static void hideKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus == null) return;
        InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        focus.clearFocus();
    }
}
