package com.dkr.kumbarastore.penjual;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkr.kumbarastore.R;
import com.dkr.kumbarastore.pembeli.Order;
import com.dkr.kumbarastore.pembeli.Product;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FragmentRiwayatPenjualanDikemas extends Fragment {

    private RecyclerView recyclerView;
    private FragmentRiwayatPenjualanDikemasAdapter adapter;
    private List<Penjualan> penjualanList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riwayat_penjualan_dikemas, container, false);

        recyclerView = view.findViewById(R.id.RecyclerViewPenjualanDikemas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        penjualanList = new ArrayList<>();
        adapter = new FragmentRiwayatPenjualanDikemasAdapter(getContext(), penjualanList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        loadKtasAndOrdersCount();

        return view;
    }

    private void loadKtasAndOrdersCount() {
        List<String> ktaList = new ArrayList<>();

        // Langkah 1: Ambil semua KTA dari dokumen users dengan posisi "pembeli"
        firestore.collection("users")
                .whereEqualTo("posisi", "pembeli")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String kta = document.getString("kta");
                            if (kta != null) {
                                ktaList.add(kta);
                            }
                        }

                        // Setelah mendapatkan semua KTA, lanjutkan untuk mencari order number di "dikemas"
                        for (String kta : ktaList) {
                            loadOrderNumbersFromKTA(kta);
                        }
                    } else {
                        Toast.makeText(getContext(), "Gagal memuat data KTA", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal mengambil data KTA", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadOrderNumbersFromKTA(String kta) {
        firestore.collection("orders")
                .document("dikemas")
                .collection(kta)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String orderNumber = document.getId();
                        String namaPembeli = document.getString("namaPembeli");
                        String nomorpembeli = document.getString("nomorPembeli");
                        String alamatPembeli = document.getString("alamatPembeli");
                        String waktu = document.getString("orderTime");
                        String deliveryTime = document.getString("deliveryTime");
                        String status = document.getString("status");

                        loadProductsFromOrder(kta, orderNumber, namaPembeli,nomorpembeli,alamatPembeli,waktu,deliveryTime,status);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat nomor pesanan", Toast.LENGTH_SHORT).show();
                });
    }
    private String formatCurrency(int amount) {
        return String.format("Rp%,d", amount).replace(',', '.');
    }

    private void loadProductsFromOrder(String kta, String orderNumber, String namaPembeli,String nomorpembeli, String alamatPembeli, String waktu,String deliveryTime, String status)  {
        firestore.collection("orders")
                .document("dikemas")
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
                    Penjualan penjualan = new Penjualan(orderNumber, productList, kta, formatCurrency(totalPrice), namaPembeli,nomorpembeli,alamatPembeli,waktu,deliveryTime,status);
                    penjualanList.add(penjualan);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                });
    }

}
