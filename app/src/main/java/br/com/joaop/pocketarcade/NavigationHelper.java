package br.com.joaop.pocketarcade;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

final class NavigationHelper {
    private NavigationHelper() { }

    static void start(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.screen_enter, R.anim.screen_exit);
    }

    static void bindBack(AppCompatActivity activity, View button) {
        button.setOnClickListener(v -> back(activity));
        activity.getOnBackPressedDispatcher().addCallback(activity, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { back(activity); }
        });
    }

    private static void back(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.screen_pop_enter, R.anim.screen_pop_exit);
    }

    static void endSession(Activity activity, String module) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(AppContract.EXTRA_MODULE, module);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(R.anim.screen_pop_enter, R.anim.screen_pop_exit);
    }
}
