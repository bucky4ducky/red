package com.example.red;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
    private final List<DashboardItem> items;
    private final LayoutInflater inflater;
    private final Context context;
    private ItemClickListener listener;

    public DashboardAdapter(Context context, List<DashboardItem> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    public interface ItemClickListener {
        void onItemClick(View view, DashboardItem item);
    }

    public void setClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.dashboard_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        DashboardItem item = items.get(position);
        holder.title.setText(item.getTitle());

        Glide.with(context)
                .load(item.getIconUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(holder.icon);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(v, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dashboard_title);
            icon = itemView.findViewById(R.id.dashboard_icon);
        }
    }
}
