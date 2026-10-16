package br.com.joaop.pocketarcade;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** One persistent form header, composed with the item adapter in the same scrolling list. */
final class SingleViewAdapter extends RecyclerView.Adapter<SingleViewAdapter.HeaderHolder> {
    private final View header;
    SingleViewAdapter(View header) { this.header = header; }
    @NonNull @Override public HeaderHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) { return new HeaderHolder(header); }
    @Override public void onBindViewHolder(@NonNull HeaderHolder holder, int position) { }
    @Override public int getItemCount() { return 1; }
    static final class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View view) { super(view); }
    }
}
