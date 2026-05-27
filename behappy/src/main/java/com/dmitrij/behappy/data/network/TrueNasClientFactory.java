package com.dmitrij.behappy.data.network;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class TrueNasClientFactory {
    private static final Map<String, TrueNasApi> apiClientCache = new HashMap<>();

    private TrueNasClientFactory() {
    }

    public static synchronized TrueNasApi create(String nasHost, String apiSecret, boolean ignoreSsl) {
        if ("demo_mode".equals(nasHost)) {
            return DemoTrueNasApi.getInstance();
        }

        String sanitizedHostUrl = sanitizeHostUrl(nasHost);
        String clientLookupKey = sanitizedHostUrl + "|" + apiSecret + "|" + ignoreSsl;
        
        if (apiClientCache.containsKey(clientLookupKey)) {
            return apiClientCache.get(clientLookupKey);
        }

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(requestChain -> {
                    Request originalRequest = requestChain.request();
                    Request authenticatedRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + apiSecret)
                            .method(originalRequest.method(), originalRequest.body())
                            .build();
                    return requestChain.proceed(authenticatedRequest);
                });

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(logging);

        OkHttpClient okHttpClientInstance = ignoreSsl
                ? UnsafeOkHttpFactory.createUnsafeClient(httpClientBuilder)
                : httpClientBuilder.build();

        Retrofit retrofitInstance = new Retrofit.Builder()
                .baseUrl(sanitizedHostUrl)
                .client(okHttpClientInstance)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        android.util.Log.d("TrueNasAPI", "Creating client for: " + sanitizedHostUrl);

        TrueNasApi nasApiService = retrofitInstance.create(TrueNasApi.class);
        apiClientCache.put(clientLookupKey, nasApiService);
        return nasApiService;
    }

    private static String sanitizeHostUrl(String rawHost) {
        if (rawHost == null) return "https://localhost/";
        String trimmedHost = rawHost.trim();
        if (trimmedHost.startsWith("http://") || trimmedHost.startsWith("https://")) {
            return trimmedHost.endsWith("/") ? trimmedHost : trimmedHost + "/";
        }
        return "https://" + (trimmedHost.endsWith("/") ? trimmedHost : trimmedHost + "/");
    }
}
