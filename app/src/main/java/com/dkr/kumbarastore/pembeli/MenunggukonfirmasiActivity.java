package com.dkr.kumbarastore.pembeli;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dkr.kumbarastore.R;

public class MenunggukonfirmasiActivity extends AppCompatActivity {

    private ImageView imgKembali;
    private String kta;
    private String orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menunggukonfirmasi);

        if (getIntent().hasExtra("kta") && getIntent().hasExtra("orderNumber")) {
            kta = getIntent().getStringExtra("kta");
            orderNumber = getIntent().getStringExtra("orderNumber");
        }

        String from = getIntent().getStringExtra("from");
        if ("pembatalan".equals(from)) {
            navigateToPembatalan();
        }

        inisiasiMenunggu();
    }

    private void inisiasiMenunggu() {
        CardView Beranda = findViewById(R.id.cvBeranda);
        Beranda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenunggukonfirmasiActivity.this, BerandaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        CardView PesananSaya = findViewById(R.id.cvPesananSaya);
        PesananSaya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenunggukonfirmasiActivity.this, RiwayatPesananPembeliActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("kta", kta); // kta adalah data yang ingin Anda kirim
                intent.putExtra("orderNumber", orderNumber); // orderNumber adalah nomor pesanan yang telah dibuat
                startActivity(intent);
            }
        });

        imgKembali = findViewById(R.id.imgkembali);
        imgKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenunggukonfirmasiActivity.this, BerandaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, BerandaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToPembatalan() {
        Intent intent = new Intent(MenunggukonfirmasiActivity.this, FragmentRiwayatPesananPembelidibatalkanActivity.class);
        startActivity(intent);
        finish();
    }
}
