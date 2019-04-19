/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.appscanprovider.AuthHandler;

public class ASOCAuthHandler extends AuthHandler {

	private static final String REST_APIKEYLOGIN = "/api/v2/Account/ApiKeyLogin";
	private static final String REST_APPSCOUNT = "/api/v2/Apps/Count";

	private static ASOCAuthHandler authHandler;

	public static synchronized ASOCAuthHandler getInstance() {
		if (authHandler == null) {
			authHandler = new ASOCAuthHandler();
		}
		return authHandler;
	}

	@Override
	protected RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Override
	protected String authenticate(String url, String apikeyid, String apikeysecret) {
		RestTemplate restTemplate = new RestTemplate();
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
