package com.ecotrack.app.models;

/**
 * Kullanıcının kaydettiği bir sürdürülebilir aktiviteyi temsil eder.
 * Örnek: Geri Dönüşüm, Toplu Taşıma, Bitki Bazlı Öğün vb.
 *
 * Bu sınıf SQLite veritabanında "activities" tablosuna karşılık gelir.
 */
public class EcoActivity {

    // Aktivite tipleri - DB'de string olarak tutulur
    public static final String TYPE_RECYCLE = "RECYCLE";        // Geri Dönüşüm
    public static final String TYPE_TRANSPORT = "TRANSPORT";    // Toplu Taşıma
    public static final String TYPE_PLANT_FOOD = "PLANT_FOOD";  // Bitki Bazlı Öğün
    public static final String TYPE_BIKE = "BIKE";              // Bisiklet
    public static final String TYPE_ENERGY_SAVE = "ENERGY_SAVE";// Enerji Tasarrufu

    private long id;
    private String title;       // Görünen isim: "Geri Dönüştürülmüş Plastikler"
    private String type;        // Tip sabitlerinden biri
    private int xpReward;       // Kazanılan XP (+15, +25, +30 gibi)
    private long timestamp;     // Aktivitenin gerçekleştiği zaman (millis)
    private double co2Saved;    // Tasarruf edilen CO2 (kg)

    public EcoActivity() {
        // Boş constructor SQLite için gerekli
    }

    public EcoActivity(String title, String type, int xpReward, long timestamp, double co2Saved) {
        this.title = title;
        this.type = type;
        this.xpReward = xpReward;
        this.timestamp = timestamp;
        this.co2Saved = co2Saved;
    }

    // --- Getter ve Setter'lar ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getCo2Saved() { return co2Saved; }
    public void setCo2Saved(double co2Saved) { this.co2Saved = co2Saved; }

    /**
     * Bu aktivite tipine ait drawable ikon kaynak ID'sini döner.
     * Adapter'da arka plan ikonu seçmek için kullanılır.
     */
    public int getIconResource() {
        switch (type) {
            case TYPE_RECYCLE:     return com.ecotrack.app.R.drawable.ic_recycle;
            case TYPE_TRANSPORT:   return com.ecotrack.app.R.drawable.ic_bus;
            case TYPE_PLANT_FOOD:  return com.ecotrack.app.R.drawable.ic_leaf;
            case TYPE_BIKE:        return com.ecotrack.app.R.drawable.ic_bike;
            default:               return com.ecotrack.app.R.drawable.ic_leaf;
        }
    }
}
