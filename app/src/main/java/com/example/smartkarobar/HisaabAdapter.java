package com.example.smartkarobar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartkarobar.R;

import java.util.ArrayList;
import java.util.List;

public class HisaabAdapter extends RecyclerView.Adapter<HisaabAdapter.VH> {

    private final List<HisaabItem> items = new ArrayList<>();

    public void submitList(List<HisaabItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_hisaab_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HisaabItem item = items.get(position);

        h.tvTitle.setText(item.getTitle());
        h.tvSubtitle.setText(item.getSubtitle());
        h.tvAmount.setText(item.getAmount());
        h.tvType.setText(item.getType());
        h.dot.setBackgroundColor(item.getDotColor());

        // amount color by sign
        if (item.getAmount().startsWith("-")) {
            h.tvAmount.setTextColor(0xFFD64545);
        } else {
            h.tvAmount.setTextColor(0xFF2D6A4F);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View dot;
        TextView tvTitle, tvSubtitle, tvAmount, tvType;

        VH(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.vDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}