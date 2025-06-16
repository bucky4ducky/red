package com.example.red;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat; // Added for tinting local drawables

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DashboardItem> list;
    private ItemClickListener mClickListener;

    public DashboardAdapter(Context context, ArrayList<DashboardItem> list) {
        this.context = context;
        this.list = list;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        DashboardItem item = list.get(position);
        holder.title.setText(item.getTitle());

        // Logic to handle either local drawable or URL icon
        if (item.getIconResId() != 0) {
            // If iconResId is set, use local drawable and enable ripple
            holder.icon.setImageResource(item.getIconResId());
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.text_dark)); // Apply tint for visibility in dark/light mode
            holder.itemRippleView.setVisibility(View.VISIBLE); // Show ripple for these items
        } else if (item.getIconUrl() != null && !item.getIconUrl().isEmpty()) {
            // If iconUrl is set, load with Picasso and hide ripple
            Picasso.get()
                    .load(item.getIconUrl())
                    .placeholder(R.drawable.ic_baseline_image_24) // Provide a placeholder drawable (you might need to create this)
                    .error(R.drawable.ic_baseline_error_24)     // Provide an error drawable (you might need to create this)
                    .into(holder.icon);
            holder.icon.setColorFilter(null); // Clear any tint from previous items
            holder.itemRippleView.setVisibility(View.GONE); // Hide ripple for URL items
        } else {
            // No icon specified, hide icon or set a default
            holder.icon.setImageDrawable(null);
            holder.itemRippleView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(v, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        WaterRippleView itemRippleView; // Reference to the custom ripple view

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.dashboard_title);
            icon = itemView.findViewById(R.id.dashboard_icon);
            itemRippleView = itemView.findViewById(R.id.item_ripple_view);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, DashboardItem item);
    }
}