package com.ecotrack.app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotrack.app.R;
import com.ecotrack.app.adapters.MakaleAdapter;
import com.ecotrack.app.api.ApiClient;
import com.ecotrack.app.api.NewsApiResponse;
import com.ecotrack.app.models.Makale;
import com.ecotrack.app.utils.AgBaglantisi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * KEŞFET sekmesi (3. ekran görüntüsü) - tam interaktif çalışır.
 *
 * Özellikler:
 *  - NewsAPI.org'dan canlı haber çeker (yoksa yerel demo)
 *  - 🔍 Arama kutusu: yazdıkça gerçek zamanlı filtre
 *  - 🏷️  Kategori chipleri: HEPSİ / TEKNOLOJİ / YAŞAM / ENERJİ
 *  - ✉️ Abone Ol: e-posta doğrulama + SharedPreferences'a kayıt
 *  - 🔄 Daha Fazla Yükle: ek demo makaleler ekler
 *
 * İki liste tutarız:
 *   - tumMakaleler:  API'den / yerelden gelen TÜM makaleler (kaynak gerçeği)
 *   - gosterilen:    O an arama/kategori filtresinden geçenler (adapter'da)
 */
public class KesfetFragment extends Fragment {

    private static final String TAG = "KesfetFragment";
    private static final String PREFS_NAME = "ecotrack_prefs";
    private static final String KEY_EPOSTA = "abone_eposta";

    private RecyclerView makaleRv;
    private ProgressBar yuklemeBar;
    private TextView sonucYokText;
    private EditText aramaKutusu;
    private TextView chipHepsi, chipTeknoloji, chipYasam, chipEnerji;

    // Kaynak veri (tüm makaleler) ve filtreli görünür liste
    private final List<Makale> tumMakaleler = new ArrayList<>();
    private final List<Makale> gosterilen = new ArrayList<>();
    private MakaleAdapter adapter;

    // Mevcut filtre durumu
    private String aktifKategori = Makale.KAT_HEPSI;
    private String aramaMetni = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kesfet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View'ları bul
        yuklemeBar   = view.findViewById(R.id.kesfetLoading);
        makaleRv     = view.findViewById(R.id.kesfetRecycler);
        sonucYokText = view.findViewById(R.id.kesfetSonucYok);
        aramaKutusu  = view.findViewById(R.id.kesfetSearch);
        chipHepsi     = view.findViewById(R.id.kesfetChipHepsi);
        chipTeknoloji = view.findViewById(R.id.kesfetChipTeknoloji);
        chipYasam     = view.findViewById(R.id.kesfetChipYasam);
        chipEnerji    = view.findViewById(R.id.kesfetChipEnerji);

        makaleRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        makaleRv.setNestedScrollingEnabled(false);
        adapter = new MakaleAdapter(gosterilen);
        makaleRv.setAdapter(adapter);

        // İnteraktif elementleri bağla
        aramaDinleyiciKur();
        kategoriChipleriniBagla();
        aboneOlButonunuBagla(view);
        dahaFazlaYukleButonunuBagla(view);
        editorSeciminiBagla(view);

        // Veriyi yükle (API varsa oradan, yoksa yerel)
        if (AgBaglantisi.internetVarMi(requireContext())
                && !ApiClient.API_KEY.equals("YOUR_NEWSAPI_KEY_HERE")) {
            haberleriApiContextenCek();
        } else {
            yerelVeriYukle();
        }
    }

    // ============================================================
    //  ARAMA - yazdıkça gerçek zamanlı filtreleme
    // ============================================================
    private void aramaDinleyiciKur() {
        aramaKutusu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {
                aramaMetni = s.toString().trim().toLowerCase();
                filtreleVeGoster();
            }
        });
    }

    // ============================================================
    //  KATEGORİ CHIPLERİ - tıklayınca o kategoriye filtrele
    // ============================================================
    private void kategoriChipleriniBagla() {
        chipHepsi.setOnClickListener(v -> kategoriSec(Makale.KAT_HEPSI));
        chipTeknoloji.setOnClickListener(v -> kategoriSec(Makale.KAT_TEKNOLOJI));
        chipYasam.setOnClickListener(v -> kategoriSec(Makale.KAT_YASAM));
        chipEnerji.setOnClickListener(v -> kategoriSec(Makale.KAT_ENERJI));
    }

    private void kategoriSec(String yeniKategori) {
        aktifKategori = yeniKategori;
        chipGorunumGuncelle();
        filtreleVeGoster();
    }

    /** Aktif chip'i koyu yeşil, diğerlerini açık yeşil yapar */
    private void chipGorunumGuncelle() {
        // Tümünü önce pasif (açık yeşil) yap
        pasifChip(chipHepsi);
        pasifChip(chipTeknoloji);
        pasifChip(chipYasam);
        pasifChip(chipEnerji);

        // Aktif olanı koyu yap
        switch (aktifKategori) {
            case Makale.KAT_HEPSI:     aktifChip(chipHepsi); break;
            case Makale.KAT_TEKNOLOJI: aktifChip(chipTeknoloji); break;
            case Makale.KAT_YASAM:     aktifChip(chipYasam); break;
            case Makale.KAT_ENERJI:    aktifChip(chipEnerji); break;
        }
    }

    private void aktifChip(TextView c) {
        c.setBackgroundResource(R.drawable.bg_card_dark_green);
        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_dark));
    }

    private void pasifChip(TextView c) {
        c.setBackgroundResource(R.drawable.bg_chip_light);
        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary_dark));
    }

    // ============================================================
    //  FİLTRELEME MOTORU - arama + kategori birlikte uygulanır
    // ============================================================
    private void filtreleVeGoster() {
        gosterilen.clear();

        for (Makale m : tumMakaleler) {
            // Kategori uyumu
            boolean kategoriUyar = aktifKategori.equals(Makale.KAT_HEPSI)
                    || aktifKategori.equals(m.getKategori());
            if (!kategoriUyar) continue;

            // Arama uyumu (boş ise hepsi geçer)
            if (TextUtils.isEmpty(aramaMetni)) {
                gosterilen.add(m);
            } else {
                String birlestir = ((m.getBaslik() != null ? m.getBaslik() : "") + " "
                        + (m.getAciklama() != null ? m.getAciklama() : "") + " "
                        + m.getEtiket()).toLowerCase();
                if (birlestir.contains(aramaMetni)) {
                    gosterilen.add(m);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // "Sonuç yok" mesajını göster/gizle
        sonucYokText.setVisibility(gosterilen.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ============================================================
    //  ABONE OL - e-posta doğrulama + SharedPreferences'a kayıt
    // ============================================================
    private void aboneOlButonunuBagla(View view) {
        EditText epostaInput = view.findViewById(R.id.kesfetEmail);
        TextView aboneBtn = view.findViewById(R.id.kesfetSubscribe);

        // Daha önce abone olunmuş mu kontrol et
        SharedPreferences p = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String oncekiEposta = p.getString(KEY_EPOSTA, null);
        if (oncekiEposta != null) {
            epostaInput.setText(oncekiEposta);
            aboneBtn.setText("✓ Abone Oldunuz");
        }

        aboneBtn.setOnClickListener(v -> {
            String eposta = epostaInput.getText().toString().trim();

            // Doğrulama
            if (TextUtils.isEmpty(eposta)) {
                epostaInput.setError("E-posta boş bırakılamaz");
                epostaInput.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(eposta).matches()) {
                epostaInput.setError("Geçerli bir e-posta adresi girin");
                epostaInput.requestFocus();
                return;
            }

            // Kaydet
            p.edit().putString(KEY_EPOSTA, eposta).apply();

            // Klavyeyi gizle
            klavyeyiGizle(epostaInput);

            // Görsel onay
            aboneBtn.setText("✓ Abone Olundunuz");
            Toast.makeText(requireContext(),
                    eposta + " adresi haftalık bültene kaydedildi 🎉",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void klavyeyiGizle(View v) {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    // ============================================================
    //  DAHA FAZLA YÜKLE - ek demo makaleler ekler
    // ============================================================
    private void dahaFazlaYukleButonunuBagla(View view) {
        TextView btn = view.findViewById(R.id.kesfetLoadMore);
        btn.setOnClickListener(v -> {
            int oncekiSayi = tumMakaleler.size();
            tumMakaleler.addAll(ekstraMakalelerOlustur());
            filtreleVeGoster();
            int yeniSayi = tumMakaleler.size() - oncekiSayi;
            Toast.makeText(requireContext(),
                    yeniSayi + " yeni makale yüklendi", Toast.LENGTH_SHORT).show();
        });
    }

    // ============================================================
    //  EDİTÖRÜN SEÇİMİ - "Devamını Oku" butonu
    // ============================================================
    private void editorSeciminiBagla(View view) {
        View editorKart = view.findViewById(R.id.kesfetEditorPick);
        TextView devamBtn = view.findViewById(R.id.kesfetEditorReadMore);

        View.OnClickListener listener = v -> {
            Toast.makeText(requireContext(),
                    "Makale yakında okunabilir olacak", Toast.LENGTH_SHORT).show();
        };
        editorKart.setOnClickListener(listener);
        devamBtn.setOnClickListener(listener);
    }

    // ============================================================
    //  API'DEN VERİ ÇEKME
    // ============================================================
    private void haberleriApiContextenCek() {
        yuklemeBar.setVisibility(View.VISIBLE);

        Call<NewsApiResponse> call = ApiClient.getNewsApi()
                .getSurdurulebilirlikHaberleri(
                        "sustainability OR renewable OR climate OR recycling",
                        "en",
                        "publishedAt",
                        20,
                        ApiClient.API_KEY
                );

        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsApiResponse> call,
                                   @NonNull Response<NewsApiResponse> response) {
                yuklemeBar.setVisibility(View.GONE);

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().articles != null
                        && !response.body().articles.isEmpty()) {

                    tumMakaleler.clear();
                    tumMakaleler.addAll(response.body().articles);
                    filtreleVeGoster();
                    Log.d(TAG, "API'den " + tumMakaleler.size() + " haber alındı");
                } else {
                    Log.w(TAG, "API yanıtı boş - yerel veriye geçiliyor");
                    yerelVeriYukle();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsApiResponse> call, @NonNull Throwable t) {
                yuklemeBar.setVisibility(View.GONE);
                Log.e(TAG, "API hatası: " + t.getMessage());
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                            "Çevrimdışı moda geçildi", Toast.LENGTH_SHORT).show();
                }
                yerelVeriYukle();
            }
        });
    }

    // ============================================================
    //  YEREL DEMO VERİ
    // ============================================================
    private void yerelVeriYukle() {
        tumMakaleler.clear();

        // Yaşam kategorisi
        tumMakaleler.add(new Makale(
                "DOĞA GÜNLÜĞÜ",
                "Yerel Ekosistemi Korumak",
                "Bahçenizde biyoçeşitliliği artırmak için uygulayabileceğiniz basit adımlar.",
                null, "2023-10-12T00:00:00Z"));
        tumMakaleler.add(new Makale(
                "ECOLIFE MAG",
                "Geri Dönüşümün Ötesi",
                "Neden 'azaltmak' ve 'yeniden kullanmak' geri dönüşümden daha önemli?",
                null, "2023-10-10T00:00:00Z"));
        tumMakaleler.add(new Makale(
                "EKOLOJİ POSTASI",
                "Sıfır Atık Mutfak Rehberi",
                "Mutfakta gıda israfını ve plastik kullanımını azaltmanın yolları.",
                null, "2023-10-09T00:00:00Z"));

        // Enerji kategorisi
        tumMakaleler.add(new Makale(
                "ENERGY TRENDS",
                "Rüzgar Enerjisi 101",
                "Ev tipi rüzgar türbinleri mantıklı mı? Maliyet ve verimlilik analizi.",
                null, "2023-10-08T00:00:00Z"));
        tumMakaleler.add(new Makale(
                "ENERJİ ATLASI",
                "Güneş Paneli Kurulum Maliyetleri 2024",
                "Türkiye'de ev tipi güneş enerjisi sisteminin geri ödeme süresi.",
                null, "2023-10-06T00:00:00Z"));

        // Teknoloji kategorisi
        tumMakaleler.add(new Makale(
                "GREENTECH DAILY",
                "Geleceğin Şehirleri: Düşük Karbonlu Yaşam Alanları",
                "Kentsel planlamada yeni yaklaşımlar, karbon ayak izimizi nasıl %40 oranında azaltabileceğimizi gösteriyor.",
                null, "2023-10-15T00:00:00Z"));
        tumMakaleler.add(new Makale(
                "GREENTECH DAILY",
                "Akıllı Çöp Konteynerleri ve Yapay Zeka",
                "AI tabanlı atık ayrıştırma sistemleri geri dönüşüm oranını ikiye katlıyor.",
                null, "2023-10-05T00:00:00Z"));

        filtreleVeGoster();
    }

    /** "Daha Fazla Yükle" butonu için ek demo makaleler */
    private List<Makale> ekstraMakalelerOlustur() {
        List<Makale> ek = new ArrayList<>();
        ek.add(new Makale(
                "İKLİM RAPORU",
                "Karbon Ayak İzinizi Hesaplayın",
                "Günlük alışkanlıklarınızın iklim üzerindeki etkisini ölçen pratik araçlar.",
                null, "2023-10-03T00:00:00Z"));
        ek.add(new Makale(
                "GREENTECH DAILY",
                "Elektrikli Araçlar: 2024 Karşılaştırması",
                "Türkiye'deki elektrikli araç modellerinin menzil ve şarj maliyetleri.",
                null, "2023-10-01T00:00:00Z"));
        ek.add(new Makale(
                "ECOLIFE MAG",
                "Vegan Beslenmeye Geçiş İçin 10 Adım",
                "Sağlığınızı ve gezegeni korumak için pratik öneriler.",
                null, "2023-09-28T00:00:00Z"));
        ek.add(new Makale(
                "ENERGY TRENDS",
                "Pil Teknolojisindeki Devrim",
                "Yeni nesil katı hal pilleri menzili nasıl uzatacak?",
                null, "2023-09-25T00:00:00Z"));
        return ek;
    }
}
