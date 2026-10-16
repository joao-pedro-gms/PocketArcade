package br.com.joaop.pocketarcade;

import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.joaop.pocketarcade.databinding.ActivityRandomizerBinding;
import br.com.joaop.pocketarcade.databinding.HeaderRandomizerBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomizerActivity extends AppCompatActivity {
    private ActivityRandomizerBinding binding;
    private HeaderRandomizerBinding header;
    private final List<RandomItem> items = new ArrayList<>();
    private final Random random = new Random();
    private RandomItemAdapter adapter;
    private MediaPlayer drawPlayer;
    private String[] categories;
    private boolean gridMode;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityRandomizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBarInsets.apply(this, binding.getRoot());
        NavigationHelper.bindBack(this, binding.navigation.backButton);
        binding.navigation.endSessionButton.setOnClickListener(v -> NavigationHelper.endSession(this, AppContract.MODULE_RANDOMIZER));
        binding.itemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        header = HeaderRandomizerBinding.inflate(getLayoutInflater(), binding.itemsRecycler, false);
        categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        header.categorySpinner.setAdapter(spinnerAdapter);
        if (state == null || !state.containsKey("randomizer_words")) seedItems();
        else {
            restoreItems(state);
            gridMode = state.getBoolean("randomizer_grid_mode");
            int selected = state.getInt("randomizer_category", 0);
            if (selected >= 0 && selected < categories.length) header.categorySpinner.setSelection(selected);
            header.itemInput.setText(state.getString("randomizer_input", ""));
            String result = state.getString("randomizer_result", "");
            if (!result.isEmpty()) {
                header.drawResult.setText(result);
                header.drawResultPanel.setVisibility(View.VISIBLE);
            }
        }
        adapter = new RandomItemAdapter(items, position -> {
            items.remove(position);
            adapter.notifyItemRemoved(position);
            renderCount();
        });
        binding.itemsRecycler.setAdapter(new ConcatAdapter(new SingleViewAdapter(header.getRoot()), adapter));
        binding.itemsRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override public void getItemOffsets(@NonNull Rect out, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State recyclerState) {
                int position = parent.getChildAdapterPosition(view);
                if (gridMode && position > 0) {
                    int gap = Math.round(6 * getResources().getDisplayMetrics().density);
                    if ((position - 1) % 2 == 0) out.right = gap;
                    else out.left = gap;
                }
            }
        });
        header.listModeButton.setCheckable(true);
        header.gridModeButton.setCheckable(true);
        applyLayoutMode();
        renderCount();
        drawPlayer = MediaPlayer.create(this, R.raw.round);
        header.addButton.setOnClickListener(v -> addItem());
        header.drawButton.setOnClickListener(v -> drawItem());
        header.listModeButton.setOnClickListener(v -> {
            if (gridMode) { gridMode = false; applyLayoutMode(); }
            else header.listModeButton.setChecked(true);
        });
        header.gridModeButton.setOnClickListener(v -> {
            if (!gridMode) { gridMode = true; applyLayoutMode(); }
            else header.gridModeButton.setChecked(true);
        });
        header.itemInput.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) { addItem(); return true; }
            return false;
        });
        UiMotion.reveal(binding.itemsRecycler);
        Log.d("PocketArcade", "Randomizador onCreate");
    }

    private void applyLayoutMode() {
        LinearLayoutManager previous = (LinearLayoutManager) binding.itemsRecycler.getLayoutManager();
        int firstPosition = previous == null ? RecyclerView.NO_POSITION : previous.findFirstVisibleItemPosition();
        View firstView = previous == null ? null : previous.findViewByPosition(firstPosition);
        int offset = firstView == null ? 0 : firstView.getTop() - binding.itemsRecycler.getPaddingTop();
        if (gridMode) {
            GridLayoutManager grid = new GridLayoutManager(this, 2);
            grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override public int getSpanSize(int position) { return position == 0 ? 2 : 1; }
            });
            binding.itemsRecycler.setLayoutManager(grid);
        } else binding.itemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        if (firstPosition != RecyclerView.NO_POSITION) {
            ((LinearLayoutManager) binding.itemsRecycler.getLayoutManager()).scrollToPositionWithOffset(firstPosition, offset);
        }
        binding.itemsRecycler.invalidateItemDecorations();
        header.listModeButton.setChecked(!gridMode);
        header.gridModeButton.setChecked(gridMode);
    }

    private void renderCount() {
        header.itemCount.setText(getResources().getQuantityString(R.plurals.item_count, items.size(), items.size()));
        header.emptyCollection.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void seedItems() {
        items.add(new RandomItem(getString(R.string.sample_movie_spirited), categories[0]));
        items.add(new RandomItem(getString(R.string.sample_movie_interstellar), categories[0]));
        items.add(new RandomItem(getString(R.string.sample_food_tacos), categories[1]));
        items.add(new RandomItem(getString(R.string.sample_food_cheese_bread), categories[1]));
        items.add(new RandomItem(getString(R.string.sample_place_lisbon), categories[2]));
        items.add(new RandomItem(getString(R.string.sample_place_park), categories[2]));
        items.add(new RandomItem(getString(R.string.sample_challenge_draw), categories[3]));
        items.add(new RandomItem(getString(R.string.sample_challenge_dance), categories[3]));
    }

    private void addItem() {
        String word = header.itemInput.getText() == null ? "" : header.itemInput.getText().toString().trim();
        if (word.isEmpty()) { header.itemInput.setError(getString(R.string.add_value_error)); return; }
        header.itemInput.setError(null);
        items.add(new RandomItem(word, categories[header.categorySpinner.getSelectedItemPosition()]));
        adapter.notifyItemInserted(items.size() - 1);
        header.itemInput.setText("");
        renderCount();
        UiMotion.hideKeyboard(this);
        binding.itemsRecycler.post(() -> {
            // ConcatAdapter includes the header before the collection items.
            int lastPosition = binding.itemsRecycler.getAdapter().getItemCount() - 1;
            if (UiMotion.enabled()) binding.itemsRecycler.smoothScrollToPosition(lastPosition);
            else binding.itemsRecycler.scrollToPosition(lastPosition);
        });
        Toast.makeText(this, R.string.ui_item_added, Toast.LENGTH_SHORT).show();
    }

    private void drawItem() {
        UiMotion.hideKeyboard(this);
        String category = categories[header.categorySpinner.getSelectedItemPosition()];
        List<RandomItem> matches = new ArrayList<>();
        for (RandomItem item : items) if (item.getCategory().equals(category)) matches.add(item);
        if (matches.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_category, category), Toast.LENGTH_SHORT).show();
            return;
        }
        RandomItem chosen = matches.get(random.nextInt(matches.size()));
        header.drawResult.setText(getString(R.string.draw_result, chosen.getWord(), chosen.getCategory()));
        header.drawResultPanel.setVisibility(View.VISIBLE);
        UiMotion.reveal(header.drawResultPanel);
        if (drawPlayer != null) {
            if (drawPlayer.isPlaying()) drawPlayer.pause();
            drawPlayer.seekTo(0);
            drawPlayer.start();
        }
    }

    private void restoreItems(Bundle state) {
        ArrayList<String> words = state.getStringArrayList("randomizer_words");
        ArrayList<String> itemCategories = state.getStringArrayList("randomizer_categories");
        if (words == null || itemCategories == null || words.size() != itemCategories.size()) { seedItems(); return; }
        for (int i = 0; i < words.size(); i++) if (words.get(i) != null && itemCategories.get(i) != null) {
            items.add(new RandomItem(words.get(i), itemCategories.get(i)));
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> itemCategories = new ArrayList<>();
        for (RandomItem item : items) { words.add(item.getWord()); itemCategories.add(item.getCategory()); }
        state.putStringArrayList("randomizer_words", words);
        state.putStringArrayList("randomizer_categories", itemCategories);
        state.putBoolean("randomizer_grid_mode", gridMode);
        state.putInt("randomizer_category", header.categorySpinner.getSelectedItemPosition());
        state.putString("randomizer_input", header.itemInput.getText() == null ? "" : header.itemInput.getText().toString());
        state.putString("randomizer_result", header.drawResult.getText().toString());
        super.onSaveInstanceState(state);
    }
    @Override protected void onPause() { super.onPause(); if (drawPlayer != null && drawPlayer.isPlaying()) drawPlayer.pause(); }
    @Override protected void onDestroy() {
        if (drawPlayer != null) { drawPlayer.release(); drawPlayer = null; }
        super.onDestroy();
        Log.d("PocketArcade", "Randomizador onDestroy");
    }
    @Override protected void onResume() { super.onResume(); Log.d("PocketArcade", "Randomizador onResume"); }
    @Override protected void onStop() { super.onStop(); Log.d("PocketArcade", "Randomizador onStop"); }
}
