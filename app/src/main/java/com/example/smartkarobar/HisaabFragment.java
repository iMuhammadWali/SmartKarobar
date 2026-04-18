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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HisaabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HisaabFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    TextView tvTransactionCount, tvAll, tvSales, tvReceiveables, tvExpenses;

    private ArrayList<HisaabItem> allTransactions = new ArrayList<>();
    HisaabAdapter adapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HisaabFragment() {
        // Required empty public constructor
    }

    public static HisaabFragment newInstance(String param1, String param2) {
        HisaabFragment fragment = new HisaabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        tvExpenses.setOnClickListener((v)->{
            applyFilter("EXPENSE");
        });
        tvReceiveables.setOnClickListener((v)->{
            applyFilter("RECEIVABLE");
        });
        tvAll.setOnClickListener((v)->{
            applyFilter("ALL");
        });
        tvSales.setOnClickListener((v)->{
            applyFilter("SALE");
        });


    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        allTransactions = getTransactions();

        RecyclerView rv = view.findViewById(R.id.rvHisaab);
        adapter = new HisaabAdapter();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        applyFilter("ALL");

        applyListeners();
    }
    private ArrayList<HisaabItem> getTransactions() {

        // TODO: Fetch today's transactions from firebase.
        ArrayList<HisaabItem> list = new ArrayList<>();
        list.add(new HisaabItem("General Store Sale", "Aaj, 2:30pm", "+Rs. 5,000", "SALE", Color.parseColor("#2D6A4F")));
        list.add(new HisaabItem("Ali ka Udhaar", "Aaj, 1:16pm", "Rs. 2,500", "RECEIVABLE", Color.parseColor("#F4A261")));
        list.add(new HisaabItem("Bijli ka Bill", "Aaj, 10:45am", "-Rs. 1,200", "EXPENSE", Color.parseColor("#D64545")));
        list.add(new HisaabItem("Inventory Purchase", "Kal, 5:00pm", "-Rs. 8,000", "EXPENSE", Color.parseColor("#D64545")));
        return list;
    }
}