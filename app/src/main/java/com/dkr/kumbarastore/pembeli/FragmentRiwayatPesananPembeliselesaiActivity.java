package com.dkr.kumbarastore.pembeli;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dkr.kumbarastore.R;

public class FragmentRiwayatPesananPembeliselesaiActivity extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riwayat_pesanan_pembeliselesai_activity, container, false);
    }
}