package br.com.joaop.pocketarcade;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import br.com.joaop.pocketarcade.databinding.ActivityVaultBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CodeVaultActivity extends AppCompatActivity {
    private ActivityVaultBinding binding;
    private String secret;
    private int attempts;
    private long startTime;
    private boolean finished;
    private boolean gameWon;
    private long finishedElapsedSeconds;
    private final ArrayList<String> historyGuesses = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Log.d("PocketArcade", "Cofre onCreate");
        binding = ActivityVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this, binding.getRoot());
        NavigationHelper.bindBack(this, binding.navigation.backButton);
        binding.guessButton.setOnClickListener(v -> submitGuess());
        binding.newGameButton.setOnClickListener(v -> newGame());
        getSupportFragmentManager().setFragmentResultListener(GameResultDialog.REPLAY_REQUEST,
                this, (key, result) -> newGame());
        binding.navigation.endSessionButton.setOnClickListener(v -> NavigationHelper.endSession(this, AppContract.MODULE_VAULT));
        if (state == null || state.getString("vault_secret") == null) newGame();
        else restoreGame(state);
        UiMotion.reveal(binding.screenContent);
    }

    @Override protected void onResume() { super.onResume(); Log.d("PocketArcade", "Cofre onResume"); }
    @Override protected void onStop() { super.onStop(); Log.d("PocketArcade", "Cofre onStop"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d("PocketArcade", "Cofre onDestroy"); }

    private void newGame() {
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i <= 9; i++) digits.add(i);
        Collections.shuffle(digits, new Random());
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) code.append(digits.get(i));
        if (code.charAt(0) == '0') {
            char first = code.charAt(1); code.setCharAt(1, '0'); code.setCharAt(0, first);
        }
        secret = code.toString();
        attempts = 0; finished = false; gameWon = false; finishedElapsedSeconds = 0;
        startTime = System.currentTimeMillis();
        historyGuesses.clear();
        binding.guessInput.setText("");
        binding.guessInput.setError(null);
        binding.guessInput.setEnabled(true);
        binding.guessButton.setEnabled(true);
        binding.historyContainer.removeAllViews();
        binding.emptyHistory.setVisibility(View.VISIBLE);
        binding.vaultMessage.setText(R.string.vault_locked);
        binding.timeResult.setText("");
        binding.newGameButton.setVisibility(View.GONE);
    }

    private void restoreGame(Bundle state) {
        secret = state.getString("vault_secret");
        attempts = state.getInt("vault_attempts");
        startTime = state.getLong("vault_start_time");
        finished = state.getBoolean("vault_finished");
        gameWon = state.getBoolean("vault_won");
        finishedElapsedSeconds = state.getLong("vault_elapsed_seconds");
        ArrayList<String> restoredGuesses = state.getStringArrayList("vault_history");
        historyGuesses.clear();
        if (restoredGuesses != null) historyGuesses.addAll(restoredGuesses);
        binding.historyContainer.removeAllViews();
        for (int i = 0; i < historyGuesses.size(); i++) addHistoryRow(historyGuesses.get(i), i + 1);
        binding.emptyHistory.setVisibility(historyGuesses.isEmpty() ? View.VISIBLE : View.GONE);
        if (finished) {
            binding.guessInput.setEnabled(false);
            binding.guessButton.setEnabled(false);
            binding.newGameButton.setVisibility(View.VISIBLE);
            showGameResult();
        } else {
            binding.guessInput.setEnabled(true);
            binding.guessButton.setEnabled(true);
            binding.newGameButton.setVisibility(View.GONE);
            if (attempts == 0) binding.vaultMessage.setText(R.string.vault_locked);
            else binding.vaultMessage.setText(getString(R.string.attempt_progress, attempts));
            binding.timeResult.setText("");
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putString("vault_secret", secret);
        outState.putInt("vault_attempts", attempts);
        outState.putLong("vault_start_time", startTime);
        outState.putBoolean("vault_finished", finished);
        outState.putBoolean("vault_won", gameWon);
        outState.putLong("vault_elapsed_seconds", finishedElapsedSeconds);
        outState.putStringArrayList("vault_history", new ArrayList<>(historyGuesses));
        super.onSaveInstanceState(outState);
    }

    private void submitGuess() {
        if (finished) return;
        String guess = binding.guessInput.getText() == null ? "" : binding.guessInput.getText().toString().trim();
        if (!guess.matches("\\d{4}")) { binding.guessInput.setError(getString(R.string.guess_exact)); return; }
        boolean[] seenDigits = new boolean[10];
        for (int i = 0; i < guess.length(); i++) {
            int digit = guess.charAt(i) - '0';
            if (seenDigits[digit]) { binding.guessInput.setError(getString(R.string.guess_unique)); return; }
            seenDigits[digit] = true;
        }
        binding.guessInput.setError(null);
        attempts++;
        historyGuesses.add(guess);
        binding.emptyHistory.setVisibility(View.GONE);
        addHistoryRow(guess, attempts);
        UiMotion.hideKeyboard(this);
        UiMotion.reveal(binding.historyContainer.getChildAt(binding.historyContainer.getChildCount() - 1));
        if (guess.equals(secret)) finishGame(true);
        else if (attempts >= 8) finishGame(false);
        else binding.vaultMessage.setText(getString(R.string.attempt_progress, attempts));
        binding.guessInput.setText("");
    }

    private void addHistoryRow(String guess, int attemptNumber) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));
        TextView count = new TextView(this);
        count.setText(getString(R.string.attempt_number, attemptNumber));
        count.setTextColor(ContextCompat.getColor(this, R.color.ink));
        count.setTextSize(14);
        count.setLayoutParams(new LinearLayout.LayoutParams(dp(36), LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(count);
        for (int i = 0; i < 4; i++) {
            TextView tile = new TextView(this);
            tile.setText(String.valueOf(guess.charAt(i)));
            tile.setTextColor(ContextCompat.getColor(this, R.color.ink));
            tile.setTextSize(18);
            tile.setGravity(Gravity.CENTER);
            boolean correctPosition = guess.charAt(i) == secret.charAt(i);
            boolean digitExists = secret.indexOf(guess.charAt(i)) >= 0;
            int colorResource = correctPosition ? R.color.green : digitExists ? R.color.yellow : R.color.gray_tile;
            int clueDescription = correctPosition ? R.string.vault_tile_correct
                    : digitExists ? R.string.vault_tile_misplaced : R.string.vault_tile_absent;
            tile.setContentDescription(getString(clueDescription, guess.charAt(i)));
            tile.setBackgroundResource(R.drawable.bg_guess_tile);
            tile.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorResource)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1);
            params.setMargins(dp(4), 0, 0, 0);
            tile.setLayoutParams(params);
            row.addView(tile);
        }
        binding.historyContainer.addView(row);
    }

    private void finishGame(boolean won) {
        finished = true;
        gameWon = won;
        finishedElapsedSeconds = (System.currentTimeMillis() - startTime) / 1000L;
        binding.guessInput.setEnabled(false);
        binding.guessButton.setEnabled(false);
        showGameResult();
        binding.newGameButton.setVisibility(View.VISIBLE);
        GameResultDialog.show(getSupportFragmentManager(), gameWon,
                binding.vaultMessage.getText() + "\n" + binding.timeResult.getText());
    }

    private void showGameResult() {
        binding.vaultMessage.setText(getString(gameWon ? R.string.vault_win : R.string.vault_loss, secret));
        UiMotion.reveal(binding.vaultMessage);
        String attemptCount = getResources().getQuantityString(R.plurals.attempt_count, attempts, attempts);
        binding.timeResult.setText(getString(R.string.game_result_time,
                getString(gameWon ? R.string.victory : R.string.defeat), attemptCount, finishedElapsedSeconds));
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
