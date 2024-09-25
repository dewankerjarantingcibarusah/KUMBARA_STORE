package com.dkr.kumbarastore.pembeli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class KategoriActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KategoriAdapter adapter;
    private List<Kategori> kategoriList;
    private FirebaseFirestore firestore;
    private ImageView imgKeranjang;
    private static final String LOGIN_STATUS_KEY = "isLoggedIn";
    private static final String PREFS_NAME = "UserPrefs";
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategori);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerViewKategori);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        kategoriList = new ArrayList<>();
        adapter = new KategoriAdapter(this, kategoriList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        imgKeranjang = findViewById(R.id.ivCart);
        imgKeranjang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoggedIn()) {
                    Toast.makeText(KategoriActivity.this, "Silahkan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(KategoriActivity.this, KeranjangActivity.class);
                    intent.putExtra("from", "Kategori");
                    startActivity(intent);
                }
            }
        });


        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_kateogri);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_Beranda) {
                startActivity(new Intent(getApplicationContext(), BerandaActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_kateogri) {
                return true;
            } else if (item.getItemId() == R.id.bottom_akun) {
                startActivity(new Intent(getApplicationContext(), AkunActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        loadKategori();
    }
    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false);
    }
    private void loadKategori() {
        firestore.collection("kategori").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        kategoriList.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Kategori kategori = documentSnapshot.toObject(Kategori.class);
                            kategoriList.add(kategori);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
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
}
