# 🌿 EcoTrack — Sürdürülebilir Yaşam Takip Uygulaması

EcoTrack, kullanıcıların günlük sürdürülebilir alışkanlıklarını (geri dönüşüm, toplu taşıma, bitki bazlı beslenme vb.) takip etmesini, CO₂ tasarrufunu ölçmesini ve çevre haberlerini keşfetmesini sağlayan bir **Android mobil uygulamasıdır**.

Java ile yazılmıştır. Yerel **SQLite** veritabanı, **NewsAPI.org** REST API entegrasyonu ve **Retrofit** ağ katmanı içerir.

---

## 📱 Ekranlar

Uygulama, alt navigasyon çubuğuyla erişilen **dört ana sayfadan** oluşur:

| Sekme | Açıklama |
|-------|----------|
| **Panel** | Günlük limit dairesel göstergesi, CO₂ tasarruf kartı, aktif seriler (yatay liste), topluluk ve günün ipucu kartları. |
| **Günlük** | Toplam XP ve seviye ilerlemesi, aktif seri sayacı, son aktiviteler listesi (SQLite'tan), haftalık hedef kartı. |
| **Keşfet** | Arama çubuğu, kategori filtreleri, editörün seçimi, haftalık bülten, ve canlı API'den çekilen "Son Paylaşılanlar" haber akışı. |
| **Profil** | Toplam XP, CO₂ tasarrufu ve aktivite sayısı istatistikleri. |

> Ekran görüntüleri `docs/screenshots/` klasöründedir.

---

## 🏗️ Mimari

```
EcoTrack/
├── app/
│   ├── src/main/
│   │   ├── java/com/ecotrack/app/
│   │   │   ├── MainActivity.java          # Tek Activity, alt navigasyonu yönetir
│   │   │   ├── fragments/                 # 4 ana ekran
│   │   │   │   ├── PanelFragment.java
│   │   │   │   ├── GunlukFragment.java
│   │   │   │   ├── KesfetFragment.java     # API çağrısının yapıldığı yer
│   │   │   │   └── ProfilFragment.java
│   │   │   ├── adapters/                   # RecyclerView adapterları
│   │   │   │   ├── ActivityAdapter.java
│   │   │   │   ├── SeriAdapter.java
│   │   │   │   └── MakaleAdapter.java
│   │   │   ├── models/                     # Veri modelleri
│   │   │   │   ├── EcoActivity.java
│   │   │   │   ├── Seri.java
│   │   │   │   └── Makale.java
│   │   │   ├── database/
│   │   │   │   └── DatabaseHelper.java      # SQLite (CRUD + toplam hesaplar)
│   │   │   ├── api/                         # Retrofit ağ katmanı
│   │   │   │   ├── ApiClient.java
│   │   │   │   ├── NewsApiService.java
│   │   │   │   └── NewsApiResponse.java
│   │   │   └── utils/
│   │   │       └── AgBaglantisi.java        # İnternet bağlantısı kontrolü
│   │   ├── res/
│   │   │   ├── layout/                      # Tüm ekran ve item XML'leri
│   │   │   ├── drawable/                    # İkonlar ve şekil arka planları
│   │   │   ├── values/                      # colors, strings, themes
│   │   │   └── menu/                        # Alt navigasyon menüsü
│   │   └── AndroidManifest.xml
│   └── build.gradle                         # Modül bağımlılıkları
├── build.gradle                             # Proje yapılandırması
├── settings.gradle
└── gradlew / gradlew.bat                    # Gradle wrapper
```

**Tasarım deseni:** Single-Activity + Fragment mimarisi (Google'ın modern önerisi).

---

## 🗄️ Veritabanı — SQLite Kullanım Nedenleri


- Android'e **yerleşik** gelir — ek kurulum, hesap veya sunucu gerektirmez.
- Tüm veri tek bir dosyada (`/data/data/com.ecotrack.app/databases/ecotrack.db`) tutulur.
- İnternet bağlantısı gerektirmez — uygulama çevrimdışı çalışır.
- Firebase'e göre çok daha az yapılandırma gerektirir (Firebase bulut hesabı, `google-services.json`, proje kurulumu ister).

`DatabaseHelper.java` tek bir `activities` tablosu yönetir ve şu işlemleri sunar:
`aktiviteEkle()`, `tumAktiviteleriGetir()`, `toplamXp()`, `toplamCo2Tasarrufu()`, `aktiviteSil()`.

İlk açılışta tablo otomatik oluşturulur ve ekran boş görünmesin diye 3 demo aktivite eklenir.

---

## 🌐 API Entegrasyonu — NewsAPI.org

Keşfet sekmesi, çevre/sürdürülebilirlik haberlerini **NewsAPI.org**'dan çeker.

### API anahtarı alma (ücretsiz)

1. [newsapi.org/register](https://newsapi.org/register) adresinden ücretsiz kayıt olun.
2. Panelden API anahtarınızı kopyalayın.
3. `app/src/main/java/com/ecotrack/app/api/ApiClient.java` dosyasını açın.
4. Şu satırı bulun ve anahtarınızı yapıştırın:
   ```java
   public static final String API_KEY = "YOUR_NEWSAPI_KEY_HERE";
   ```

> **Önemli:** NewsAPI'nin ücretsiz katmanı yalnızca geliştirme amaçlıdır ve bazı bölgelerde yalnızca `localhost`'tan çalışabilir. Bu yüzden uygulama, API çağrısı başarısız olursa **otomatik olarak yerel demo verilere düşer** (`yerelVeriYukle()`). Yani anahtar olmadan da uygulama sorunsuz çalışır ve örnek haberleri gösterir.

### Ağ katmanı

- **Retrofit 2** + **Gson** dönüştürücü → JSON'u otomatik olarak `Makale` nesnelerine çevirir.
- **OkHttp** + log interceptor → istekleri Logcat'te izlemenizi sağlar.
- `AgBaglantisi.internetVarMi()` → çağrı öncesi bağlantı kontrolü.

---

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler

- **JDK 17** (Android Gradle Plugin 8.x ister)
- **Android SDK** (API 34 derleme hedefi, API 24+ çalışma)
- Bir IDE: **Android Studio** *veya* **IntelliJ IDEA** (kod aynıdır)

### Adımlar

```bash
# 1. Projeyi klonlayın
git clone https://github.com/ozlemharmanci/ecotrack.git
cd EcoTrack

# 2. (İsteğe bağlı) NewsAPI anahtarınızı ApiClient.java'ya ekleyin

# 3. Android Studio ile açın: File > Open > EcoTrack klasörünü seçin
#    Gradle senkronizasyonunu bekleyin (ilk seferde bağımlılıklar in
#    indirilir).

# 4. Çalıştırın: Yeşil "Run" düğmesine basın veya:
./gradlew installDebug    # bağlı cihaza yükler
```
---

## 📦 GitHub'a Yükleme

```bash
cd EcoTrack

# Git deposunu başlat
git init
git add .
git commit -m "İlk commit: EcoTrack tam uygulama"

# GitHub'da boş bir repo oluştur (github.com/new), sonra:
git branch -M main
git remote add origin git clone https://github.com/ozlemharmanci/ecotrack.git
git push -u origin main
```

`.gitignore` dosyası `build/`, `.gradle/`, `local.properties` ve imza anahtarlarını otomatik hariç tutar — yani repoya yalnızca kaynak kod gider.

---


## 🔧 Kullanılan Teknolojiler

- **Dil:** Java 17
- **Min SDK:** 24 (Android 7.0) — **Hedef SDK:** 34 (Android 14)
- **UI:** Material Components, ConstraintLayout, RecyclerView, CardView
- **Ağ:** Retrofit 2.9, OkHttp 4.12, Gson
- **Resim:** Glide 4.16
- **Veritabanı:** SQLite (android.database.sqlite)
- **Build:** Gradle 8.4, Android Gradle Plugin 8.1.4

---

## 📄 Lisans

MIT Lisansı — ayrıntılar için [LICENSE](LICENSE) dosyasına bakın.
