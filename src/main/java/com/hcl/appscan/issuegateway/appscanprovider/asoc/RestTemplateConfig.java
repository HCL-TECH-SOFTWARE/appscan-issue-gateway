package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

import static org.apache.http.impl.client.HttpClients.*;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {
        return builder
                .requestFactory(this::validateAllCertificatesRequestFactory)
                .build();
    }

    private ClientHttpRequestFactory validateAllCertificatesRequestFactory(){
        SSLContext sslContext = null;
        try {
            sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true) // Trust all certificates
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

        CloseableHttpClient httpClient = custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier((hostname, session) -> true) // Disable hostname verification
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
