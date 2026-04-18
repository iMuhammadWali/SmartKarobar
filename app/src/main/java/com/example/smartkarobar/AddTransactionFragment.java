package com.example.smartkarobar;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddTransactionFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinearLayout btnSale, btnReceivable, btnSupplier, btnExpense, layoutUdhaarFields, btnSaveTransaction;
    private EditText etAmount, etDescription, etCustomerName, etCustomerPhone;

    private ImageView ivSale, ivReceivable, ivSupplier, ivExpense;
    private TextView tvSale, tvReceivable, tvSupplier, tvExpense;
    private String selectedCategory = "SALE";

    public AddTransactionFragment() {
        // Required empty public constructor
    }

    public static AddTransactionFragment newInstance(String param1, String param2) {
        AddTransactionFragment fragment = new AddTransactionFragment();
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
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    private void init(View v) {
        btnSale = v.findViewById(R.id.btnSale);
        btnReceivable = v.findViewById(R.id.btnReceivable);
        btnSupplier = v.findViewById(R.id.btnSupplier);
        btnExpense = v.findViewById(R.id.btnExpense);

        layoutUdhaarFields = v.findViewById(R.id.layoutUdhaarFields);
        btnSaveTransaction = v.findViewById(R.id.btnSaveTransaction);

        etAmount = v.findViewById(R.id.etAmount);
        etDescription = v.findViewById(R.id.etDescription);
        etCustomerName = v.findViewById(R.id.etCustomerName);
        etCustomerPhone = v.findViewById(R.id.etCustomerPhone);

        ivSale = (ImageView) btnSale.getChildAt(0);
        tvSale = (TextView) btnSale.getChildAt(1);

        ivReceivable = (ImageView) btnReceivable.getChildAt(0);
        tvReceivable = (TextView) btnReceivable.getChildAt(1);

        ivSupplier = (ImageView) btnSupplier.getChildAt(0);
        tvSupplier = (TextView) btnSupplier.getChildAt(1);

        ivExpense = (ImageView) btnExpense.getChildAt(0);
        tvExpense = (TextView) btnExpense.getChildAt(1);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        setCategory("RECEIVABLE"); // default selected
    }

    private void setCategory(String category) {
        selectedCategory = category;

        layoutUdhaarFields.setVisibility("RECEIVABLE".equals(category) ? View.VISIBLE : View.GONE);

        int selectedBg = R.drawable.bg_rounded_dark_green;
        int unselectedBg = R.drawable.bg_quick_item;

        int selectedText = ContextCompat.getColor(requireContext(), android.R.color.white);
        int unselectedText = 0xFF2F3740;
        int selectedIcon = ContextCompat.getColor(requireContext(), android.R.color.white);
        int unselectedIcon = 0xFF2D6A4F;

        // reset all
        applyCategoryStyle(btnSale, ivSale, tvSale, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
        applyCategoryStyle(btnReceivable, ivReceivable, tvReceivable, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
        applyCategoryStyle(btnSupplier, ivSupplier, tvSupplier, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
        applyCategoryStyle(btnExpense, ivExpense, tvExpense, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);

        // select one
        switch (category) {
            case "SALE":
                applyCategoryStyle(btnSale, ivSale, tvSale, true, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
                break;
            case "RECEIVABLE":
                applyCategoryStyle(btnReceivable, ivReceivable, tvReceivable, true, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
                break;
            case "SUPPLIER":
                applyCategoryStyle(btnSupplier, ivSupplier, tvSupplier, true, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
                break;
            case "EXPENSE":
                applyCategoryStyle(btnExpense, ivExpense, tvExpense, true, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
                break;
        }
    }

    private void applyCategoryStyle(
            LinearLayout container,
            ImageView icon,
            TextView text,
            boolean selected,
            int selectedBg,
            int unselectedBg,
            int selectedText,
            int unselectedText,
            int selectedIcon,
            int unselectedIcon
    ) {
        container.setBackgroundResource(selected ? selectedBg : unselectedBg);
        text.setTextColor(selected ? selectedText : unselectedText);
        icon.setImageTintList(  ColorStateList.valueOf(selected ? selectedIcon : unselectedIcon));
    }

    private void onSaveClicked() {
        String amount = etAmount.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (amount.isEmpty() || amount.equals("0") || amount.equals(".")) {
            etAmount.setError("Enter valid amount");
            etAmount.requestFocus();
            return;
        }

        if ("RECEIVABLE".equals(selectedCategory)) {
            String customerName = etCustomerName.getText().toString().trim();
            if (customerName.isEmpty()) {
                etCustomerName.setError("Customer name required");
                etCustomerName.requestFocus();
                return;
            }
        }

        // TODO: save to Firebase/Room
        Toast.makeText(requireContext(),
                "Saved: " + selectedCategory + " | Rs. " + amount + " | " + desc,
                Toast.LENGTH_SHORT).show();
    }
}