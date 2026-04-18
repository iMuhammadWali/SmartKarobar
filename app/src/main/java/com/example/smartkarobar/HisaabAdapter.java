package com.example.smartkarobar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HisaabAdapter extends RecyclerView.Adapter<HisaabAdapter.VH> {

    public interface ActionListener {
        void onClearUdhaar(@NonNull HisaabItem item);
        void onClearPayable(@NonNull HisaabItem item);
        void onDelete(@NonNull HisaabItem item);
    }

    private final List<HisaabItem> items = new ArrayList<>();
    private final ActionListener listener;

    public HisaabAdapter(@NonNull ActionListener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<HisaabItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_hisaab_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HisaabItem item = items.get(position);

        String type = (item.getType() != null) ? item.getType().toUpperCase() : "";
        int color = getResolvedColor(type);

        h.tvTitle.setText(item.getTitle());
        h.tvSubtitle.setText(item.getSubtitle());
        h.tvAmount.setText(item.getAmount());
        h.tvType.setText(type);

        // Apply calculated color
        h.tvAmount.setTextColor(color);
        if (h.vDot != null && h.vDot.getBackground() != null) {
            h.vDot.getBackground().setTint(color);
        }

        // Button Visibility Logic
        h.btnClearUdhaar.setVisibility("RECEIVABLE".equals(type) ? View.VISIBLE : View.GONE);
        h.btnClearPayable.setVisibility("PAYABLE".equals(type) ? View.VISIBLE : View.GONE);

        h.btnClearUdhaar.setOnClickListener(v -> listener.onClearUdhaar(item));
        h.btnClearPayable.setOnClickListener(v -> listener.onClearPayable(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    // Helper method inside Adapter to match your Fragment's color logic
    private int getResolvedColor(String type) {
        switch (type) {
            case "SALE":
                return Color.parseColor("#2D6A4F");
            case "RECEIVABLE":
                return Color.parseColor("#F4A261");
            case "EXPENSE":
            case "PAYABLE":
                return Color.parseColor("#D64545");
            default:
                return Color.parseColor("#2F3740");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View vDot;
        TextView tvTitle, tvSubtitle, tvAmount, tvType;
        TextView btnClearUdhaar, btnClearPayable, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            vDot = itemView.findViewById(R.id.vDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            btnClearUdhaar = itemView.findViewById(R.id.btnClearUdhaar);
            btnClearPayable = itemView.findViewById(R.id.btnClearPayable);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}