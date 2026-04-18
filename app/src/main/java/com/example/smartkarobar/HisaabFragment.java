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
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        ArrayList<HisaabItem> transactions = getTransactions();
        tvTransactionCount.setText(transactions.size() + " TRANSACTIONS");

        RecyclerView rv = view.findViewById(R.id.rvHisaab);
        HisaabAdapter adapter = new HisaabAdapter();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        adapter.submitList(transactions);

    }
    private ArrayList<HisaabItem> getTransactions() {
        // TODO: Fetch today's transactions from firebase.
        ArrayList<HisaabItem> list = new ArrayList<>();
        list.add(new HisaabItem("General Store Sale", "Aaj, 2:30pm", "+Rs. 5,000", "SALE", Color.parseColor("#2D6A4F")));
        list.add(new HisaabItem("Ali ka Udhaar", "Aaj, 1:16pm", "Rs. 2,500", "UDHAAR", Color.parseColor("#F4A261")));
        list.add(new HisaabItem("Bijli ka Bill", "Aaj, 10:45am", "-Rs. 1,200", "EXPENSE", Color.parseColor("#D64545")));
        list.add(new HisaabItem("Inventory Purchase", "Kal, 5:00pm", "-Rs. 8,000", "EXPENSE", Color.parseColor("#D64545")));
        return list;
    }
}