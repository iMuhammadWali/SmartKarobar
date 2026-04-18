package com.example.smartkarobar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout tabGhar, tabHisaab, tabReports;
    private ImageView ivGhar, ivHisaab, ivReports;
    private TextView tvGhar, tvHisaab, tvReports;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private AlertDialog usernameDialog; // prevent duplicate dialogs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        init();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SignupActivity.class));
            finish();
            return;
        }

        if (savedInstanceState == null) {
            switchTab(new DashboardFragment(), "GHAR");
        }

        applyNavbarListeners();

        // first check
        checkAndAskUsernameIfMissing();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // check again when activity comes to foreground
        checkAndAskUsernameIfMissing();
    }

    private void init() {
        tabGhar = findViewById(R.id.tabGhar);
        tabHisaab = findViewById(R.id.tabHisaab);
        tabReports = findViewById(R.id.tabReports);

        ivGhar = findViewById(R.id.ivGhar);
        ivHisaab = findViewById(R.id.ivHisaab);
        ivReports = findViewById(R.id.ivReports);

        tvGhar = findViewById(R.id.tvGhar);
        tvHisaab = findViewById(R.id.tvHisaab);
        tvReports = findViewById(R.id.tvReports);
    }

    private void applyNavbarListeners() {
        tabGhar.setOnClickListener(v -> switchTab(new DashboardFragment(), "GHAR"));
        tabHisaab.setOnClickListener(v -> switchTab(new HisaabFragment(), "HISAAB"));
        tabReports.setOnClickListener(v -> switchTab(new ReportsFragment(), "REPORTS"));
    }

    private void resetTabs() {
        tabGhar.setBackground(null);
        tabHisaab.setBackground(null);
        tabReports.setBackground(null);

        int unselected = Color.parseColor("#2D6A4F");
        ivGhar.setColorFilter(unselected);
        ivHisaab.setColorFilter(unselected);
        ivReports.setColorFilter(unselected);

        tvGhar.setTextColor(unselected);
        tvHisaab.setTextColor(unselected);
        tvReports.setTextColor(unselected);
    }

    private void switchTab(Fragment fragment, String tab) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        resetTabs();

        switch (tab) {
            case "GHAR":
                selectTab(tabGhar, ivGhar, tvGhar);
                break;
            case "HISAAB":
                selectTab(tabHisaab, ivHisaab, tvHisaab);
                break;
            case "REPORTS":
                selectTab(tabReports, ivReports, tvReports);
                break;
        }
    }

    private void selectTab(LinearLayout tab, ImageView icon, TextView text) {
        tab.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_nav_selected));
        int selected = Color.parseColor("#D8F3DC");
        icon.setColorFilter(selected);
        text.setTextColor(selected);
    }

    private void checkAndAskUsernameIfMissing() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    // If dialog already visible, do nothing
                    if (usernameDialog != null && usernameDialog.isShowing()) return;

                    // doc missing -> ask username
                    if (!doc.exists()) {
                        showUsernameDialog(uid);
                        return;
                    }

                    // username missing/empty -> ask username
                    String username = doc.getString("username");
                    if (username == null || username.trim().isEmpty()) {
                        showUsernameDialog(uid);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not verify username: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showUsernameDialog(@NonNull String uid) {
        final EditText et = new EditText(this);
        et.setHint("Enter username");
        et.setInputType(InputType.TYPE_CLASS_TEXT);

        int p = (int) (16 * getResources().getDisplayMetrics().density);
        et.setPadding(p, p, p, p);

        usernameDialog = new AlertDialog.Builder(this)
                .setTitle("Add a username")
                .setMessage("Please add your username to continue.")
                .setView(et)
                .setCancelable(false)
                .setNegativeButton("Logout", (d, w) -> {
                    auth.signOut();
                    startActivity(new Intent(this, SignupActivity.class));
                    finish();
                })
                .setPositiveButton("Save", null)
                .create();

        usernameDialog.setOnShowListener(d -> usernameDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = et.getText().toString().trim();

                    if (name.isEmpty()) {
                        et.setError("Username is required");
                        return;
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("username", name);

                    db.collection("users")
                            .document(uid)
                            .set(map, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Username saved", Toast.LENGTH_SHORT).show();
                                if (usernameDialog != null && usernameDialog.isShowing()) {
                                    usernameDialog.dismiss();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save username: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                }));

        usernameDialog.show();
    }
}