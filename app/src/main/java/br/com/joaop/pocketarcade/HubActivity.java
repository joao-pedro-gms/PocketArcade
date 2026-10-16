package br.com.joaop.pocketarcade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import br.com.joaop.pocketarcade.databinding.ActivityHubBinding;

public class HubActivity extends AppCompatActivity {
    private ActivityHubBinding binding;
    private String nickname;
    private boolean navigating;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this, binding.getRoot());
        NavigationHelper.bindBack(this, binding.backButton);
        String received = getIntent().getStringExtra(AppContract.EXTRA_NICKNAME);
        if (!AppContract.isValidNickname(received)) {
            Toast.makeText(this, R.string.nickname_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nickname = received.trim();
        binding.welcome.setText(getString(R.string.welcome, nickname));
        if (UiMotion.enabled()) binding.menuContent.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_slide_in));
        binding.cardScanner.getRoot().setOnClickListener(v -> open(ScannerActivity.class, AppContract.MODULE_SCANNER));
        binding.cardArena.getRoot().setOnClickListener(v -> open(ArenaActivity.class, AppContract.MODULE_ARENA));
        binding.cardVault.getRoot().setOnClickListener(v -> open(CodeVaultActivity.class, AppContract.MODULE_VAULT));
        binding.cardRandomizer.getRoot().setOnClickListener(v -> open(RandomizerActivity.class, AppContract.MODULE_RANDOMIZER));
        log("onCreate");
    }

    private void open(Class<?> destination, String module) {
        if (navigating) return;
        navigating = true;
        Intent intent = new Intent(this, destination);
        intent.putExtra(AppContract.EXTRA_NICKNAME, nickname);
        intent.putExtra(AppContract.EXTRA_MODULE, module);
        NavigationHelper.start(this, intent);
    }
    private void log(String event) { Log.d("PocketArcade", "Central " + event); }
    @Override protected void onResume() { super.onResume(); navigating = false; log("onResume"); }
    @Override protected void onStop() { super.onStop(); log("onStop"); }
    @Override protected void onDestroy() { super.onDestroy(); log("onDestroy"); }
}
