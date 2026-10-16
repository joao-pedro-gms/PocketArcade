package br.com.joaop.pocketarcade;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.joaop.pocketarcade.databinding.ItemRandomizerBinding;
import java.util.List;

public class RandomItemAdapter extends RecyclerView.Adapter<RandomItemAdapter.ItemViewHolder> {
    public interface OnItemRemoved { void onRemove(int position); }
    private final List<RandomItem> items;
    private final OnItemRemoved onItemRemoved;

    public RandomItemAdapter(List<RandomItem> items, OnItemRemoved onItemRemoved) {
        this.items = items;
        this.onItemRemoved = onItemRemoved;
    }
    @NonNull @Override public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new ItemViewHolder(ItemRandomizerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        RandomItem item = items.get(position);
        holder.binding.itemWord.setText(item.getWord());
        holder.binding.itemCategory.setText(item.getCategory());
        holder.binding.deleteItemButton.setContentDescription(holder.itemView.getContext().getString(R.string.ui_remove_item_named, item.getWord()));
        holder.binding.deleteItemButton.setOnClickListener(v -> remove(holder));
        holder.binding.getRoot().setOnLongClickListener(v -> { remove(holder); return true; });
    }
    private void remove(ItemViewHolder holder) {
        int position = holder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) onItemRemoved.onRemove(position);
    }
    @Override public int getItemCount() { return items.size(); }
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final ItemRandomizerBinding binding;
        ItemViewHolder(ItemRandomizerBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
