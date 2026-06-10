package com.ecotrack.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ecotrack.app.models.EcoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite veritabanı yardımcısı.
 *
 * Neden SQLite?
 *  - Android'e yerleşik gelir, ek bağımlılık yok
 *  - Tek bir dosyada veri tutar (uygulama veri dizininde)
 *  - Internet bağlantısı gerektirmez
 *  - Firebase'den çok daha basit
 *
 * Bu sınıf tek tablo yönetir: activities (aktiviteler).
 * Profil, ayarlar gibi tekil veriler SharedPreferences ile tutulur.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ecotrack.db";
    private static final int DB_VERSION = 1;

    // Aktiviteler tablosu
    private static final String TABLE_ACTIVITIES = "activities";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_TYPE = "type";
    private static final String COL_XP = "xp_reward";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_CO2 = "co2_saved";

    private static final String CREATE_TABLE_ACTIVITIES =
            "CREATE TABLE " + TABLE_ACTIVITIES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT NOT NULL, " +
                    COL_TYPE + " TEXT NOT NULL, " +
                    COL_XP + " INTEGER DEFAULT 0, " +
                    COL_TIMESTAMP + " INTEGER NOT NULL, " +
                    COL_CO2 + " REAL DEFAULT 0" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACTIVITIES);
        // İlk açılışta birkaç demo veri ekleyelim ki ekran boş gözükmesin
        seedDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Basit migration stratejisi: tabloyu silip yeniden oluştur.
        // Gerçek üretim ortamında ALTER TABLE kullanılır.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(db);
    }

    /** Yeni bir aktiviteyi DB'ye ekler ve eklenen satırın ID'sini döner */
    public long aktiviteEkle(EcoActivity a) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, a.getTitle());
        cv.put(COL_TYPE, a.getType());
        cv.put(COL_XP, a.getXpReward());
        cv.put(COL_TIMESTAMP, a.getTimestamp());
        cv.put(COL_CO2, a.getCo2Saved());
        long id = db.insert(TABLE_ACTIVITIES, null, cv);
        db.close();
        return id;
    }

    /** Tüm aktiviteleri zaman olarak yeniden eskiye göre döner */
    public List<EcoActivity> tumAktiviteleriGetir() {
        List<EcoActivity> liste = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ACTIVITIES, null, null, null, null, null,
                COL_TIMESTAMP + " DESC");

        if (c.moveToFirst()) {
            do {
                EcoActivity a = new EcoActivity();
                a.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
                a.setTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
                a.setType(c.getString(c.getColumnIndexOrThrow(COL_TYPE)));
                a.setXpReward(c.getInt(c.getColumnIndexOrThrow(COL_XP)));
                a.setTimestamp(c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP)));
                a.setCo2Saved(c.getDouble(c.getColumnIndexOrThrow(COL_CO2)));
                liste.add(a);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return liste;
    }

    /** Toplam XP'yi hesaplar (tüm aktivitelerin XP'lerinin toplamı) */
    public int toplamXp() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_XP + ") FROM " + TABLE_ACTIVITIES, null);
        int toplam = 0;
        if (c.moveToFirst()) {
            toplam = c.getInt(0);
        }
        c.close();
        db.close();
        return toplam;
    }

    /** Tasarruf edilen toplam CO2 (kg) */
    public double toplamCo2Tasarrufu() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_CO2 + ") FROM " + TABLE_ACTIVITIES, null);
        double toplam = 0;
        if (c.moveToFirst()) {
            toplam = c.getDouble(0);
        }
        c.close();
        db.close();
        return toplam;
    }

    /** Bir aktiviteyi sil */
    public void aktiviteSil(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ACTIVITIES, COL_ID + "=?", new String[]{ String.valueOf(id) });
        db.close();
    }

    /**
     * İlk açılışta demo veri ekler.
     * Üretim uygulamasında kaldırılmalı veya bir flag arkasına alınmalı.
     */
    private void seedDemoData(SQLiteDatabase db) {
        long simdi = System.currentTimeMillis();
        long birGun = 24L * 60 * 60 * 1000;

        insertSeed(db, "Geri Dönüştürülmüş Plastikler", EcoActivity.TYPE_RECYCLE, 15, simdi - 2 * 3600_000L, 0.5);
        insertSeed(db, "Toplu Taşıma", EcoActivity.TYPE_TRANSPORT, 25, simdi - birGun, 1.2);
        insertSeed(db, "Bitki Bazlı Öğün", EcoActivity.TYPE_PLANT_FOOD, 30, simdi - birGun - 18000_000L, 0.7);
    }

    private void insertSeed(SQLiteDatabase db, String title, String type, int xp, long ts, double co2) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_TYPE, type);
        cv.put(COL_XP, xp);
        cv.put(COL_TIMESTAMP, ts);
        cv.put(COL_CO2, co2);
        db.insert(TABLE_ACTIVITIES, null, cv);
    }
}
