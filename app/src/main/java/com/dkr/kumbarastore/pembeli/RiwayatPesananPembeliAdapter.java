package com.dkr.kumbarastore.pembeli;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
public class RiwayatPesananPembeliAdapter extends FragmentStateAdapter {

    public RiwayatPesananPembeliAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FragmentRiwayatPesananPembelidikemasActivity();
            case 1:
                return new FragmentRiwayatPesananPembelidikirimActivity();
            case 2:
                return new FragmentRiwayatPesananPembeliselesaiActivity();
            case 3:
                return new FragmentRiwayatPesananPembelidibatalkanActivity();
            default:
                return new FragmentRiwayatPesananPembelidikemasActivity();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Number of fragments
    }
}


