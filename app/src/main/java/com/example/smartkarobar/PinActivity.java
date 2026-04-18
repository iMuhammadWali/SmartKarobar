package com.example.smartkarobar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// TODO: Use Firebase Auth and get user details on PIN (Or see what to do)
public class PinActivity extends AppCompatActivity {
    private final StringBuilder enteredPin = new StringBuilder();
    private View dot1, dot2, dot3, dot4;
    private AppCompatButton btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private ImageButton btnBackspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setupListeners();
        updatePinDots();
    }
    private void init() {
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);

        btnBackspace = findViewById(R.id.btnBackspace);
    }
    private void setupListeners() {
        btn0.setOnClickListener(v -> onDigitPressed("0"));
        btn1.setOnClickListener(v -> onDigitPressed("1"));
        btn2.setOnClickListener(v -> onDigitPressed("2"));
        btn3.setOnClickListener(v -> onDigitPressed("3"));
        btn4.setOnClickListener(v -> onDigitPressed("4"));
        btn5.setOnClickListener(v -> onDigitPressed("5"));
        btn6.setOnClickListener(v -> onDigitPressed("6"));
        btn7.setOnClickListener(v -> onDigitPressed("7"));
        btn8.setOnClickListener(v -> onDigitPressed("8"));
        btn9.setOnClickListener(v -> onDigitPressed("9"));

        btnBackspace.setOnClickListener(v -> {
            int len = enteredPin.length();
            if (len > 0) {
                enteredPin.deleteCharAt(len - 1);
                updatePinDots();
            }
        });
    }
    private void onDigitPressed(String digit) {
        enteredPin.append(digit);
        updatePinDots();

        if (enteredPin.length() == 4) {
            checkPinAndProceed();
        }
    }
    private void checkPinAndProceed() {
        if ("1234".contentEquals(enteredPin)) {
            Intent intent = new Intent(PinActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            enteredPin.setLength(0);
            updatePinDots();
            Toast.makeText(this, "You entered wrong pin", Toast.LENGTH_SHORT).show();
        }
    }
    private void updatePinDots() {
        setDotState(dot1, enteredPin.length() >= 1);
        setDotState(dot2, enteredPin.length() >= 2);
        setDotState(dot3, enteredPin.length() >= 3);
        setDotState(dot4, enteredPin.length() >= 4);
    }

    private void setDotState(View dot, boolean filled) {
        if (dot == null) return;
        dot.setBackgroundResource(filled ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot_empty);
    }
}