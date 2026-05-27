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

    public static OkHttpClient createUnsafeClient(OkHttpClient.Builder httpClientBuilder) {
        try {
            X509TrustManager trustAllCertificatesManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] certChain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certChain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext unsafeSslContext = SSLContext.getInstance("TLS");
            unsafeSslContext.init(null, new TrustManager[]{trustAllCertificatesManager}, new SecureRandom());

            HostnameVerifier allHostsValidVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostName, SSLSession sslSession) {
                    return true;
                }
            };

            return httpClientBuilder
                    .sslSocketFactory(unsafeSslContext.getSocketFactory(), trustAllCertificatesManager)
                    .hostnameVerifier(allHostsValidVerifier)
                    .build();
        } catch (Exception creationError) {
            throw new IllegalStateException("Unable to create unsafe SSL client", creationError);
        }
    }
}
