package com.dmitrij.behappy.data.network;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class TrueNasClientFactory {
    private static final Map<String, TrueNasApi> clients = new HashMap<>();

    private TrueNasClientFactory() {
    }

    public static synchronized TrueNasApi create(String host, String apiKey, boolean allowSelfSigned) {
        if ("demo_mode".equals(host)) {
            return DemoTrueNasApi.getInstance();
        }

        String normalizedHost = normalizeHost(host);
        String cacheKey = normalizedHost + "|" + apiKey + "|" + allowSelfSigned;
        
        if (clients.containsKey(cacheKey)) {
            return clients.get(cacheKey);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + apiKey)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                });

        OkHttpClient client = allowSelfSigned
                ? UnsafeOkHttpFactory.createUnsafeClient(builder)
                : builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(normalizedHost)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TrueNasApi api = retrofit.create(TrueNasApi.class);
        clients.put(cacheKey, api);
        return api;
    }

    private static String normalizeHost(String host) {
        if (host == null) return "https://localhost/";
        String h = host.trim();
        if (h.startsWith("http://") || h.startsWith("https://")) {
            return h.endsWith("/") ? h : h + "/";
        }
        return "https://" + (h.endsWith("/") ? h : h + "/");
    }
}
