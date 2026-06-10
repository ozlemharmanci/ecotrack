package com.ecotrack.app.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tek seferlik Retrofit istemcisi.
 *
 * Tüm uygulama boyunca aynı OkHttpClient ve Retrofit örneğini kullanırız;
 * her API çağrısında yeni nesne yaratmak verimsizdir.
 */
public class ApiClient {

    public static final String BASE_URL = "https://newsapi.org/";

    // ÖNEMLİ: Bu anahtarı kendi NewsAPI hesabınızla alıp değiştirin.
    // newsapi.org/register'dan ücretsiz alabilirsiniz.
    // Üretim ortamında BuildConfig veya .properties dosyasında saklayın!
    public static final String API_KEY = "YOUR_NEWSAPI_KEY_HERE";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // İstekleri Logcat'te görmek için log interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static NewsApiService getNewsApi() {
        return getClient().create(NewsApiService.class);
    }
}
