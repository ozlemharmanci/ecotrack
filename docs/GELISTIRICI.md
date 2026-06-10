# 🛠️ Geliştirici Dokümantasyonu

Bu belge, EcoTrack'in iç işleyişini ve kodu genişletmek isteyenler için teknik detayları açıklar.

## Veri Akışı

### SQLite okuma akışı (örn. Günlük sekmesi)

```
GunlukFragment.onViewCreated()
  → new DatabaseHelper(context)
  → db.tumAktiviteleriGetir()      // List<EcoActivity> döner
  → new ActivityAdapter(liste)
  → recyclerView.setAdapter(adapter)
```

### API çağrısı akışı (Keşfet sekmesi)

```
KesfetFragment.onViewCreated()
  → AgBaglantisi.internetVarMi()?  ── hayır ──> yerelVeriYukle()
        │ evet
        ▼
  ApiClient.getNewsApi()
  → .getSurdurulebilirlikHaberleri(...)   // Retrofit Call
  → call.enqueue(callback)                // arka planda asenkron
        │
        ├─ onResponse + başarılı  → adapter'ı API verisiyle doldur
        └─ onFailure / boş yanıt  → yerelVeriYukle()  (yedek)
```

Asenkron `enqueue()` kullanıldığı için ağ çağrısı UI thread'ini bloklamaz.

## Yeni bir aktivite tipi ekleme

1. `EcoActivity.java` içine yeni bir `TYPE_*` sabiti ekle.
2. `getIconResource()` switch'ine yeni tip için ikon eşlemesi ekle.
3. (İsteğe bağlı) `drawable/` klasörüne yeni vektör ikon koy.

## Yeni bir sekme/fragment ekleme

1. `fragments/` altına yeni `XFragment.java` oluştur.
2. `res/layout/fragment_x.xml` layout'unu yaz.
3. `res/menu/bottom_nav_menu.xml` içine yeni `<item>` ekle.
4. `MainActivity` içindeki `setOnItemSelectedListener` bloğuna yeni `else if` ekle.

## XP ve Seviye Mantığı

- Her aktivitenin sabit bir `xpReward` değeri vardır (DB'de saklanır).
- `GunlukFragment` toplam XP'yi `1240 (baz) + db.toplamXp()` olarak hesaplar.
- Baz değer, ekran görüntüsündeki "1.240 XP" başlangıç durumunu taklit eder; gerçek bir kullanıcı sisteminde bu sadece `db.toplamXp()` olur.
- Seviye eşiği `SONRAKI_SEVIYE_XP = 1500` sabitiyle belirlenir.

## SQLite Şeması

```sql
CREATE TABLE activities (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT NOT NULL,      -- "Geri Dönüştürülmüş Plastikler"
    type        TEXT NOT NULL,      -- RECYCLE / TRANSPORT / PLANT_FOOD ...
    xp_reward   INTEGER DEFAULT 0,  -- +15, +25, +30
    timestamp   INTEGER NOT NULL,   -- System.currentTimeMillis()
    co2_saved   REAL DEFAULT 0      -- kg cinsinden
);
```

Veritabanı sürümü değişirse (`DB_VERSION`), `onUpgrade()` mevcut tabloyu silip yeniden oluşturur. Üretimde veri kaybını önlemek için `ALTER TABLE` tabanlı migration tercih edilmelidir.

## Bilinen Sınırlamalar / Geliştirme Fikirleri

- **+ (FAB) butonu** layout'larda gösterilmiyor; `ic_plus` ikonu hazır. Bir `FloatingActionButton` ekleyip tıklanınca "Aktivite Ekle" diyaloğu açılabilir ve `db.aktiviteEkle()` çağrılabilir.
- Kategori chip'leri ve arama kutusu şu an statik; `KesfetFragment`'ta filtreleme mantığı eklenebilir.
- Bülten "Abone Ol" butonu UI'da var; bir e-posta servisi API'sine bağlanabilir.
- Profil bilgileri sabit; `SharedPreferences` ile kullanıcı adı/avatar kişiselleştirilebilir.

## Test

`androidTest` ve `test` bağımlılıkları (`build.gradle`) JUnit ve Espresso içerir. Örnek bir veritabanı testi `DatabaseHelper.aktiviteEkle()` → `tumAktiviteleriGetir()` döngüsünü doğrulayabilir.
