/**
 * © Copyright HCL Technologies Ltd. 2018, 2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

import static org.apache.http.impl.client.HttpClients.custom;

class ASOCUtils {

	private ASOCUtils() {}

	public static RestTemplate createUntrustedASOCRestTemplate() {
		RestTemplate restTemplate;
		RestTemplateBuilder builder = new RestTemplateBuilder();
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

		Supplier<ClientHttpRequestFactory> clientHttpRequestFactory = () -> new HttpComponentsClientHttpRequestFactory(httpClient);
		restTemplate = builder.requestFactory(clientHttpRequestFactory).build();

		restTemplate.setErrorHandler(new ResponseErrorHandler());
		return restTemplate;
	}

	public static RestTemplate createASOCRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ResponseErrorHandler());
		return restTemplate;
	}

	public static HttpHeaders createASOCAuthorizedHeaders(PushJobData jobData) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, ASOCAuthHandler.getInstance().getBearerToken(jobData));
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		return headers;
	}

}
