package com.ecotrack.app.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.ecotrack.app.database.DatabaseHelper;
import com.ecotrack.app.models.EcoActivity;

/**
 * Kullanıcı sağ alttaki + (FAB) düğmesine bastığında açılan diyalog.
 *
 * 5 aktivite tipinden birini seçer, SQLite veritabanına ekler.
 * Eklendikten sonra dinleyiciyi (listener) tetikler — Panel ve Günlük
 * sekmeleri kendilerini yenileyebilsin diye.
 */
public class AktiviteEkleDialog {

    /** Aktivite eklendiğinde bilgilendirilmek isteyen sınıflar için */
    public interface AktiviteEklendiListener {
        void aktiviteEklendi(EcoActivity yeniAktivite);
    }

    /** Diyalogu açar ve seçim sonrası DB'ye yazar */
    public static void goster(Context ctx, AktiviteEklendiListener dinleyici) {

        // Görünen isimler + emoji (kullanıcı dostu)
        final String[] secenekler = {
                "♻️  Geri Dönüşüm  (+15 XP)",
                "🚌  Toplu Taşıma  (+25 XP)",
                "🌱  Bitki Bazlı Öğün  (+30 XP)",
                "🚴  Bisiklet  (+20 XP)",
                "💡  Enerji Tasarrufu  (+10 XP)"
        };

        // DB için gerçek başlık + tip + XP + CO2 değerleri
        final String[][] meta = {
                {"Geri Dönüştürülmüş Plastikler", EcoActivity.TYPE_RECYCLE,     "15", "0.5"},
                {"Toplu Taşıma",                  EcoActivity.TYPE_TRANSPORT,   "25", "1.2"},
                {"Bitki Bazlı Öğün",              EcoActivity.TYPE_PLANT_FOOD,  "30", "0.7"},
                {"Bisiklet ile Ulaşım",           EcoActivity.TYPE_BIKE,        "20", "0.8"},
                {"Enerji Tasarrufu",              EcoActivity.TYPE_ENERGY_SAVE, "10", "0.3"}
        };

        new AlertDialog.Builder(ctx)
                .setTitle("Aktivite Ekle")
                .setItems(secenekler, (dialog, hangi) -> {
                    // Seçilen aktivite için EcoActivity nesnesi oluştur
                    EcoActivity yeni = new EcoActivity(
                            meta[hangi][0],                            // başlık
                            meta[hangi][1],                            // tip
                            Integer.parseInt(meta[hangi][2]),          // XP
                            System.currentTimeMillis(),                // şimdi
                            Double.parseDouble(meta[hangi][3])         // CO2
                    );

                    // SQLite'a kaydet
                    DatabaseHelper db = new DatabaseHelper(ctx);
                    long yeniId = db.aktiviteEkle(yeni);
                    yeni.setId(yeniId);

                    // Dinleyiciyi bilgilendir (Panel/Günlük yenilensin)
                    if (dinleyici != null) {
                        dinleyici.aktiviteEklendi(yeni);
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }
}
