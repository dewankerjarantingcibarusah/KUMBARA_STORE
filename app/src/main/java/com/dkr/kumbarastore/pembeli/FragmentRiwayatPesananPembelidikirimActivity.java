package com.dkr.kumbarastore.pembeli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FragmentRiwayatPesananPembelidikirimActivity extends Fragment {

    private RecyclerView recyclerView;
    private RiwayatPesanandikirimAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riwayat_pesanan_pembelidikirim_activity, container, false);

        recyclerView = view.findViewById(R.id.RecyclerViewDikirim);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        orderAdapter = new RiwayatPesanandikirimAdapter(getContext(), orderList);
        recyclerView.setAdapter(orderAdapter);


        // Gunakan getActivity() untuk mendapatkan SharedPreferences di Fragment
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        String kta = sharedPreferences.getString("kta", "");

        if (!kta.isEmpty()) {
            loadOrdersFromFirestore(kta);
        } else {
        }

        return view;
    }
    private void loadOrdersFromFirestore(String kta) {
        firestore = FirebaseFirestore.getInstance(); // Inisialisasi Firestore

        firestore.collection("orders")
                .document("dikirim")
                .collection(kta)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String orderNumber = document.getId();

                        // Panggil method untuk mengambil produk berdasarkan orderNumber
                        loadProductsFromOrder(kta,orderNumber);
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private String formatCurrency(int amount) {
        return String.format("Rp%,d", amount).replace(',', '.');
    }

    private void loadProductsFromOrder(String kta, String orderNumber) {
        firestore.collection("orders")
                .document("dikirim")
                .collection(kta)
                .document(orderNumber)
                .collection("produk_pembelian")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> productList = new ArrayList<>();
                    int totalPrice = 0; // Variabel untuk menyimpan total harga

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String imageUrl = document.getString("imageUrl");
                        String name = document.getString("name");
                        String price = document.getString("price");
                        long quantity = document.getLong("quantity");

                        // Konversi harga ke int dan kalikan dengan kuantitas
                        int priceInt = Integer.parseInt(price);
                        int quantityInt = (int) quantity;
                        totalPrice += priceInt * quantityInt;

                        // Buat objek Product dan tambahkan ke daftar
                        Product product = new Product(imageUrl, name, price, quantityInt);
                        productList.add(product);
                    }

                    // Buat objek Order dengan daftar produk dan total harga
                    Order order = new Order(orderNumber, productList, kta, formatCurrency(totalPrice));
                    orderList.add(order);
                    orderAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                });
    }
}