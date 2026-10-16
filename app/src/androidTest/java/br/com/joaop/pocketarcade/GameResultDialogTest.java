package br.com.joaop.pocketarcade;

import android.animation.AnimatorSet;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Field;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GameResultDialogTest {
    @Test public void arenaVictoryCanReplay() { checkArena(true); }
    @Test public void arenaDefeatCanReplay() { checkArena(false); }
    @Test public void vaultVictoryCanReplay() { checkVault(true); }
    @Test public void vaultDefeatCanReplay() { checkVault(false); }

    private void checkArena(boolean won) {
        try (ActivityScenario<ArenaActivity> scenario = ActivityScenario.launch(ArenaActivity.class)) {
            scenario.onActivity(activity -> {
                // Exercise actual scoring with predictable device choices.
                ((Random) field(activity, "random")).setSeed(123);
                Random prediction = new Random(123);
                int[] buttons = {R.id.rockButton, R.id.paperButton, R.id.scissorsButton};
                for (int round = 0; round < 2; round++) {
                    int device = prediction.nextInt(3);
                    int player = (device + (won ? 1 : 2)) % 3;
                    activity.findViewById(buttons[player]).performClick();
                    AnimatorSet animator = (AnimatorSet) field(activity, "roundAnimator");
                    if (animator != null) animator.end();
                    if (round == 0) assertNull(activity.getSupportFragmentManager().findFragmentByTag(GameResultDialog.TAG));
                }
                GameResultDialog result = assertResult(activity, won);
                assertTrue(message(result).contains(won ? "2 × 0" : "0 × 2"));
                assertEquals(true, field(activity, "finished"));
            });
            // An open result survives Activity recreation, and replay resets the match.
            scenario.recreate();
            scenario.onActivity(activity -> {
                GameResultDialog result = assertResult(activity, won);
                ((AlertDialog) result.requireDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                assertEquals(false, field(activity, "finished"));
                assertEquals(0, field(activity, "playerPoints"));
                assertEquals(0, field(activity, "devicePoints"));
                assertTrue(activity.findViewById(R.id.rockButton).isEnabled());
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> assertNull(activity.getSupportFragmentManager().findFragmentByTag(GameResultDialog.TAG)));
        }
    }

    private void checkVault(boolean won) {
        try (ActivityScenario<CodeVaultActivity> scenario = ActivityScenario.launch(CodeVaultActivity.class)) {
            scenario.onActivity(activity -> {
                setField(activity, "secret", "1234");
                for (int attempt = 0; attempt < (won ? 1 : 8); attempt++) {
                    ((TextView) activity.findViewById(R.id.guessInput)).setText(won ? "1234" : "5678");
                    activity.findViewById(R.id.guessButton).performClick();
                    if (!won && attempt < 7) assertNull(activity.getSupportFragmentManager().findFragmentByTag(GameResultDialog.TAG));
                }
                GameResultDialog result = assertResult(activity, won);
                assertTrue(message(result).contains("1234"));
                assertTrue(message(result).contains(won ? "1 tentativa" : "8 tentativas"));
                assertFalse(activity.findViewById(R.id.guessButton).isEnabled());
                // Closing preserves the final result and does not start a new game.
                ((AlertDialog) result.requireDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertNull(activity.getSupportFragmentManager().findFragmentByTag(GameResultDialog.TAG));
                assertEquals(true, field(activity, "finished"));
                activity.findViewById(R.id.newGameButton).performClick();
                setField(activity, "secret", "1234");
                ((TextView) activity.findViewById(R.id.guessInput)).setText("1234");
                activity.findViewById(R.id.guessButton).performClick();
                GameResultDialog result = assertResult(activity, true);
                ((AlertDialog) result.requireDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                assertEquals(false, field(activity, "finished"));
                assertEquals(0, field(activity, "attempts"));
                assertTrue(activity.findViewById(R.id.guessButton).isEnabled());
                assertEquals(View.GONE, activity.findViewById(R.id.newGameButton).getVisibility());
            });
        }
    }

    private GameResultDialog assertResult(AppCompatActivity activity, boolean won) {
        activity.getSupportFragmentManager().executePendingTransactions();
        GameResultDialog fragment = (GameResultDialog) activity.getSupportFragmentManager().findFragmentByTag(GameResultDialog.TAG);
        assertNotNull(fragment);
        assertTrue(fragment.requireDialog().isShowing());
        TextView title = fragment.requireDialog().findViewById(androidx.appcompat.R.id.alertTitle);
        assertEquals(activity.getString(won ? R.string.result_victory_title : R.string.result_defeat_title), title.getText().toString());
        if (UiMotion.enabled()) {
            AnimatorSet animation = (AnimatorSet) field(fragment, "resultAnimator");
            assertNotNull(animation);
            assertEquals(600L, animation.getDuration());
        }
        return fragment;
    }

    private String message(GameResultDialog fragment) {
        return ((TextView) fragment.requireDialog().findViewById(R.id.resultMessage)).getText().toString();
    }

    private Object field(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
}
