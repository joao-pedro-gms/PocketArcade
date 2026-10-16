package br.com.joaop.pocketarcade;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import br.com.joaop.pocketarcade.databinding.DialogGameResultBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** A result that survives recreation without replaying a completed game. */
public class GameResultDialog extends DialogFragment {
    static final String TAG = "game_result";
    static final String REPLAY_REQUEST = "game_result_replay";
    private DialogGameResultBinding binding;
    private AnimatorSet resultAnimator;

    static void show(FragmentManager manager, boolean won, String message) {
        if (manager.isStateSaved() || manager.findFragmentByTag(TAG) != null) return;
        Bundle arguments = new Bundle();
        arguments.putBoolean("won", won);
        arguments.putString("message", message);
        GameResultDialog dialog = new GameResultDialog();
        dialog.setArguments(arguments);
        dialog.show(manager, TAG);
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        boolean won = requireArguments().getBoolean("won");
        binding = DialogGameResultBinding.inflate(requireActivity().getLayoutInflater());
        binding.resultIcon.setImageResource(won ? R.drawable.ic_trophy : R.drawable.ic_defeat);
        binding.resultIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(
                requireContext(), won ? R.color.accent : R.color.coral)));
        binding.resultMessage.setText(requireArguments().getString("message"));
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(won ? R.string.result_victory_title : R.string.result_defeat_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.result_play_again, (dialog, which) ->
                        getParentFragmentManager().setFragmentResult(REPLAY_REQUEST, new Bundle()))
                .setNegativeButton(R.string.result_close, null)
                .create();
    }

    @Override public void onStart() {
        super.onStart();
        // Dialog buttons use the app's primary button style; keep their text readable.
        AlertDialog dialog = (AlertDialog) requireDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.accent));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        UiMotion.reveal(binding.getRoot(), 400);
        if (!UiMotion.enabled()) return;
        View icon = binding.resultIcon;
        resultAnimator = new AnimatorSet();
        if (requireArguments().getBoolean("won")) {
            resultAnimator.playTogether(
                    ObjectAnimator.ofFloat(icon, View.SCALE_X, 0.5f, 1.15f, 1f),
                    ObjectAnimator.ofFloat(icon, View.SCALE_Y, 0.5f, 1.15f, 1f),
                    ObjectAnimator.ofFloat(icon, View.ROTATION, -15f, 12f, -6f, 0f));
        } else {
            float distance = 8 * getResources().getDisplayMetrics().density;
            resultAnimator.playTogether(ObjectAnimator.ofFloat(icon, View.TRANSLATION_X,
                    0f, -distance, distance, -distance / 2, distance / 2, 0f));
        }
        resultAnimator.setDuration(600);
        resultAnimator.setInterpolator(new FastOutSlowInInterpolator());
        resultAnimator.start();
    }

    @Override public void onDestroyView() {
        if (resultAnimator != null) { resultAnimator.cancel(); resultAnimator = null; }
        if (binding != null) { binding.getRoot().animate().cancel(); binding = null; }
        super.onDestroyView();
    }
}
