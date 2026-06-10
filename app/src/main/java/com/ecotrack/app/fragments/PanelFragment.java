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
import com.ecotrack.app.adapters.SeriAdapter;
import com.ecotrack.app.database.DatabaseHelper;
import com.ecotrack.app.models.Seri;

import java.util.ArrayList;
import java.util.List;

/**
 * PANEL sekmesi (1. ekran görüntüsü).
 *
 * Gösterilenler:
 *  - Günlük limit dairesel ilerleme (%70)
 *  - CO2 tasarruf kartı (DB'den toplam CO2 hesaplanır)
 *  - Aktif Seriler yatay listesi (RecyclerView)
 *  - Sürdürülebilir Yolculuk koyu yeşil kart
 *  - Günün ipucu + Topluluk kartları
 */
public class PanelFragment extends Fragment {

    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());

        // Dairesel günlük limit ilerlemesi
        ProgressBar dairesel = view.findViewById(R.id.panelCircularProgress);
        dairesel.setProgress(70);

        // CO2 tasarruf metnini DB toplamından üret
        TextView co2Baslik = view.findViewById(R.id.panelCo2Title);
        double toplamCo2 = db.toplamCo2Tasarrufu();
        if (toplamCo2 > 0) {
            co2Baslik.setText(String.format("%.1fkg CO2 tasarrufu sağladınız", toplamCo2));
        }

        // Aktif Seriler yatay RecyclerView
        RecyclerView seriRv = view.findViewById(R.id.panelSeriRecycler);
        seriRv.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        seriRv.setAdapter(new SeriAdapter(seriListesiOlustur()));
    }

    /** Demo seri verisi - gerçek uygulamada DB'den hesaplanır */
    private List<Seri> seriListesiOlustur() {
        List<Seri> liste = new ArrayList<>();
        liste.add(new Seri("ULAŞIM", 12, 14, R.drawable.ic_bike, false));
        liste.add(new Seri("BİTKİ BAZLI", 5, 7, R.drawable.ic_food, true));
        liste.add(new Seri("GERİ DÖNÜŞÜM", 8, 10, R.drawable.ic_recycle, false));
        return liste;
    }
}
