package com.example.smartkarobar;

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
    }

    private final List<HisaabItem> items = new ArrayList<>();
    private final ActionListener actionListener;

    public HisaabAdapter(@NonNull ActionListener actionListener) {
        this.actionListener = actionListener;
    }

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

        // single type variable (use everywhere below)
        String type = item.getType() == null ? "" : item.getType().toUpperCase();

        // amount color by TYPE (not by +/- sign)
        if ("SALE".equals(type)) {
            h.tvAmount.setTextColor(0xFF2D6A4F); // green
        } else if ("EXPENSE".equals(type) || "PAYABLE".equals(type)) {
            h.tvAmount.setTextColor(0xFFD64545); // red
        } else if ("RECEIVABLE".equals(type)) {
            h.tvAmount.setTextColor(0xFFF4A261); // orange
        } else {
            h.tvAmount.setTextColor(0xFF2F3740); // default
        }

        // default hidden
        h.btnClearUdhaar.setVisibility(View.GONE);
        h.btnClearPayable.setVisibility(View.GONE);

        if ("RECEIVABLE".equals(type)) {
            h.btnClearUdhaar.setVisibility(View.VISIBLE);
            h.btnClearUdhaar.setOnClickListener(v -> actionListener.onClearUdhaar(item));
        } else if ("PAYABLE".equals(type)) {
            h.btnClearPayable.setVisibility(View.VISIBLE);
            h.btnClearPayable.setOnClickListener(v -> actionListener.onClearPayable(item));
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View dot;
        TextView tvTitle, tvSubtitle, tvAmount, tvType;
        TextView btnClearUdhaar, btnClearPayable;

        VH(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.vDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);

            btnClearUdhaar = itemView.findViewById(R.id.btnClearUdhaar);
            btnClearPayable = itemView.findViewById(R.id.btnClearPayable);
        }
    }
}