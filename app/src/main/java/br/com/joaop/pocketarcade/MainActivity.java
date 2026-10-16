package br.com.joaop.pocketarcade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import br.com.joaop.pocketarcade.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private boolean navigating;
    private String lastModule;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this, binding.getRoot());
        binding.studentInfo.setText(getString(R.string.ui_student_info,
                getString(R.string.student_name), getString(R.string.student_id), getString(R.string.course_name)));
        binding.enterButton.setOnClickListener(v -> enterArcade());
        binding.nicknameInput.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) { enterArcade(); return true; }
            return false;
        });
        showLastModule(state == null ? getIntent().getStringExtra(AppContract.EXTRA_MODULE) : state.getString("last_module"));
        UiMotion.reveal(binding.enterButton);
        log("onCreate");
    }

    private void enterArcade() {
        if (navigating) return;
        String nickname = binding.nicknameInput.getText() == null ? "" : binding.nicknameInput.getText().toString().trim();
        if (!AppContract.isValidNickname(nickname)) {
            binding.nicknameInput.setError(getString(R.string.nickname_error));
            Toast.makeText(this, R.string.nickname_error, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.nicknameInput.setError(null);
        navigating = true;
        UiMotion.hideKeyboard(this);
        Intent intent = new Intent(this, HubActivity.class);
        intent.putExtra(AppContract.EXTRA_NICKNAME, nickname);
        NavigationHelper.start(this, intent);
    }

    private void showLastModule(String module) {
        if (!AppContract.isKnownModule(module)) return;
        lastModule = module;
        binding.lastModule.setText(getString(R.string.last_module, module));
        binding.lastModule.setVisibility(View.VISIBLE);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showLastModule(intent.getStringExtra(AppContract.EXTRA_MODULE));
        log("onNewIntent");
    }
    @Override protected void onSaveInstanceState(Bundle state) { state.putString("last_module", lastModule); super.onSaveInstanceState(state); }
    private void log(String event) { Log.d("PocketArcade", "Apresentação " + event); }
    @Override protected void onResume() { super.onResume(); navigating = false; log("onResume"); }
    @Override protected void onStop() { super.onStop(); log("onStop"); }
    @Override protected void onDestroy() { super.onDestroy(); log("onDestroy"); }
}
