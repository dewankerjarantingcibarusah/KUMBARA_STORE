package com.dkr.kumbarastore.penjual;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dkr.kumbarastore.pembeli.FragmentRiwayatPesananPembelidibatalkanActivity;
import com.dkr.kumbarastore.pembeli.FragmentRiwayatPesananPembelidikemasActivity;
import com.dkr.kumbarastore.pembeli.FragmentRiwayatPesananPembelidikirimActivity;
import com.dkr.kumbarastore.pembeli.FragmentRiwayatPesananPembeliselesaiActivity;

public class RiwayatPenjualanAdapter extends FragmentStateAdapter {

    public RiwayatPenjualanAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FragmentRiwayatPenjualanDikemas();
            case 1:
                return new FragmentRiwayatPenjualanDikemas();
            case 2:
                return new FragmentRiwayatPenjualanDikemas();
            case 3:
                return new FragmentRiwayatPenjualanDikemas();
            default:
                return new FragmentRiwayatPenjualanDikemas();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Number of fragments
    }
}
