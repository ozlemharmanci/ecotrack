package com.ecotrack.app.models;

/**
 * Aktif bir "seri" (streak) - kullanıcının üst üste sürdürdüğü bir alışkanlık.
 * Panel ekranındaki yatay kaydırılabilir "Aktif Seriler" listesinde gösterilir.
 *
 * Örnek: "ULAŞIM - 12 Gün", "BİTKİ BAZLI - 5 Gün"
 */
public class Seri {

    private String etiket;        // "ULAŞIM" gibi büyük başlık
    private int gunSayisi;        // Mevcut seri uzunluğu (gün cinsinden)
    private int hedefGun;         // İlerleme barı için hedef
    private int ikonResource;     // Drawable kaynak ID'si
    private boolean turuncuTema;  // true = turuncu/yiyecek teması, false = yeşil

    public Seri(String etiket, int gunSayisi, int hedefGun, int ikonResource, boolean turuncuTema) {
        this.etiket = etiket;
        this.gunSayisi = gunSayisi;
        this.hedefGun = hedefGun;
        this.ikonResource = ikonResource;
        this.turuncuTema = turuncuTema;
    }

    public String getEtiket() { return etiket; }
    public int getGunSayisi() { return gunSayisi; }
    public int getHedefGun() { return hedefGun; }
    public int getIkonResource() { return ikonResource; }
    public boolean isTuruncuTema() { return turuncuTema; }

    /** İlerleme yüzdesi (0-100 arası) */
    public int getProgressYuzdesi() {
        if (hedefGun <= 0) return 0;
        int p = (int) ((gunSayisi / (double) hedefGun) * 100);
        return Math.min(100, p);
    }
}
