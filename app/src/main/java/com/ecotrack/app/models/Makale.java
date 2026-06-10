package com.ecotrack.app.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Keşfet sekmesinde gösterilen bir haber/makale.
 *
 * Hem yerel verileri (SQLite veya hardcoded) hem de NewsAPI.org'dan gelen
 * uzak veriyi temsil eder. NewsAPI.org'da alan adları snake_case veya
 * camelCase olabildiği için @SerializedName ile eşleştiriyoruz.
 */
public class Makale {

    // Kategori sabitleri - filtreleme için kullanılır
    public static final String KAT_HEPSI = "HEPSI";
    public static final String KAT_TEKNOLOJI = "TEKNOLOJI";
    public static final String KAT_YASAM = "YASAM";
    public static final String KAT_ENERJI = "ENERJI";

    @SerializedName("title")
    private String baslik;

    @SerializedName("description")
    private String aciklama;

    @SerializedName("urlToImage")
    private String resimUrl;

    @SerializedName("url")
    private String makaleUrl;

    @SerializedName("publishedAt")
    private String yayinTarihi;

    @SerializedName("source")
    private Kaynak kaynak;

    // Etiket: GREENTECH DAILY, ECOLIFE MAG, ENERGY TRENDS gibi
    private String etiket;

    // Kategori: TEKNOLOJI, YASAM, ENERJI - filtreleme için
    private String kategori;

    public Makale() {}

    public Makale(String etiket, String baslik, String aciklama, String resimUrl, String yayinTarihi) {
        this.etiket = etiket;
        this.baslik = baslik;
        this.aciklama = aciklama;
        this.resimUrl = resimUrl;
        this.yayinTarihi = yayinTarihi;
        // Demo veriler için otomatik kategori tahmini
        this.kategori = kategoriTahminEt(baslik, aciklama, etiket);
    }

    public String getBaslik() { return baslik; }
    public String getAciklama() { return aciklama; }
    public String getResimUrl() { return resimUrl; }
    public String getMakaleUrl() { return makaleUrl; }
    public String getYayinTarihi() { return yayinTarihi; }

    public String getEtiket() {
        // Eğer manuel etiket varsa onu, yoksa kaynak adını kullan
        if (etiket != null) return etiket;
        return kaynak != null ? kaynak.name.toUpperCase() : "HABER";
    }

    public void setEtiket(String etiket) { this.etiket = etiket; }

    public String getKategori() {
        if (kategori == null) {
            kategori = kategoriTahminEt(baslik, aciklama, getEtiket());
        }
        return kategori;
    }

    public void setKategori(String kategori) { this.kategori = kategori; }

    /**
     * API'den gelen makaleler için kategoriyi başlık/açıklama içeriğine
     * bakarak tahmin eder. Aşağıdaki anahtar kelimeler bulunursa o kategoriye atar.
     */
    private static String kategoriTahminEt(String baslik, String aciklama, String etiket) {
        String hepsi = ((baslik != null ? baslik : "") + " " +
                       (aciklama != null ? aciklama : "") + " " +
                       (etiket != null ? etiket : "")).toLowerCase();

        // Enerji anahtar kelimeleri
        if (icerirMi(hepsi, "enerji", "energy", "rüzgar", "wind", "güneş", "solar",
                "elektrik", "electricity", "battery", "pil")) {
            return KAT_ENERJI;
        }

        // Teknoloji anahtar kelimeleri
        if (icerirMi(hepsi, "teknoloji", "technology", "tech", "yapay zeka", "ai",
                "innovation", "smart", "akıllı", "greentech")) {
            return KAT_TEKNOLOJI;
        }

        // Yaşam (geri kalan her şey)
        return KAT_YASAM;
    }

    private static boolean icerirMi(String metin, String... anahtarlar) {
        if (TextUtils.isEmpty(metin)) return false;
        for (String a : anahtarlar) {
            if (metin.contains(a)) return true;
        }
        return false;
    }

    /** NewsAPI'nin iç içe "source" nesnesi için */
    public static class Kaynak {
        @SerializedName("name")
        public String name;
    }
}
