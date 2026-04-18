package com.example.smartkarobar;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private LinearLayout tabGhar, tabHisaab, tabReports;
    private ImageView ivGhar, ivHisaab, ivReports;
    private TextView tvGhar, tvHisaab, tvReports;

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
        init();
        // default fragment
        if (savedInstanceState == null) {
            switchTab(new DashboardFragment(), "GHAR");
        }
        applyNavbarListeners();
    }
    private void init(){
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
    private void applyNavbarListeners(){
        tabHisaab.setOnClickListener((v)->{
            switchTab(new HisaabFragment(), "HISAAB");
        });
        tabGhar.setOnClickListener((v)->{
            switchTab(new DashboardFragment(), "GHAR");
        });
        tabReports.setOnClickListener((v)->{
            switchTab(new ReportsFragment(), "REPORTS");
        });
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
}