package com.ecotrack.app.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * NewsAPI.org REST endpoint'lerinin Retrofit arayüzü.
 *
 * NewsAPI sürdürülebilirlik/çevre haberlerini ücretsiz katmanından
 * sağlar. Geliştirici hesabı ile günde 100 istek yapılabilir.
 * Kayıt: https://newsapi.org/register
 *
 * NOT: Ücretsiz katman sadece geliştirme makinesinden (localhost) çalışır.
 *      Üretim için ücretli plan gerekir. Bu yüzden API hata verirse
 *      KesfetFragment yerel demo verilere düşer.
 */
public interface NewsApiService {

    /**
     * Anahtar kelimeyle haber arar.
     * @param q   arama sorgusu, örn. "sustainability OR renewable OR climate"
     * @param language  "tr" Türkçe içerik; "en" ile başla, içerik daha fazladır
     * @param sortBy    "publishedAt" en yeniden başlar
     * @param pageSize  sayfa başı sonuç (max 100, free plan max 20 önerilir)
     * @param apiKey    https://newsapi.org/account adresinden alınır
     */
    @GET("v2/everything")
    Call<NewsApiResponse> getSurdurulebilirlikHaberleri(
            @Query("q") String q,
            @Query("language") String language,
            @Query("sortBy") String sortBy,
            @Query("pageSize") int pageSize,
            @Query("apiKey") String apiKey
    );
}
