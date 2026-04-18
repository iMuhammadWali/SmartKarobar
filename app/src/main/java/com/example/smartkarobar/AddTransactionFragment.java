package com.example.smartkarobar;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddTransactionFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private LinearLayout btnSale, btnReceivable, btnSupplier, btnExpense, layoutUdhaarFields, btnSaveTransaction;
    private EditText etAmount, etDescription, etCustomerName, etCustomerPhone;
    private ImageView ivSale, ivReceivable, ivSupplier, ivExpense, ivBack;
    private TextView tvSale, tvReceivable, tvSupplier, tvExpense;

    private String selectedCategory = "RECEIVABLE";
    private boolean isSaving = false;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public AddTransactionFragment() {}

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
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        ivSale = v.findViewById(R.id.ivSale);
        tvSale = v.findViewById(R.id.tvSale);

        ivReceivable = v.findViewById(R.id.ivReceivable);
        tvReceivable = v.findViewById(R.id.tvReceivable);

        ivSupplier = v.findViewById(R.id.ivSupplier);
        tvSupplier = v.findViewById(R.id.tvSupplier);

        ivExpense = v.findViewById(R.id.ivExpense);
        tvExpense = v.findViewById(R.id.tvExpense);

        ivBack = v.findViewById(R.id.ivBack);
    }

    private void applyListeners() {
        btnSale.setOnClickListener(v -> setCategory("SALE"));
        btnReceivable.setOnClickListener(v -> setCategory("RECEIVABLE"));
        btnSupplier.setOnClickListener(v -> setCategory("SUPPLIER"));
        btnExpense.setOnClickListener(v -> setCategory("EXPENSE"));

        btnSaveTransaction.setOnClickListener(v -> onSaveClicked());

        ivBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        setCategory("RECEIVABLE");
        applyListeners();
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

        applyCategoryStyle(btnSale, ivSale, tvSale, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
        applyCategoryStyle(btnReceivable, ivReceivable, tvReceivable, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
        applyCategoryStyle(btnSupplier, ivSupplier, tvSupplier, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);
        applyCategoryStyle(btnExpense, ivExpense, tvExpense, false, selectedBg, unselectedBg, selectedText, unselectedText, selectedIcon, unselectedIcon);

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
        icon.setImageTintList(ColorStateList.valueOf(selected ? selectedIcon : unselectedIcon));
    }

    private void onSaveClicked() {
        if (isSaving) return;

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String customerName = etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr) || ".".equals(amountStr)) {
            etAmount.setError("Enter valid amount");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            etAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Amount must be > 0");
            etAmount.requestFocus();
            return;
        }

        if ("RECEIVABLE".equals(selectedCategory) && TextUtils.isEmpty(customerName)) {
            etCustomerName.setError("Customer name required");
            etCustomerName.requestFocus();
            return;
        }

        isSaving = true;
        btnSaveTransaction.setEnabled(false);

        String uid = currentUser.getUid();

        Map<String, Object> tx = new HashMap<>();
        tx.put("amount", amount);
        tx.put("description", desc);
        tx.put("category", selectedCategory);
        tx.put("customerName", customerName);
        tx.put("customerPhone", customerPhone);
        tx.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .add(tx)
                .addOnSuccessListener(documentReference -> {
                    isSaving = false;
                    btnSaveTransaction.setEnabled(true);

                    Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show();
                    clearForm();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    isSaving = false;
                    btnSaveTransaction.setEnabled(true);

                    Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearForm() {
        etAmount.setText("");
        etDescription.setText("");
        etCustomerName.setText("");
        etCustomerPhone.setText("");
        setCategory("RECEIVABLE");
    }
}