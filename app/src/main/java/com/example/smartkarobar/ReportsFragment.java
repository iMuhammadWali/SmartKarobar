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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private ImageView ivProfile;
    private TextView tvAiSummary, tvWeeklySalesAmount;
    private TextView tvProfitProjectionAmount, tvProfitProjectionSub;
    private LineChart lineChartWeeklySales;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ReportsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        init(view);
        applyListeners();
        setupWeeklyChart();
        loadReportData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReportData();
    }

    private void init(View v) {
        ivProfile = v.findViewById(R.id.ivProfile);
        tvAiSummary = v.findViewById(R.id.tvAiSummary);
        tvWeeklySalesAmount = v.findViewById(R.id.tvWeeklySalesAmount);
        tvProfitProjectionAmount = v.findViewById(R.id.tvProfitProjectionAmount);
        tvProfitProjectionSub = v.findViewById(R.id.tvProfitProjectionSub);
        lineChartWeeklySales = v.findViewById(R.id.lineChartWeeklySales);
    }

    private void applyListeners() {
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

    private void setupWeeklyChart() {
        lineChartWeeklySales.getDescription().setEnabled(false);
        lineChartWeeklySales.getLegend().setEnabled(false);
        lineChartWeeklySales.setTouchEnabled(false);
        lineChartWeeklySales.setDragEnabled(false);
        lineChartWeeklySales.setScaleEnabled(false);
        lineChartWeeklySales.setPinchZoom(false);
        lineChartWeeklySales.setNoDataText("No weekly data");

        XAxis xAxis = lineChartWeeklySales.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#7B848A"));
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}
        ));

        YAxis left = lineChartWeeklySales.getAxisLeft();
        left.setDrawGridLines(true);
        left.setGridColor(Color.parseColor("#E3ECE7"));
        left.setTextColor(Color.parseColor("#7B848A"));
        left.setTextSize(10f);
        left.setAxisMinimum(0f);
        left.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Rs " + (int) value;
            }
        });

        YAxis right = lineChartWeeklySales.getAxisRight();
        right.setEnabled(false);
    }
    private void loadReportData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            bindZeroState();
            return;
        }

        Calendar now = Calendar.getInstance();

        // Setup for Monthly Data
        Calendar monthStart = (Calendar) now.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);

        Calendar nextMonthStart = (Calendar) monthStart.clone();
        nextMonthStart.add(Calendar.MONTH, 1);

        // Setup for Weekly Data
        Calendar weekStart = (Calendar) now.clone();
        weekStart.setFirstDayOfWeek(Calendar.MONDAY);
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);

        Calendar nextWeekStart = (Calendar) weekStart.clone();
        nextWeekStart.add(Calendar.WEEK_OF_YEAR, 1);

        Timestamp monthStartTs = new Timestamp(monthStart.getTime());
        Timestamp nextMonthStartTs = new Timestamp(nextMonthStart.getTime());
        Timestamp weekStartTs = new Timestamp(weekStart.getTime());
        Timestamp nextWeekStartTs = new Timestamp(nextWeekStart.getTime());

        // 1. FETCH MONTHLY TRANSACTIONS & CALCULATE PROJECTION
        db.collection("users")
                .document(user.getUid())
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", monthStartTs)
                .whereLessThan("createdAt", nextMonthStartTs)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(monthSnap -> {
                    double sales = 0, expense = 0, receivable = 0, payable = 0;

                    for (QueryDocumentSnapshot doc : monthSnap) {
                        Double amtObj = doc.getDouble("amount");
                        if (amtObj == null) continue;

                        double amt = amtObj;
                        String cat = doc.getString("category");
                        if (cat == null) cat = "";

                        switch (cat) {
                            case "SALE": sales += amt; break;
                            case "EXPENSE": expense += amt; break;
                            case "RECEIVABLE": receivable += amt; break;
                            case "PAYABLE": payable += amt; break;
                        }
                    }

                    // --- PROJECTION LOGIC START ---
                    double currentProfit = sales - expense;
                    Calendar calendar = Calendar.getInstance();
                    int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                    // Math: (Total Profit so far / Day of the month) * Days in month
                    double dailyAverage = currentProfit / Math.max(1, currentDay);
                    double projectedProfit = dailyAverage * totalDaysInMonth;

                    tvProfitProjectionAmount.setText("Rs. " + formatAmount(Math.abs(projectedProfit)));

                    if (projectedProfit >= 0) {
                        tvProfitProjectionAmount.setTextColor(Color.parseColor("#9FE3C3"));
                        tvProfitProjectionSub.setText("Projected net gain for " + totalDaysInMonth + " days");
                    } else {
                        tvProfitProjectionAmount.setTextColor(Color.parseColor("#FFD6D6"));
                        tvProfitProjectionSub.setText("Projected net loss for " + totalDaysInMonth + " days");
                    }
                    // --- PROJECTION LOGIC END ---

                    tvAiSummary.setText(buildAiSummary(sales, expense, receivable, payable));
                })
                .addOnFailureListener(e -> bindZeroState());

        // 2. FETCH WEEKLY TRANSACTIONS FOR THE CHART
        db.collection("users")
                .document(user.getUid())
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", weekStartTs)
                .whereLessThan("createdAt", nextWeekStartTs)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(weekSnap -> {
                    double weekSales = 0;
                    double[] daySales = new double[7];

                    long weekStartMillis = weekStart.getTimeInMillis();

                    for (QueryDocumentSnapshot doc : weekSnap) {
                        Double amtObj = doc.getDouble("amount");
                        if (amtObj == null) continue;

                        String cat = doc.getString("category");
                        Timestamp ts = doc.getTimestamp("createdAt");

                        if ("SALE".equals(cat)) {
                            weekSales += amtObj;

                            if (ts != null) {
                                long diff = ts.toDate().getTime() - weekStartMillis;
                                int dayIndex = (int) (diff / (24L * 60L * 60L * 1000L));
                                if (dayIndex >= 0 && dayIndex < 7) {
                                    daySales[dayIndex] += amtObj;
                                }
                            }
                        }
                    }

                    tvWeeklySalesAmount.setText("Rs. " + formatAmount(weekSales));
                    bindWeeklyChart(daySales);
                })
                .addOnFailureListener(e -> {
                    tvWeeklySalesAmount.setText("Rs. 0");
                    bindWeeklyChart(new double[]{0, 0, 0, 0, 0, 0, 0});
                });
    }
    private void bindWeeklyChart(double[] daySales) {
        ArrayList<Entry> entries = new ArrayList<>();
        float max = 0f;

        for (int i = 0; i < 7; i++) {
            float y = (float) daySales[i];
            entries.add(new Entry(i, y));
            if (y > max) max = y;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Sales");
        dataSet.setColor(Color.parseColor("#2D6A4F"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.parseColor("#2D6A4F"));
        dataSet.setCircleRadius(3.5f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#D8F3DC"));
        dataSet.setFillAlpha(170);

        LineData lineData = new LineData(dataSet);
        lineChartWeeklySales.setData(lineData);

        YAxis left = lineChartWeeklySales.getAxisLeft();
        left.setAxisMaximum(max <= 0 ? 100f : (max * 1.25f));

        lineChartWeeklySales.invalidate();
    }

    private void bindZeroState() {
        tvAiSummary.setText("Abhi data kam hai. Transactions add karein taake smart summary ban sake.");
        tvWeeklySalesAmount.setText("Rs. 0");
        tvProfitProjectionAmount.setText("Rs. 0");
        tvProfitProjectionSub.setText("Expected net gain this month");
        tvProfitProjectionAmount.setTextColor(Color.parseColor("#9FE3C3"));
        bindWeeklyChart(new double[]{0, 0, 0, 0, 0, 0, 0});
    }

    private String buildAiSummary(double sales, double expense, double receivable, double payable) {
        double net = sales - expense;

        if (sales == 0 && expense == 0 && receivable == 0 && payable == 0) {
            return "Abhi koi transaction nahi mili. Aaj ki entry add karein aur smart report dekhein.";
        }

        if (net >= 0) {
            return "Is mahine bikri kharch se zyada rahi. Net bachat Rs. " + formatAmount(net)
                    + " hai. Receivables follow-up se cash flow aur behtar ho sakta hai.";
        } else {
            return "Is mahine kharcha bikri se zyada raha. Net gap Rs. " + formatAmount(Math.abs(net))
                    + " hai. Expenses aur payables control se profit improve hoga.";
        }
    }

    private String formatAmount(double value) {
        return String.format(Locale.US, "%,.0f", value);
    }
}