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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HisaabFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HisaabFragment.
     */
    // TODO: Rename and change types and number of parameters
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rvHisaab);
        HisaabAdapter adapter = new HisaabAdapter();

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        // Later replace with Room/API/ViewModel
        adapter.submitList(getTransactions());

    }
    private ArrayList<HisaabItem> getTransactions() {
        ArrayList<HisaabItem> list = new ArrayList<>();
        list.add(new HisaabItem("General Store Sale", "Aaj, 2:30pm", "+Rs. 5,000", "NAQD", Color.parseColor("#2D6A4F")));
        list.add(new HisaabItem("Ali ka Udhaar", "Aaj, 1:16pm", "Rs. 2,500", "BAQI", Color.parseColor("#F4A261")));
        list.add(new HisaabItem("Bijli ka Bill", "Aaj, 10:45am", "-Rs. 1,200", "EXPENSE", Color.parseColor("#D64545")));
        list.add(new HisaabItem("Inventory Purchase", "Kal, 5:00pm", "-Rs. 8,000", "EXPENSE", Color.parseColor("#D64545")));
        return list;
    }
}