package com.dkr.kumbarastore.penjual;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.pembeli.AkunActivity;
import com.dkr.kumbarastore.pembeli.RiwayatPesananPembeliActivity;
import com.dkr.kumbarastore.pembeli.RiwayatPesananPembeliAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RiwayatPenjualanActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RiwayatPenjualanAdapter viewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_penjualan);
        tabLayout = findViewById(R.id.tabLayout1);
        viewPager = findViewById(R.id.viewPager1);
        viewPagerAdapter = new RiwayatPenjualanAdapter(this);

        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Dikemas");
                    break;
                case 1:
                    tab.setText("Dikirim");
                    break;
                case 2:
                    tab.setText("Selesai");
                    break;
                case 3:
                    tab.setText("Dibatalkan");
                    break;
            }
        }).attach();
        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
        viewPager.setCurrentItem(selectedTab);
    }

    // Mengatur ViewPager ke tab yang diterima dari intent
    @Override
    public void onBackPressed() {
        int currentFragmentIndex = viewPager.getCurrentItem();

        // Cek jika fragment tertentu sedang aktif
        if (currentFragmentIndex == 0) {
            // Create an Intent to carry result data
            Intent resultIntent = new Intent();
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RiwayatPenjualanActivity.this, TampilanpenjualActivity.class);
            startActivity(resultIntent);
        } else if (currentFragmentIndex == 1) {
            // Create an Intent to carry result data
            Intent resultIntent = new Intent();
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RiwayatPenjualanActivity.this, TampilanpenjualActivity.class);
            startActivity(resultIntent);
        } else if (currentFragmentIndex == 2) {
            // Create an Intent to carry result data
            Intent resultIntent = new Intent();
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RiwayatPenjualanActivity.this, TampilanpenjualActivity.class);
            startActivity(resultIntent);
        } else if (currentFragmentIndex == 3) {
            // Create an Intent to carry result data
            Intent resultIntent = new Intent();
            // If coming from "dikemas", return to RincianpesanandibatalkanActivity
            resultIntent = new Intent(RiwayatPenjualanActivity.this, TampilanpenjualActivity.class);
            startActivity(resultIntent);
        }
    }

}