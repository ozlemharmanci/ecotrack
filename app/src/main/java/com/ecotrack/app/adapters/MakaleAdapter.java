package com.ecotrack.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecotrack.app.R;
import com.ecotrack.app.models.Makale;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Son Paylaşılanlar" makale listesini gösteren adapter (Keşfet sekmesi)
 * Her kart: görsel + etiket + başlık + açıklama + tarih + yer imi ikonu
 *
 * Karta tıklanınca makale URL'si tarayıcıda açılır
 * Yer imi ikonuna tıklanınca makale işaretlenir/işareti kaldırılır
 * (SharedPreferences'ta saklanır, uygulama kapansa bile kalır)
 */
public class MakaleAdapter extends RecyclerView.Adapter<MakaleAdapter.VH> {

    private static final String PREFS_NAME = "ecotrack_prefs";
    private static final String KEY_BOOKMARKS = "kayitli_makaleler";

    private final List<Makale> liste;

    public MakaleAdapter(List<Makale> liste) {
        this.liste = liste;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_makale, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Makale m = liste.get(pos);
        Context ctx = h.itemView.getContext();

        h.etiket.setText(m.getEtiket());
        h.baslik.setText(m.getBaslik());
        h.aciklama.setText(m.getAciklama());
        h.tarih.setText(tarihFormatla(m.getYayinTarihi()));

        // Görseli Glide ile yükle; yoksa placeholder göster
        if (!TextUtils.isEmpty(m.getResimUrl())) {
            Glide.with(ctx)
                    .load(m.getResimUrl())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(h.gorsel);
        } else {
            h.gorsel.setImageResource(R.drawable.bg_image_placeholder);
        }

        // Yer imi durumunu güncel SharedPreferences'tan oku ve göster
        bookmarkIkonGuncelle(ctx, h.yerImi, m);

        // Yer imi ikonuna tıklama - işaretle/işareti kaldır
        h.yerImi.setOnClickListener(v -> {
            bookmarkToggle(ctx, m);
            bookmarkIkonGuncelle(ctx, h.yerImi, m);
            boolean kayitli = bookmarkKayitliMi(ctx, m);
            Toast.makeText(ctx,
                    kayitli ? "Yer imlerine eklendi" : "Yer imlerinden çıkarıldı",
                    Toast.LENGTH_SHORT).show();
        });

        // Karta tıklanınca makaleyi tarayıcıda aç
        h.itemView.setOnClickListener(v -> {
            String url = m.getMakaleUrl();
            if (!TextUtils.isEmpty(url)) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    ctx.startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(ctx, "Tarayıcı açılamadı", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ctx, "Bu demo makale için bağlantı yok",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    // --- Yer imi yönetimi (SharedPreferences) ---

    private static String bookmarkAnahtari(Makale m) {
        // Her makale için benzersiz anahtar; URL varsa onu, yoksa başlığı kullan
        return !TextUtils.isEmpty(m.getMakaleUrl()) ? m.getMakaleUrl() : m.getBaslik();
    }

    private static boolean bookmarkKayitliMi(Context ctx, Makale m) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> kayitlilar = p.getStringSet(KEY_BOOKMARKS, new HashSet<>());
        return kayitlilar.contains(bookmarkAnahtari(m));
    }

    private static void bookmarkToggle(Context ctx, Makale m) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // getStringSet'ten dönen set'i doğrudan değiştirme - kopyalayıp kaydet
        Set<String> kayitlilar = new HashSet<>(p.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        String anahtar = bookmarkAnahtari(m);
        if (kayitlilar.contains(anahtar)) {
            kayitlilar.remove(anahtar);
        } else {
            kayitlilar.add(anahtar);
        }
        p.edit().putStringSet(KEY_BOOKMARKS, kayitlilar).apply();
    }

    private static void bookmarkIkonGuncelle(Context ctx, ImageView ikon, Makale m) {
        if (bookmarkKayitliMi(ctx, m)) {
            // Kayıtlı - koyu yeşil
            ikon.setColorFilter(ContextCompat.getColor(ctx, R.color.green_primary_dark));
        } else {
            // Kayıtlı değil - gri
            ikon.setColorFilter(ContextCompat.getColor(ctx, R.color.text_secondary));
        }
    }

    /** ISO 8601 tarihini "12 EKİM 2023" gibi okunabilir hale getirir */
    private String tarihFormatla(String iso) {
        if (TextUtils.isEmpty(iso)) return "";
        try {
            String[] parcalar = iso.split("T")[0].split("-");
            String[] aylar = {"OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN",
                    "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK"};
            int ay = Integer.parseInt(parcalar[1]);
            return parcalar[2] + " " + aylar[ay - 1] + " " + parcalar[0];
        } catch (Exception e) {
            return iso;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView gorsel, yerImi;
        TextView etiket, baslik, aciklama, tarih;

        VH(@NonNull View itemView) {
            super(itemView);
            gorsel = itemView.findViewById(R.id.itemMakaleImage);
            yerImi = itemView.findViewById(R.id.itemMakaleBookmark);
            etiket = itemView.findViewById(R.id.itemMakaleTag);
            baslik = itemView.findViewById(R.id.itemMakaleTitle);
            aciklama = itemView.findViewById(R.id.itemMakaleDesc);
            tarih = itemView.findViewById(R.id.itemMakaleDate);
        }
    }
}
