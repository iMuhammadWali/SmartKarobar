package com.example.smartkarobar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private ImageView ivProfile;
    private TextView tvUsername, tvAiSummary, tvWeeklySalesAmount;
    private TextView tvProfitProjectionAmount, tvProfitProjectionSub;
    private TextView tvSentiment;
    private View vSentimentDot;
    private LinearLayout llChatHistory, llChips;
    private EditText etPulseInput;
    private AppCompatButton btnPulseSend;
    private LineChart lineChartWeeklySales;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GeminiHelper gemini;

    private ScrollView svOuter, svChatHistory;
    private double liveSales, liveExpenses, liveReceivables, livePayables;
    private final List<Map<String, String>> chatHistory = new ArrayList<>();
    private boolean isLoading = false;

    private static final String[] SUGGESTION_CHIPS = {
            "Expenses kyun zyada hain?",
            "Next month ka forecast",
            "Receivables collect karne ka plan",
            "Profit badhane ke tips",
            "Sabse zyada bikri ka din"
    };

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
        gemini = new GeminiHelper();

        init(view);
        applyListeners();
        setupWeeklyChart();
        setupChips();
        loadUsername();
        loadReportData();
    }

    private void init(View v) {
        ivProfile = v.findViewById(R.id.ivProfile);
        tvUsername = v.findViewById(R.id.tvUsername);
        tvAiSummary = v.findViewById(R.id.tvAiSummary);
        tvWeeklySalesAmount = v.findViewById(R.id.tvWeeklySalesAmount);
        tvProfitProjectionAmount = v.findViewById(R.id.tvProfitProjectionAmount);
        tvProfitProjectionSub = v.findViewById(R.id.tvProfitProjectionSub);
        tvSentiment = v.findViewById(R.id.tvSentiment);
        vSentimentDot = v.findViewById(R.id.vSentimentDot);
        llChatHistory = v.findViewById(R.id.llChatHistory);
        llChips = v.findViewById(R.id.llChips);
        etPulseInput = v.findViewById(R.id.etPulseInput);
        btnPulseSend = v.findViewById(R.id.btnPulseSend);
        lineChartWeeklySales = v.findViewById(R.id.lineChartWeeklySales);
        svOuter = v.findViewById(R.id.svOuter);
//        svChatHistory = v.findViewById(R.id.svChatHistory);
    }

    private void setupChips() {
        for (String chip : SUGGESTION_CHIPS) {
            TextView tv = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(8);
            tv.setLayoutParams(lp);
            tv.setText(chip);
            tv.setTextColor(Color.parseColor("#9FE3C3"));
            tv.setTextSize(11);
            tv.setBackgroundResource(R.drawable.bg_nav_selected);
            tv.setPadding(24, 12, 24, 12);

            final String question = chip;
            tv.setOnClickListener(v -> {
                if (isLoading) return;
                etPulseInput.setText(question);
                askPulseQuestion(question);
            });

            llChips.addView(tv);
        }
    }

    private void applyListeners() {
        btnPulseSend.setOnClickListener(v -> {
            if (isLoading) return;
            String q = etPulseInput.getText().toString().trim();
            if (!q.isEmpty()) {
                etPulseInput.setText("");
                askPulseQuestion(q);
            }
        });

        etPulseInput.setOnEditorActionListener((v, actionId, event) -> {
            if (isLoading) return true;
            String q = etPulseInput.getText().toString().trim();
            if (!q.isEmpty()) {
                etPulseInput.setText("");
                askPulseQuestion(q);
            }
            return true;
        });

        ivProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Kya aap nikalna chahte hain?")
                    .setPositiveButton("Logout", (d, w) -> {
                        auth.signOut();
                        MyApplication.username = "";
                        startActivity(new Intent(requireActivity(), SignupActivity.class));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Nahi", null).show();
        });
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnPulseSend.setEnabled(!loading);
        etPulseInput.setEnabled(!loading);
        btnPulseSend.setText(loading ? "..." : "Poochein");
    }

    private void askPulseQuestion(String question) {
        if (isLoading) return;
        setLoadingState(true);
        etPulseInput.setText("");

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "User");
        userMsg.put("text", question);
        chatHistory.add(userMsg);

        addChatBubble(question, true);
        TextView aiBubble = addChatBubble("...", false);

        gemini.askBusinessQuestion(
                liveSales, liveExpenses, liveReceivables, livePayables,
                question, chatHistory,
                new GeminiHelper.GeminiCallback() {
                    @Override
                    public void onResponse(String advice) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            Map<String, String> aiMsg = new HashMap<>();
                            aiMsg.put("role", "Assistant");
                            aiMsg.put("text", advice);
                            chatHistory.add(aiMsg);
                            typewriterEffect(aiBubble, advice);
                            setLoadingState(false);
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            aiBubble.setText("Maafi chahta hoon, dobara try karein.");
                            setLoadingState(false);
                        });
                    }
                });
    }

    private TextView addChatBubble(String text, boolean isUser) {
        llChatHistory.setVisibility(View.VISIBLE);

        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wlp.bottomMargin = 8;
        wrapper.setLayoutParams(wlp);

        TextView bubble = new TextView(requireContext());
        bubble.setText(text);
        bubble.setTextSize(12);
        bubble.setLineSpacing(4, 1);
        bubble.setPadding(24, 16, 24, 16);

        if (isUser) {
            bubble.setTextColor(Color.parseColor("#D8F3DC"));
            bubble.setBackgroundResource(R.drawable.bg_nav_selected);
        } else {
            bubble.setTextColor(Color.parseColor("#1B4D38"));
            bubble.setBackgroundResource(R.drawable.bg_light_green);
        }

        wrapper.addView(bubble);
        llChatHistory.addView(wrapper);
//        svChatHistory.post(() -> svChatHistory.fullScroll(ScrollView.FOCUS_DOWN));
//        svOuter.post(() -> svOuter.fullScroll(ScrollView.FOCUS_DOWN));
        return bubble;
    }

    private void typewriterEffect(TextView tv, String fullText) {
        tv.setText("");
        final int[] index = {0};
        final android.os.Handler h = new android.os.Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    tv.setText(fullText.substring(0, index[0]));
                    index[0]++;
                    h.postDelayed(this, 18);
                }
            }
        };
        h.post(r);
    }

    private void loadUsername() {
        if (MyApplication.username != null && !MyApplication.username.isEmpty()) {
            tvUsername.setText(MyApplication.username);
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("username");
                    if (name != null) {
                        MyApplication.username = name;
                        tvUsername.setText(name);
                    }
                });
    }

    private void loadReportData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        tvAiSummary.setText("Soch raha hoon... ✦");

        Calendar now = Calendar.getInstance();

        Calendar mStart = (Calendar) now.clone();
        mStart.set(Calendar.DAY_OF_MONTH, 1);
        mStart.set(Calendar.HOUR_OF_DAY, 0);
        mStart.set(Calendar.MINUTE, 0);
        mStart.set(Calendar.SECOND, 0);
        Calendar mEnd = (Calendar) mStart.clone();
        mEnd.add(Calendar.MONTH, 1);

        Calendar wStart = (Calendar) now.clone();
        wStart.setFirstDayOfWeek(Calendar.MONDAY);
        wStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        wStart.set(Calendar.HOUR_OF_DAY, 0);
        Calendar wEnd = (Calendar) wStart.clone();
        wEnd.add(Calendar.WEEK_OF_YEAR, 1);

        // Monthly fetch for AI + projection
        db.collection("users").document(user.getUid())
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(mStart.getTime()))
                .whereLessThan("createdAt", new Timestamp(mEnd.getTime()))
                .get()
                .addOnSuccessListener(snap -> {
                    double s = 0, e = 0, r = 0, p = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        double amt = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0;
                        String cat = doc.getString("category");
                        if ("SALE".equals(cat)) s += amt;
                        else if ("EXPENSE".equals(cat)) e += amt;
                        else if ("RECEIVABLE".equals(cat)) r += amt;
                        else if ("PAYABLE".equals(cat)) p += amt;
                    }
                    liveSales = s;
                    liveExpenses = e;
                    liveReceivables = r;
                    livePayables = p;

                    updateProjectionUI(s, e, now);
                    updateSentiment(s, e);
                    fetchAiAdvice(s, e, r, p);
                });

        // Weekly fetch for chart
        db.collection("users").document(user.getUid())
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(wStart.getTime()))
                .whereLessThan("createdAt", new Timestamp(wEnd.getTime()))
                .get()
                .addOnSuccessListener(snap -> {
                    double totalW = 0;
                    double[] days = new double[7];
                    long startMs = wStart.getTimeInMillis();
                    for (QueryDocumentSnapshot doc : snap) {
                        if ("SALE".equals(doc.getString("category"))) {
                            double amt = doc.getDouble("amount") != null ? doc.getDouble("amount") : 0;
                            totalW += amt;
                            Timestamp ts = doc.getTimestamp("createdAt");
                            if (ts != null) {
                                int idx = (int) ((ts.toDate().getTime() - startMs) / 86400000L);
                                if (idx >= 0 && idx < 7) days[idx] += amt;
                            }
                        }
                    }
                    tvWeeklySalesAmount.setText("Rs. " + format(totalW));
                    bindChart(days);
                });
    }

    private void updateSentiment(double sales, double expenses) {
        double ratio = expenses == 0 ? 1 : sales / expenses;
        if (ratio >= 1.3) {
            tvSentiment.setText("Business health: Strong growth this month");
            vSentimentDot.setBackgroundResource(R.drawable.bg_pin_dot_filled);
        } else if (ratio >= 1.0) {
            tvSentiment.setText("Business health: Stable — watch expenses");
            vSentimentDot.setBackgroundResource(R.drawable.bg_pin_dot_filled);
        } else {
            tvSentiment.setText("Business health: Expenses exceed sales — take action");
            vSentimentDot.setBackgroundResource(R.drawable.bg_pin_dot_empty);
        }
    }

    private void fetchAiAdvice(double s, double e, double r, double p) {
        gemini.getBusinessAdvice(s, e, r, p, new GeminiHelper.GeminiCallback() {
            @Override
            public void onResponse(String advice) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> typewriterEffect(tvAiSummary, advice));
            }

            @Override
            public void onError(Throwable t) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        tvAiSummary.setText("Transactions dekh kar behtar advice de sakta hoon."));
            }
        });
    }

    private void updateProjectionUI(double s, double e, Calendar now) {
        double profit = s - e;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int total = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        double projected = (profit / day) * total;
        tvProfitProjectionAmount.setText("Rs. " + format(Math.abs(projected)));
        tvProfitProjectionAmount.setTextColor(projected >= 0
                ? Color.parseColor("#9FE3C3") : Color.parseColor("#FFD6D6"));
        tvProfitProjectionSub.setText("Projected net " + (projected >= 0 ? "gain" : "loss") + " is mahine ke liye");
    }

    private void bindChart(double[] data) {
        ArrayList<Entry> entries = new ArrayList<>();
        float max = 0;
        for (int i = 0; i < 7; i++) {
            entries.add(new Entry(i, (float) data[i]));
            if (data[i] > max) max = (float) data[i];
        }
        LineDataSet set = new LineDataSet(entries, "Weekly Sales");
        set.setColor(Color.parseColor("#2D6A4F"));
        set.setLineWidth(3f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#D8F3DC"));
        set.setDrawValues(false);
        lineChartWeeklySales.setData(new LineData(set));
        lineChartWeeklySales.getAxisLeft().setAxisMaximum(max == 0 ? 1000 : max * 1.2f);
        lineChartWeeklySales.invalidate();
    }

    private void setupWeeklyChart() {
        lineChartWeeklySales.getDescription().setEnabled(false);
        lineChartWeeklySales.getLegend().setEnabled(false);
        XAxis x = lineChartWeeklySales.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        lineChartWeeklySales.getAxisRight().setEnabled(false);
    }

    private String format(double val) {
        return String.format(Locale.US, "%,.0f", val);
    }
}