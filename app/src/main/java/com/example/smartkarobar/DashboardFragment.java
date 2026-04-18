package com.example.smartkarobar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private LinearLayout btnAddTransaction;
    private TextView tvSalesAmount, tvExpensesAmount, tvReceivablesAmount, tvPayablesAmount, tvNetCashAmount;

    private ImageView ivProfile;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        btnAddTransaction.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // quick load: cache first, then server
        loadThisMonthDashboard(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadThisMonthDashboard(true);
    }

    private void init(View v) {
        btnAddTransaction = v.findViewById(R.id.btnAddExpense);
        tvSalesAmount = v.findViewById(R.id.tvSalesAmount);
        tvExpensesAmount = v.findViewById(R.id.tvExpensesAmount);
        tvReceivablesAmount = v.findViewById(R.id.tvReceivablesAmount);
        tvPayablesAmount = v.findViewById(R.id.tvPayablesAmount);
        tvNetCashAmount = v.findViewById(R.id.tvNetCashAmount);
    }

    private void loadThisMonthDashboard(boolean cacheFirst) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            setAllAmounts(0, 0, 0, 0, 0);
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar nextCal = (Calendar) startCal.clone();
        nextCal.add(Calendar.MONTH, 1);

        Date monthStart = startCal.getTime();
        Date nextMonthStart = nextCal.getTime();

        Query query = db.collection("users")
                .document(uid)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(monthStart))
                .whereLessThan("createdAt", new Timestamp(nextMonthStart))
                .orderBy("createdAt", Query.Direction.DESCENDING);

        if (cacheFirst) {
            // 1) instantly try cache
            query.get(Source.CACHE)
                    .addOnSuccessListener(this::bindSnapshotToUi)
                    .addOnFailureListener(e -> { /* ignore cache miss */ })
                    .addOnCompleteListener(task -> {
                        // 2) then always refresh from server
                        query.get(Source.SERVER)
                                .addOnSuccessListener(this::bindSnapshotToUi)
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    });
        } else {
            query.get(Source.SERVER)
                    .addOnSuccessListener(this::bindSnapshotToUi)
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void bindSnapshotToUi(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
        double saleTotal = 0;
        double receivableTotal = 0;
        double supplierTotal = 0; // payable
        double expenseTotal = 0;

        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
            Double amountObj = doc.getDouble("amount");
            if (amountObj == null) continue;

            double amount = amountObj;
            String category = doc.getString("category");
            if (category == null) category = "";

            switch (category) {
                case "SALE":
                    saleTotal += amount;
                    break;
                case "RECEIVABLE":
                    receivableTotal += amount;
                    break;
                case "SUPPLIER":
                    supplierTotal += amount;
                    break;
                case "EXPENSE":
                    expenseTotal += amount;
                    break;
            }
        }

        // Net cash in hand: sales - expenses - supplier payments
        // (receivables are pending, so not added yet)
        double netCash = saleTotal - expenseTotal - supplierTotal;

        setAllAmounts(saleTotal, expenseTotal, receivableTotal, supplierTotal, netCash);
    }

    private void setAllAmounts(double sales, double expenses, double receivables, double payables, double netCash) {
        tvSalesAmount.setText("Rs. " + formatAmount(sales));
        tvExpensesAmount.setText("Rs. " + formatAmount(expenses));
        tvReceivablesAmount.setText("Rs. " + formatAmount(receivables));
        tvPayablesAmount.setText("Rs. " + formatAmount(payables));

        String netPrefix = netCash < 0 ? "-Rs. " : "Rs. ";
        tvNetCashAmount.setText(netPrefix + formatAmount(Math.abs(netCash)));
    }

    private String formatAmount(double value) {
        return String.format(Locale.US, "%,.0f", value);
    }
}