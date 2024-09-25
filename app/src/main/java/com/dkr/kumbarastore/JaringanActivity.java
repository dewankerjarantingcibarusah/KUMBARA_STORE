package com.dkr.kumbarastore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class JaringanActivity extends AppCompatActivity {

    private static final String TAG = "JaringanActivity";
    private Button CobaLagi;
    private ProgressBar progressBar;
    private MyApp MyApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jaringan);

        CobaLagi = findViewById(R.id.btnCobaLagi);

        MyApp = (MyApp) getApplicationContext();

        // Hentikan pengecekan otomatis saat JaringanActivity dimulai
        MyApp.stopNetworkChecks();
        CobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNetworkStatus();
            }
        });

    }

    private void checkNetworkStatus() {
        NetworkUtils.checkNetworkQuality(isPingSuccessful -> {
            if (isPingSuccessful) {
                finish();
            } else {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApp.startNetworkChecks(); // Start network check when leaving JaringanActivity
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApp.stopNetworkChecks(); // Stop network check when entering JaringanActivity
    }
    @Override
    public void onBackPressed() {
        // Jangan panggil super.onBackPressed() untuk menonaktifkan tombol back
    }
}
