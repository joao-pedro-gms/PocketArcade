package br.com.joaop.pocketarcade;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import br.com.joaop.pocketarcade.databinding.ActivityScannerBinding;
import java.text.Normalizer;
import java.util.Locale;

public class ScannerActivity extends AppCompatActivity {
    private ActivityScannerBinding binding;
    private boolean hasResult;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this, binding.getRoot());
        NavigationHelper.bindBack(this, binding.navigation.backButton);
        binding.navigation.endSessionButton.setOnClickListener(v -> NavigationHelper.endSession(this, AppContract.MODULE_SCANNER));
        binding.analyzeButton.setOnClickListener(v -> analyze(true));
        binding.clearButton.setOnClickListener(v -> clear());
        if (state != null && state.getBoolean("scanner_has_result")) {
            binding.messageInput.setText(state.getString("scanner_message", ""));
            analyze(false);
        }
        UiMotion.reveal(binding.screenContent);
        Log.d("PocketArcade", "Scanner onCreate");
    }

    private void analyze(boolean animate) {
        String value = binding.messageInput.getText() == null ? "" : binding.messageInput.getText().toString();
        String trimmed = value.trim();
        if (trimmed.codePointCount(0, trimmed.length()) < 3) {
            binding.validationMessage.setText(R.string.scan_invalid);
            binding.validationMessage.setVisibility(View.VISIBLE);
            binding.messageInput.setError(getString(R.string.scan_invalid));
            Toast.makeText(this, R.string.scan_invalid, Toast.LENGTH_SHORT).show();
            binding.resultPanel.setVisibility(View.GONE);
            hasResult = false;
            return;
        }
        binding.messageInput.setError(null);
        binding.validationMessage.setVisibility(View.GONE);
        String compact = value.replaceAll("\\s+", "");
        int words = trimmed.split("\\s+").length;
        int vowels = 0;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        for (int i = 0; i < normalized.length(); i++) if ("aeiou".indexOf(normalized.charAt(i)) >= 0) vowels++;
        String palindrome = compact.toLowerCase(Locale.ROOT);
        boolean isPalindrome = palindrome.contentEquals(new StringBuilder(palindrome).reverse());
        binding.charactersResult.setText(getString(R.string.ui_scanner_count, compact.codePointCount(0, compact.length())));
        binding.wordsResult.setText(getString(R.string.ui_scanner_count, words));
        binding.vowelsResult.setText(getString(R.string.ui_scanner_count, vowels));
        binding.reversedResult.setText(new StringBuilder(value).reverse().toString());
        binding.palindromeResult.setText(isPalindrome ? R.string.yes : R.string.no);
        binding.resultPanel.setVisibility(View.VISIBLE);
        hasResult = true;
        if (animate) { UiMotion.hideKeyboard(this); UiMotion.reveal(binding.resultPanel); }
    }

    private void clear() {
        binding.messageInput.setText("");
        binding.messageInput.setError(null);
        binding.validationMessage.setVisibility(View.GONE);
        binding.resultPanel.setVisibility(View.GONE);
        hasResult = false;
        UiMotion.hideKeyboard(this);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("scanner_has_result", hasResult);
        state.putString("scanner_message", binding.messageInput.getText() == null ? "" : binding.messageInput.getText().toString());
        super.onSaveInstanceState(state);
    }
    @Override protected void onResume() { super.onResume(); Log.d("PocketArcade", "Scanner onResume"); }
    @Override protected void onStop() { super.onStop(); Log.d("PocketArcade", "Scanner onStop"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d("PocketArcade", "Scanner onDestroy"); }
}
