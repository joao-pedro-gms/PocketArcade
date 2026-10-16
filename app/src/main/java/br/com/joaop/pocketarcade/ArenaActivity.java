package br.com.joaop.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import br.com.joaop.pocketarcade.databinding.ActivityArenaBinding;
import com.google.android.material.card.MaterialCardView;
import java.util.Random;

public class ArenaActivity extends AppCompatActivity {
    private static final long ROUND_ANIMATION_DURATION_MS = 360;
    private ActivityArenaBinding binding;
    private final Random random = new Random();
    private final int[] icons = {R.drawable.ic_rock, R.drawable.ic_paper, R.drawable.ic_scissors};
    private MediaPlayer winPlayer;
    private MediaPlayer losePlayer;
    private AnimatorSet roundAnimator;
    private int playerPoints;
    private int devicePoints;
    private int playerChoice = -1;
    private int deviceChoice = -1;
    private boolean finished;
    private boolean roundAnimating;
    private String[] names;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityArenaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this, binding.getRoot());
        NavigationHelper.bindBack(this, binding.navigation.backButton);
        binding.navigation.endSessionButton.setOnClickListener(v -> NavigationHelper.endSession(this, AppContract.MODULE_ARENA));
        names = new String[]{getString(R.string.rock), getString(R.string.paper), getString(R.string.scissors)};
        winPlayer = MediaPlayer.create(this, R.raw.win);
        losePlayer = MediaPlayer.create(this, R.raw.round);
        for (MaterialCardView choice : choices()) choice.setCheckable(true);
        binding.rockButton.setOnClickListener(v -> playRound(0));
        binding.paperButton.setOnClickListener(v -> playRound(1));
        binding.scissorsButton.setOnClickListener(v -> playRound(2));
        binding.newMatchButton.setOnClickListener(v -> resetMatch());
        getSupportFragmentManager().setFragmentResultListener(GameResultDialog.REPLAY_REQUEST,
                this, (key, result) -> resetMatch());
        if (state != null) {
            playerPoints = state.getInt("arena_player_points");
            devicePoints = state.getInt("arena_device_points");
            playerChoice = state.getInt("arena_player_choice", -1);
            deviceChoice = state.getInt("arena_device_choice", -1);
            finished = state.getBoolean("arena_finished");
            binding.roundMessage.setText(state.getString("arena_message", getString(R.string.round_choose)));
        }
        renderScore();
        renderChoices();
        setChoiceButtonsEnabled(!finished);
        binding.newMatchButton.setVisibility(finished ? View.VISIBLE : View.GONE);
        UiMotion.reveal(binding.screenContent);
        Log.d("PocketArcade", "Arena onCreate");
    }

    private MaterialCardView[] choices() {
        return new MaterialCardView[]{binding.rockButton, binding.paperButton, binding.scissorsButton};
    }

    private void playRound(int player) {
        if (finished || roundAnimating) return;
        playerChoice = player;
        deviceChoice = random.nextInt(3);
        boolean won = player != deviceChoice && (player - deviceChoice + 3) % 3 == 1;
        if (player == deviceChoice) binding.roundMessage.setText(R.string.draw_round);
        else if (won) { playerPoints++; binding.roundMessage.setText(R.string.player_round_win); }
        else { devicePoints++; binding.roundMessage.setText(R.string.device_round_win); }
        finished = playerPoints == 2 || devicePoints == 2;
        if (finished) binding.roundMessage.setText(playerPoints == 2 ? R.string.player_match_win : R.string.device_match_win);
        renderScore();
        renderChoices();
        UiMotion.reveal(binding.arenaBoard, ROUND_ANIMATION_DURATION_MS);
        UiMotion.reveal(binding.roundMessage, ROUND_ANIMATION_DURATION_MS);
        binding.newMatchButton.setVisibility(finished ? View.VISIBLE : View.GONE);
        if (player != deviceChoice) play(won ? winPlayer : losePlayer);
        if (finished) GameResultDialog.show(getSupportFragmentManager(), playerPoints == 2,
                getString(R.string.arena_result_score, binding.roundMessage.getText(), playerPoints, devicePoints));
        if (!UiMotion.enabled()) { setChoiceButtonsEnabled(!finished); return; }
        roundAnimating = true;
        setChoiceButtonsEnabled(false);
        View selected = choices()[player];
        roundAnimator = new AnimatorSet();
        roundAnimator.playTogether(ObjectAnimator.ofFloat(selected, View.SCALE_X, 1f, 0.96f, 1f),
                ObjectAnimator.ofFloat(selected, View.SCALE_Y, 1f, 0.96f, 1f),
                ObjectAnimator.ofFloat(binding.deviceChoicePanel, View.SCALE_X, 0.85f, 1f),
                ObjectAnimator.ofFloat(binding.deviceChoicePanel, View.SCALE_Y, 0.85f, 1f));
        roundAnimator.setDuration(ROUND_ANIMATION_DURATION_MS);
        roundAnimator.setInterpolator(new FastOutSlowInInterpolator());
        roundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                selected.setScaleX(1f);
                selected.setScaleY(1f);
                binding.deviceChoicePanel.setScaleX(1f);
                binding.deviceChoicePanel.setScaleY(1f);
                roundAnimating = false;
                setChoiceButtonsEnabled(!finished);
            }
        });
        roundAnimator.start();
    }

    private void renderChoices() {
        MaterialCardView[] choices = choices();
        for (int i = 0; i < choices.length; i++) choices[i].setChecked(i == playerChoice);
        if (playerChoice < 0 || deviceChoice < 0) {
            binding.playerChoiceIcon.setImageResource(R.drawable.ic_arcade);
            binding.deviceChoiceIcon.setImageResource(R.drawable.ic_arcade);
            binding.playerChoiceName.setText(R.string.ui_waiting_choice);
            binding.deviceChoiceName.setText(R.string.ui_waiting_choice);
        } else {
            binding.playerChoiceIcon.setImageResource(icons[playerChoice]);
            binding.deviceChoiceIcon.setImageResource(icons[deviceChoice]);
            binding.playerChoiceName.setText(names[playerChoice]);
            binding.deviceChoiceName.setText(names[deviceChoice]);
        }
    }

    private void resetMatch() {
        if (roundAnimator != null) roundAnimator.cancel();
        playerPoints = 0;
        devicePoints = 0;
        playerChoice = -1;
        deviceChoice = -1;
        finished = false;
        roundAnimating = false;
        renderScore();
        renderChoices();
        binding.roundMessage.setText(R.string.round_choose);
        binding.newMatchButton.setVisibility(View.GONE);
        setChoiceButtonsEnabled(true);
        UiMotion.reveal(binding.arenaBoard, ROUND_ANIMATION_DURATION_MS);
    }

    private void setChoiceButtonsEnabled(boolean enabled) {
        for (MaterialCardView choice : choices()) choice.setEnabled(enabled);
    }

    private void renderScore() {
        binding.playerScore.setText(getString(R.string.ui_scanner_count, playerPoints));
        binding.deviceScore.setText(getString(R.string.ui_scanner_count, devicePoints));
    }

    private void play(MediaPlayer player) {
        if (player == null) return;
        if (player.isPlaying()) player.pause();
        player.seekTo(0);
        player.start();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("arena_player_points", playerPoints);
        state.putInt("arena_device_points", devicePoints);
        state.putInt("arena_player_choice", playerChoice);
        state.putInt("arena_device_choice", deviceChoice);
        state.putBoolean("arena_finished", finished);
        state.putString("arena_message", binding.roundMessage.getText().toString());
        super.onSaveInstanceState(state);
    }
    @Override protected void onPause() {
        if (roundAnimator != null) roundAnimator.cancel();
        if (winPlayer != null && winPlayer.isPlaying()) winPlayer.pause();
        if (losePlayer != null && losePlayer.isPlaying()) losePlayer.pause();
        super.onPause();
    }
    @Override protected void onDestroy() {
        if (winPlayer != null) { winPlayer.release(); winPlayer = null; }
        if (losePlayer != null) { losePlayer.release(); losePlayer = null; }
        super.onDestroy();
        Log.d("PocketArcade", "Arena onDestroy");
    }
    @Override protected void onResume() { super.onResume(); Log.d("PocketArcade", "Arena onResume"); }
    @Override protected void onStop() { super.onStop(); Log.d("PocketArcade", "Arena onStop"); }
}
