package com.ecotrack.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.R;
import com.ecotrack.app.adapters.ActivityAdapter;
import com.ecotrack.app.database.DatabaseHelper;
import com.ecotrack.app.models.EcoActivity;

import java.util.List;

/**
 * GÜNLÜK sekmesi (2. ekran görüntüsü)
 *
 * Gösterilenler:
 *  - Toplam Etki XP kartı + seviye ilerleme barı (DB'den toplam XP)
 *  - Seri Devam Ediyor koyu yeşil kart
 *  - Son Aktiviteler listesi (RecyclerView, DB'den okunur)
 *  - Haftalık Hedef kartı
 */
public class GunlukFragment extends Fragment {

    private DatabaseHelper db;

    // Bir sonraki seviye için gereken toplam XP (örnek: Seviye 5 = 1500 XP)
    private static final int SONRAKI_SEVIYE_XP = 1500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gunluk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());

        // Toplam XP'yi DB'den al. Demo veriye ek olarak 1240 baz ekleyerek
        // ekran görüntüsündeki gibi 1.240 XP civarı gösterelim.
        int kazanilanXp = db.toplamXp();
        int toplamXp = 1240 + kazanilanXp;  // baz + kazanılan

        TextView xpText = view.findViewById(R.id.gunlukTotalXp);
        xpText.setText(String.format("%,d", toplamXp));

        // Seviye ilerleme barı
        ProgressBar seviyeBar = view.findViewById(R.id.gunlukLevelProgress);
        int kalanXp = Math.max(0, SONRAKI_SEVIYE_XP - toplamXp);
        int yuzde = (int) ((toplamXp / (double) SONRAKI_SEVIYE_XP) * 100);
        seviyeBar.setProgress(Math.min(100, yuzde));

        TextView seviyeText = view.findViewById(R.id.gunlukLevelText);
        seviyeText.setText(String.format(
                "Seviye 4: Doğa Dostu (Sonraki seviye için %d XP)", kalanXp));

        // Son Aktiviteler listesi
        RecyclerView aktiviteRv = view.findViewById(R.id.gunlukActivityRecycler);
        aktiviteRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        aktiviteRv.setNestedScrollingEnabled(false);
        List<EcoActivity> aktiviteler = db.tumAktiviteleriGetir();
        aktiviteRv.setAdapter(new ActivityAdapter(aktiviteler));
    }
}
