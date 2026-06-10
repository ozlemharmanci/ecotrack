package com.ecotrack.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.R;
import com.ecotrack.app.models.EcoActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * "Son Aktiviteler" listesini gösteren adapter (Günlük sekmesi)
 * Her satır: ikon + başlık + zaman + XP rozeti
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.VH> {

    private final List<EcoActivity> liste;

    public ActivityAdapter(List<EcoActivity> liste) {
        this.liste = liste;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        EcoActivity a = liste.get(pos);
        h.baslik.setText(a.getTitle());
        h.zaman.setText(zamanFormatla(a.getTimestamp()));
        h.xp.setText("+" + a.getXpReward() + " XP");
        h.ikon.setImageResource(a.getIconResource());
    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    /** Timestamp'i "Bugün, 09:15" / "Dün, 18:30" formatına çevirir */
    private String zamanFormatla(long ts) {
        long simdi = System.currentTimeMillis();
        long fark = simdi - ts;
        long birGun = 24L * 60 * 60 * 1000;

        SimpleDateFormat saatFmt = new SimpleDateFormat("HH:mm", new Locale("tr", "TR"));
        String saat = saatFmt.format(new Date(ts));

        if (fark < birGun && ayniGun(ts, simdi)) {
            return "Bugün, " + saat;
        } else if (fark < 2 * birGun) {
            return "Dün, " + saat;
        } else {
            SimpleDateFormat tarihFmt = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR"));
            return tarihFmt.format(new Date(ts));
        }
    }

    private boolean ayniGun(long t1, long t2) {
        SimpleDateFormat gunFmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return gunFmt.format(new Date(t1)).equals(gunFmt.format(new Date(t2)));
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ikon;
        TextView baslik, zaman, xp;

        VH(@NonNull View itemView) {
            super(itemView);
            ikon = itemView.findViewById(R.id.itemActivityIcon);
            baslik = itemView.findViewById(R.id.itemActivityTitle);
            zaman = itemView.findViewById(R.id.itemActivityTime);
            xp = itemView.findViewById(R.id.itemActivityXp);
        }
    }
}
