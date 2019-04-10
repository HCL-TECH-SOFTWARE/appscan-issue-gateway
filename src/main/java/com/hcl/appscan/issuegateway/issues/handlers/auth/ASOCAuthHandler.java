package com.hcl.appscan.issuegateway.issues.handlers.auth;

import org.springframework.web.client.RestTemplate;

public class ASOCAuthHandler extends AuthHandler{
	private static ASOCAuthHandler authHandler;
	
	public static synchronized ASOCAuthHandler getInstance() {
		if (authHandler == null) {
			authHandler = new ASOCAuthHandler();
		}
		return authHandler;
	}
	
	private String getAuthToken(String url, String apikeyid, String apikeysecret) {
		RestTemplate restTemplate = new RestTemplate();
		ASOCApiKeyLoginRequest apiKeyLoginRequest = new ASOCApiKeyLoginRequest();
		apiKeyLoginRequest.KeyId=apikeyid;
		apiKeyLoginRequest.KeySecret=apikeysecret;
		ASOCApiKeyLoginResponse response = restTemplate.postForObject(url + ASOC_API__APIKEYLOGIN, apiKeyLoginRequest, ASOCApiKeyLoginResponse.class);
		return "Bearer " + response.Token;
	}
	
	@SuppressWarnings("unused")
	private static class ASOCApiKeyLoginRequest {
		public String KeyId;
		public String KeySecret;
	}
	
	@SuppressWarnings("unused")
	private static class ASOCApiKeyLoginResponse {
		public String Token;
		public String Expire;
	}

	@Override
	protected RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Override
	protected String authenticate(String url, String apikeyid, String apikeysecret) throws Exception {
		return getAuthToken(url, apikeyid, apikeysecret);
	}

	@Override
	protected String getAuthorizationHeaderName() {
		return HEADER_AUTHORIZATION;
	}

	@Override
	protected String getValidationAPI() {
		return ASOC_API_APPSCOUNT;
	}
}
