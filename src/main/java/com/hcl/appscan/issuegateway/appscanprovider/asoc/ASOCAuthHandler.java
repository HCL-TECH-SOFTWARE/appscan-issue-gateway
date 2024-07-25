/**
 * © Copyright HCL Technologies Ltd. 2019, 2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.appscanprovider.AuthHandler;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.apache.http.impl.client.HttpClients.custom;

public class ASOCAuthHandler extends AuthHandler {

	private static final String REST_APIKEYLOGIN = "/api/v4/Account/ApiKeyLogin";
	private static final String REST_APPSCOUNT = "/api/v4/Account/IsAuthenticated";

	private static ASOCAuthHandler authHandler;

	public static synchronized ASOCAuthHandler getInstance() {
		if (authHandler == null) {
			authHandler = new ASOCAuthHandler();
		}
		return authHandler;
	}

	@Override
	protected RestTemplate getRestTemplate() {
		RestTemplateBuilder builder = new RestTemplateBuilder();
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
	@Override
	protected String authenticate(String url, String apikeyid, String apikeysecret) {
		RestTemplate restTemplate = getRestTemplate();
		ApiKeyLoginRequest apiKeyLoginRequest = new ApiKeyLoginRequest();
		apiKeyLoginRequest.KeyId = apikeyid;
		apiKeyLoginRequest.KeySecret = apikeysecret;
		ApiKeyLoginResponse response = restTemplate.postForObject(url + REST_APIKEYLOGIN, apiKeyLoginRequest,
				ApiKeyLoginResponse.class);
		return "Bearer " + response.Token;
	}

	@Override
	protected String getAuthorizationHeaderName() {
		return HttpHeaders.AUTHORIZATION;
	}

	@Override
	protected String getValidationAPI() {
		return REST_APPSCOUNT;
	}

	@SuppressWarnings("unused")
	private static class ApiKeyLoginRequest {
		public String KeyId;
		public String KeySecret;
	}

	@SuppressWarnings("unused")
	private static class ApiKeyLoginResponse {
		public String Token;
		public String Expire;
	}
}
