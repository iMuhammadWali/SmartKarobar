package com.example.smartkarobar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HisaabFragment extends Fragment {

    // Type chips
    private TextView tvTransactionCount, tvAll, tvSales, tvReceivables, tvExpenses, tvPayables;
    // Range chips + title
    private TextView tvRangeToday, tvRangeWeek, tvRangeMonth, tvSectionTitle;
    private ImageView ivProfile;
    private TextView tvUsername;

    private final ArrayList<HisaabItem> allTransactions = new ArrayList<>();
    private HisaabAdapter adapter;

    private String selectedType = "ALL";     // ALL | SALE | RECEIVABLE | EXPENSE | PAYABLE
    private String selectedRange = "TODAY";  // TODAY | WEEK | MONTH

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public HisaabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hisaab, container, false);
    }

    private void init(View v) {
        tvTransactionCount = v.findViewById(R.id.tvTransactionsCount);
        tvAll = v.findViewById(R.id.tvAll);
        tvSales = v.findViewById(R.id.tvSales);
        tvReceivables = v.findViewById(R.id.tvReceivables);
        tvExpenses = v.findViewById(R.id.tvExpenses);
        tvPayables = v.findViewById(R.id.tvPayables);

        tvRangeToday = v.findViewById(R.id.tvRangeToday);
        tvRangeWeek = v.findViewById(R.id.tvRangeWeek);
        tvRangeMonth = v.findViewById(R.id.tvRangeMonth);
        tvSectionTitle = v.findViewById(R.id.tvSectionTitle);

        ivProfile = v.findViewById(R.id.ivProfile);
        tvUsername = v.findViewById(R.id.tvUsername);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        RecyclerView rv = view.findViewById(R.id.rvHisaab);

        adapter = new HisaabAdapter(new HisaabAdapter.ActionListener() {
            @Override
            public void onClearUdhaar(@NonNull HisaabItem item) {
                confirmAndClear(item, "RECEIVABLE");
            }

            @Override
            public void onClearPayable(@NonNull HisaabItem item) {
                confirmAndClear(item, "PAYABLE");
            }

            @Override
            public void onDelete(@NonNull HisaabItem item) {
                confirmAndDelete(item);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        applyListeners();
        updateChipUI();
        updateRangeUI();
        loadUsername();
        loadTransactionsByRange();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsername();
        loadTransactionsByRange();
    }

    private void applyListeners() {
        // type chips
        tvAll.setOnClickListener(v -> applyFilter("ALL"));
        tvSales.setOnClickListener(v -> applyFilter("SALE"));
        tvReceivables.setOnClickListener(v -> applyFilter("RECEIVABLE"));
        tvExpenses.setOnClickListener(v -> applyFilter("EXPENSE"));
        tvPayables.setOnClickListener(v -> applyFilter("PAYABLE"));

        // range chips
        tvRangeToday.setOnClickListener(v -> {
            selectedRange = "TODAY";
            updateRangeUI();
            loadTransactionsByRange();
        });

        tvRangeWeek.setOnClickListener(v -> {
            selectedRange = "WEEK";
            updateRangeUI();
            loadTransactionsByRange();
        });

        tvRangeMonth.setOnClickListener(v -> {
            selectedRange = "MONTH";
            updateRangeUI();
            loadTransactionsByRange();
        });

        // profile/logout
        ivProfile.setOnClickListener(v -> {
            final String[] options = {"Logout"};

            new AlertDialog.Builder(requireContext())
                    .setTitle("Choose Option")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Logout")
                                    .setMessage("Are you sure you want to logout?")
                                    .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                                    .setPositiveButton("Logout", (d, w) -> {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent i = new Intent(requireActivity(), SignupActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                        requireActivity().finish();
                                    })
                                    .show();
                        }
                    })
                    .show();
        });
    }

    private void loadUsername() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvUsername.setText("Guest");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String username = "";
                    if (doc.exists() && doc.getString("username") != null) {
                        username = doc.getString("username").trim();
                    }
                    tvUsername.setText(username.isEmpty() ? "User" : username);
                })
                .addOnFailureListener(e -> tvUsername.setText("User"));
    }

    private void updateChipUI() {
        int selBg = R.drawable.bg_nav_selected;
        int unselBg = R.drawable.bg_rounded_very_light_green;
        int sel = Color.WHITE;
        int unsel = Color.parseColor("#2D6A4F");

        tvAll.setBackgroundResource(selectedType.equals("ALL") ? selBg : unselBg);
        tvSales.setBackgroundResource(selectedType.equals("SALE") ? selBg : unselBg);
        tvReceivables.setBackgroundResource(selectedType.equals("RECEIVABLE") ? selBg : unselBg);
        tvExpenses.setBackgroundResource(selectedType.equals("EXPENSE") ? selBg : unselBg);
        tvPayables.setBackgroundResource(selectedType.equals("PAYABLE") ? selBg : unselBg);

        tvAll.setTextColor(selectedType.equals("ALL") ? sel : unsel);
        tvSales.setTextColor(selectedType.equals("SALE") ? sel : unsel);
        tvReceivables.setTextColor(selectedType.equals("RECEIVABLE") ? sel : unsel);
        tvExpenses.setTextColor(selectedType.equals("EXPENSE") ? sel : unsel);
        tvPayables.setTextColor(selectedType.equals("PAYABLE") ? sel : unsel);
    }

    private void updateRangeUI() {
        int selBg = R.drawable.bg_nav_selected;
        int unselBg = R.drawable.bg_rounded_very_light_green;
        int sel = Color.WHITE;
        int unsel = Color.parseColor("#2D6A4F");

        tvRangeToday.setBackgroundResource(selectedRange.equals("TODAY") ? selBg : unselBg);
        tvRangeWeek.setBackgroundResource(selectedRange.equals("WEEK") ? selBg : unselBg);
        tvRangeMonth.setBackgroundResource(selectedRange.equals("MONTH") ? selBg : unselBg);

        tvRangeToday.setTextColor(selectedRange.equals("TODAY") ? sel : unsel);
        tvRangeWeek.setTextColor(selectedRange.equals("WEEK") ? sel : unsel);
        tvRangeMonth.setTextColor(selectedRange.equals("MONTH") ? sel : unsel);

        if ("TODAY".equals(selectedRange)) {
            tvSectionTitle.setText("Aaj ki Tafseel");
        } else if ("WEEK".equals(selectedRange)) {
            tvSectionTitle.setText("Is Hafte ki Tafseel");
        } else {
            tvSectionTitle.setText("Is Mahine ki Tafseel");
        }
    }

    private void applyFilter(String type) {
        selectedType = type;
        updateChipUI();

        ArrayList<HisaabItem> filtered = new ArrayList<>();
        if ("ALL".equals(type)) {
            filtered.addAll(allTransactions);
        } else {
            for (HisaabItem item : allTransactions) {
                if (type.equalsIgnoreCase(item.getType())) {
                    filtered.add(item);
                }
            }
        }

        adapter.submitList(filtered);
        tvTransactionCount.setText(filtered.size() + " TRANSACTIONS");
    }

    private void loadTransactionsByRange() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            allTransactions.clear();
            applyFilter("ALL");
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar startCal = Calendar.getInstance();
        Calendar endCal;

        if ("TODAY".equals(selectedRange)) {
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.DAY_OF_MONTH, 1);

        } else if ("WEEK".equals(selectedRange)) {
            startCal.setFirstDayOfWeek(Calendar.MONDAY);
            startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.WEEK_OF_YEAR, 1);

        } else { // MONTH
            startCal.set(Calendar.DAY_OF_MONTH, 1);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.MONTH, 1);
        }

        Timestamp startTs = new Timestamp(startCal.getTime());
        Timestamp endTs = new Timestamp(endCal.getTime());

        db.collection("users")
                .document(user.getUid())
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", startTs)
                .whereLessThan("createdAt", endTs)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    allTransactions.clear();

                    for (QueryDocumentSnapshot doc : snap) {
                        Double amountObj = doc.getDouble("amount");
                        if (amountObj == null) continue;

                        double amount = amountObj;
                        String type = safe(doc.getString("category"));
                        String description = safe(doc.getString("description"));
                        String customerName = safe(doc.getString("customerName"));
                        Timestamp createdAt = doc.getTimestamp("createdAt");

                        String title = description.isEmpty() ? defaultTitle(type, customerName) : description;
                        String subtitle = buildTimeText(createdAt);
                        String amountText = formatAmountByType(type, amount);
                        int amountColor = getAmountColor(type);

                        HisaabItem item = new HisaabItem(title, subtitle, amountText, type, amountColor);
                        item.setDocId(doc.getId());
                        item.setRawAmount(amount);
                        allTransactions.add(item);
                    }

                    applyFilter(selectedType);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmAndClear(HisaabItem item, String sourceType) {
        String title = "RECEIVABLE".equals(sourceType) ? "Clear Udhaar" : "Clear Payable";
        String msg = "RECEIVABLE".equals(sourceType)
                ? "Is entry ko clear karke Sales mein add karna hai?"
                : "Is entry ko clear karke Expense mein add karna hai?";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Yes", (d, w) -> clearEntry(item, sourceType))
                .show();
    }

    private void clearEntry(HisaabItem item, String sourceType) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String newCategory = "RECEIVABLE".equals(sourceType) ? "SALE" : "EXPENSE";
        String clearNote = "RECEIVABLE".equals(sourceType) ? "Udhaar cleared" : "Payable cleared";

        double amount = item.getRawAmount() > 0 ? item.getRawAmount() : parseAmount(item.getAmount());
        if (amount <= 0) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> tx = new HashMap<>();
        tx.put("amount", amount);
        tx.put("description", clearNote + " | " + safe(item.getTitle()));
        tx.put("category", newCategory);
        tx.put("customerName", "");
        tx.put("customerPhone", "");
        tx.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .add(tx)
                .addOnSuccessListener(ref -> {
                    String oldDocId = item.getDocId();
                    if (oldDocId == null || oldDocId.trim().isEmpty()) {
                        Toast.makeText(requireContext(), "New entry added (old id missing)", Toast.LENGTH_SHORT).show();
                        loadTransactionsByRange();
                        return;
                    }

                    db.collection("users")
                            .document(uid)
                            .collection("transactions")
                            .document(oldDocId)
                            .delete()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(requireContext(), "Entry cleared", Toast.LENGTH_SHORT).show();
                                loadTransactionsByRange();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Added new entry, old not deleted: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                loadTransactionsByRange();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Clear failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void confirmAndDelete(@NonNull HisaabItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Entry")
                .setMessage("Kya aap is transaction ko delete karna chahte hain?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Delete", (d, w) -> deleteEntry(item))
                .show();
    }

    private void deleteEntry(@NonNull HisaabItem item) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = item.getDocId();
        if (docId == null || docId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Cannot delete: missing doc id", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("transactions")
                .document(docId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                    loadTransactionsByRange();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String buildTimeText(Timestamp timestamp) {
        if (timestamp == null) return "No time";
        Date d = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, h:mma", Locale.getDefault());
        return sdf.format(d).toLowerCase(Locale.getDefault());
    }

    private String defaultTitle(String type, String customerName) {
        switch (type) {
            case "SALE":
                return "Sale Entry";
            case "RECEIVABLE":
                return customerName.isEmpty() ? "Udhaar Entry" : customerName + " ka Udhaar";
            case "EXPENSE":
                return "Expense Entry";
            case "PAYABLE":
                return "Payable Entry";
            default:
                return "Transaction";
        }
    }

    private String formatAmountByType(String type, double amount) {
        String amt = String.format(Locale.US, "%,.0f", amount);

        if ("SALE".equals(type)) return "+Rs. " + amt;
        if ("EXPENSE".equals(type)) return "-Rs. " + amt;
        if ("PAYABLE".equals(type)) return "Rs. " + amt;
        return "Rs. " + amt;
    }

    private int getAmountColor(String type) {
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

    private double parseAmount(String amountText) {
        if (amountText == null) return 0;
        String cleaned = amountText.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return 0;
        try {
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }
}