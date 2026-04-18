package com.example.smartkarobar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {
    // Firebase requires 6 digits for OTP
    private final StringBuilder enteredPin = new StringBuilder();
    private String verificationId;
    private FirebaseAuth auth;

    private View dot1, dot2, dot3, dot4, dot5, dot6;
    private AppCompatButton btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private ImageButton btnBackspace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        // Obtain the verification ID from SignupActivity
        verificationId = getIntent().getStringExtra("vId");

        init();
        setupListeners();
        updatePinDots();
    }

    private void init() {
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        dot5 = findViewById(R.id.dot5);
        dot6 = findViewById(R.id.dot6);

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
        View.OnClickListener digitListener = v -> {
            AppCompatButton b = (AppCompatButton) v;
            onDigitPressed(b.getText().toString());
        };

        btn0.setOnClickListener(digitListener);
        btn1.setOnClickListener(digitListener);
        btn2.setOnClickListener(digitListener);
        btn3.setOnClickListener(digitListener);
        btn4.setOnClickListener(digitListener);
        btn5.setOnClickListener(digitListener);
        btn6.setOnClickListener(digitListener);
        btn7.setOnClickListener(digitListener);
        btn8.setOnClickListener(digitListener);
        btn9.setOnClickListener(digitListener);

        btnBackspace.setOnClickListener(v -> {
            int len = enteredPin.length();
            if (len > 0) {
                enteredPin.deleteCharAt(len - 1);
                updatePinDots();
            }
        });
    }

    private void onDigitPressed(String digit) {
        if (enteredPin.length() < 6) {
            enteredPin.append(digit);
            updatePinDots();

            if (enteredPin.length() == 6) {
                verifyOtpWithFirebase(enteredPin.toString());
            }
        }
    }

    private void verifyOtpWithFirebase(String otpCode) {
        // Create the credential using the ID from SignupActivity and the user's input
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
//                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Reset dots and show error if OTP is wrong
                        enteredPin.setLength(0);
                        updatePinDots();
                        Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePinDots() {
        setDotState(dot1, enteredPin.length() >= 1);
        setDotState(dot2, enteredPin.length() >= 2);
        setDotState(dot3, enteredPin.length() >= 3);
        setDotState(dot4, enteredPin.length() >= 4);
        setDotState(dot5, enteredPin.length() >= 5);
        setDotState(dot6, enteredPin.length() >= 6);
    }

    private void setDotState(View dot, boolean filled) {
        if (dot == null) return;
        dot.setBackgroundResource(filled ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot_empty);
    }
}