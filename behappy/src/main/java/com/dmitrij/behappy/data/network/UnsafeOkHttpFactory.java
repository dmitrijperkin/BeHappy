package com.dmitrij.behappy.data.network;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public final class UnsafeOkHttpFactory {

    private UnsafeOkHttpFactory() {
    }

    public static OkHttpClient createUnsafeClient(OkHttpClient.Builder baseBuilder) {
        try {
            X509TrustManager trustAll = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAll}, new SecureRandom());

            HostnameVerifier unsafeHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            return baseBuilder
                    .sslSocketFactory(sslContext.getSocketFactory(), trustAll)
                    .hostnameVerifier(unsafeHostnameVerifier)
                    .build();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create unsafe SSL client", exception);
        }
    }
}
