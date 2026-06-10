package com.ecotrack.app.api;

import com.ecotrack.app.models.Makale;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * NewsAPI.org "/v2/everything" endpoint'inin döndürdüğü JSON yanıtının kabı.
 *
 * Örnek yanıt:
 * {
 *   "status": "ok",
 *   "totalResults": 1234,
 *   "articles": [ { ... }, { ... } ]
 * }
 */
public class NewsApiResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("totalResults")
    public int totalResults;

    @SerializedName("articles")
    public List<Makale> articles;

    @SerializedName("message")
    public String message;  // Hata durumunda gelir
}
