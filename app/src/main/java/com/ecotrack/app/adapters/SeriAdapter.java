package com.ecotrack.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.R;
import com.ecotrack.app.models.Seri;

import java.util.List;

/**
 * "Aktif Seriler" yatay listesini gösteren adapter (Panel sekmesi)
 * Her kart: ikon kutusu + etiket + "X Gün" + ilerleme barı
 */
public class SeriAdapter extends RecyclerView.Adapter<SeriAdapter.VH> {

    private final List<Seri> liste;

    public SeriAdapter(List<Seri> liste) {
        this.liste = liste;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seri, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Seri s = liste.get(pos);
        h.etiket.setText(s.getEtiket());
        h.gun.setText(s.getGunSayisi() + " Gün");
        h.ikon.setImageResource(s.getIkonResource());
        h.progress.setProgress(s.getProgressYuzdesi());

        // İkon kutusu temasını seriye göre değiştir (yeşil / turuncu)
        int bgRes = s.isTuruncuTema()
                ? R.drawable.bg_icon_square_orange
                : R.drawable.bg_icon_square_green;
        h.ikonKutusu.setBackground(
                ContextCompat.getDrawable(h.itemView.getContext(), bgRes));
    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View ikonKutusu;
        ImageView ikon;
        TextView etiket, gun;
        ProgressBar progress;

        VH(@NonNull View itemView) {
            super(itemView);
            ikonKutusu = itemView.findViewById(R.id.itemSeriIconBox);
            ikon = itemView.findViewById(R.id.itemSeriIcon);
            etiket = itemView.findViewById(R.id.itemSeriLabel);
            gun = itemView.findViewById(R.id.itemSeriDays);
            progress = itemView.findViewById(R.id.itemSeriProgress);
        }
    }
}
