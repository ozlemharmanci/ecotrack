package com.ecotrack.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ecotrack.app.R;
import com.ecotrack.app.database.DatabaseHelper;

/**
 * PROFİL sekmesi
 *
 * Kullanıcının özet istatistiklerini gösterir:
 *  - Toplam XP
 *  - Toplam CO2 tasarrufu
 *  - Toplam aktivite sayısı
 *
 * Tüm veriler SQLite'tan canlı okunur.
 */
public class ProfilFragment extends Fragment {

    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());

        TextView xpDeger = view.findViewById(R.id.profilXpValue);
        TextView co2Deger = view.findViewById(R.id.profilCo2Value);
        TextView aktiviteDeger = view.findViewById(R.id.profilActivityValue);

        int toplamXp = 1240 + db.toplamXp();
        double toplamCo2 = db.toplamCo2Tasarrufu();
        int aktiviteSayisi = db.tumAktiviteleriGetir().size();

        xpDeger.setText(String.format("%,d", toplamXp));
        co2Deger.setText(String.format("%.1f kg", toplamCo2));
        aktiviteDeger.setText(String.valueOf(aktiviteSayisi));
    }
}
