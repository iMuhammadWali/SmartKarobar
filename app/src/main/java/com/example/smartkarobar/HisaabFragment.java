package com.example.smartkarobar;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HisaabFragment extends Fragment {
    TextView tvTransactionCount, tvAll, tvSales, tvReceiveables, tvExpenses;
    private ArrayList<HisaabItem> allTransactions = new ArrayList<>();
    HisaabAdapter adapter;

    public HisaabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hisaab, container, false);
    }

    private void init(View v){
        tvTransactionCount = v.findViewById(R.id.tvTransactionsCount);
        tvExpenses = v.findViewById(R.id.tvExpenses);
        tvReceiveables = v.findViewById(R.id.tvReceivables);
        tvSales = v.findViewById(R.id.tvSales);
        tvAll = v.findViewById(R.id.tvAll);
    }

    private void updateChipUI(String selected) {
        tvAll.setBackgroundResource(selected.equals("ALL") ? R.drawable.bg_nav_selected : R.drawable.bg_rounded_very_light_green);
        tvSales.setBackgroundResource(selected.equals("SALE") ? R.drawable.bg_nav_selected : R.drawable.bg_rounded_very_light_green);
        tvReceiveables.setBackgroundResource(selected.equals("RECEIVABLE") ? R.drawable.bg_nav_selected : R.drawable.bg_rounded_very_light_green);
        tvExpenses.setBackgroundResource(selected.equals("EXPENSE") ? R.drawable.bg_nav_selected : R.drawable.bg_rounded_very_light_green);

        int sel = Color.WHITE;
        int unsel = Color.parseColor("#2D6A4F");

        tvAll.setTextColor(selected.equals("ALL") ? sel : unsel);
        tvSales.setTextColor(selected.equals("SALE") ? sel : unsel);
        tvReceiveables.setTextColor(selected.equals("RECEIVABLE") ? sel : unsel);
        tvExpenses.setTextColor(selected.equals("EXPENSE") ? sel : unsel);
    }

    private void applyFilter(String type) {
        ArrayList<HisaabItem> filtered = new ArrayList<>();

        if (type.equals("ALL")) {
            filtered.addAll(allTransactions);
        } else {
            for (HisaabItem item : allTransactions) {
                if (item.getType().equalsIgnoreCase(type)) {
                    filtered.add(item);
                }
            }
        }

        adapter.submitList(filtered);
        tvTransactionCount.setText(filtered.size() + " TRANSACTIONS");
        updateChipUI(type);
    }

    private void applyListeners(){
        tvExpenses.setOnClickListener(v -> applyFilter("EXPENSE"));
        tvReceiveables.setOnClickListener(v -> applyFilter("RECEIVABLE"));
        tvAll.setOnClickListener(v -> applyFilter("ALL"));
        tvSales.setOnClickListener(v -> applyFilter("SALE"));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        RecyclerView rv = view.findViewById(R.id.rvHisaab);
        adapter = new HisaabAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        applyListeners();
        loadTodayTransactions();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayTransactions();
    }

    private void loadTodayTransactions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            allTransactions.clear();
            applyFilter("ALL");
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Start of today
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        // Start of tomorrow (exclusive)
        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.DAY_OF_MONTH, 1);

        Timestamp startTs = new Timestamp(startCal.getTime());
        Timestamp endTs = new Timestamp(endCal.getTime());

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", startTs)
                .whereLessThan("createdAt", endTs)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    ArrayList<HisaabItem> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        Double amountObj = doc.getDouble("amount");
                        if (amountObj == null) continue;

                        double amount = amountObj;
                        String type = safe(doc.getString("category"));
                        String description = safe(doc.getString("description"));
                        String customerName = safe(doc.getString("customerName"));

                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        String timeText = buildTimeText(createdAt);

                        String title = description.isEmpty() ? defaultTitle(type, customerName) : description;
                        String amountText = formatAmountByType(type, amount);
                        int amountColor = getAmountColor(type);

                        list.add(new HisaabItem(title, timeText, amountText, type, amountColor));
                    }

                    allTransactions = list;
                    applyFilter("ALL");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String buildTimeText(Timestamp timestamp) {
        if (timestamp == null) return "Aaj";
        Date d = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mma", Locale.getDefault());
        return "Aaj, " + sdf.format(d).toLowerCase(Locale.getDefault());
    }

    private String defaultTitle(String type, String customerName) {
        switch (type) {
            case "SALE":
                return "Sale Entry";
            case "RECEIVABLE":
                return customerName.isEmpty() ? "Udhaar Entry" : customerName + " ka Udhaar";
            case "EXPENSE":
                return "Expense Entry";
            case "SUPPLIER":
                return "Supplier Payment";
            default:
                return "Transaction";
        }
    }

    private String formatAmountByType(String type, double amount) {
        String amt = String.format(Locale.US, "%,.0f", amount);

        if ("SALE".equals(type)) return "+Rs. " + amt;
        if ("EXPENSE".equals(type) || "SUPPLIER".equals(type)) return "-Rs. " + amt;
        return "Rs. " + amt;
    }

    private int getAmountColor(String type) {
        switch (type) {
            case "SALE":
                return Color.parseColor("#2D6A4F");
            case "RECEIVABLE":
                return Color.parseColor("#F4A261");
            case "EXPENSE":
            case "SUPPLIER":
                return Color.parseColor("#D64545");
            default:
                return Color.parseColor("#2F3740");
        }
    }
}