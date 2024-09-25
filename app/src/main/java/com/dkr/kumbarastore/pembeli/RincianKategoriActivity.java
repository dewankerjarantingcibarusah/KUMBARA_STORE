package com.dkr.kumbarastore.pembeli;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RincianKategoriActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RincianKategoriAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_kategori);

        recyclerView = findViewById(R.id.rvProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new RincianKategoriAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get kategori from intent
        String kategori = getIntent().getStringExtra("kategori");

        if (kategori != null) {
            loadDataFromFirestore(kategori);
        } else {
            Toast.makeText(this, "Kategori tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataFromFirestore(String kategori) {
        productList.clear();

        // Query Firestore for products based on category
        firestore.collection("items")
                .whereEqualTo("kategori", kategori)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String name = documentSnapshot.getString("imageName");
                        String price = documentSnapshot.getString("price");
                        int stock = documentSnapshot.getLong("stock").intValue();
                        int terjual = documentSnapshot.getLong("terjual").intValue();
                        String id = documentSnapshot.getId();

                        Product product = new Product(imageUrl, name, price, stock, terjual, id);
                        productList.add(product);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RincianKategoriActivity.this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
