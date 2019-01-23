/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.issues.PushJobData;

public class AuthHandler {
	
	private final String REST_APIKEYLOGIN  = "/api/v2/Account/ApiKeyLogin";
	private final String REST_APPSCOUNT    = "/api/v2/Apps/Count";
	
	private static AuthHandler authHandler;
	private static Map<String, String> bearerTokens = Collections.synchronizedMap(new PassiveExpiringMap<String, String>(1000 * 60 * 60 * 3));  //3 hour expiry
	
	public static synchronized AuthHandler getInstance() {
		if (authHandler == null) {
			authHandler = new AuthHandler();
		}
		return authHandler;
	}
	
	public String getBearerToken(PushJobData jobData) {
		String url          = jobData.appscanData.url;
		String apikeyid     = jobData.appscanData.apikeyid;
		String apikeysecret = jobData.appscanData.apikeysecret;
		
		String key = url + apikeyid; //keys are the url + the apikeyid since we want to support multiple servers and users
		
		String bearerToken = bearerTokens.get(key);  
		if ( (bearerToken != null) && isStillValid(url, bearerToken)) {
			return bearerToken;
		}
		//TODO: Handle error
		String newBearerToken = authenticate(url, apikeyid, apikeysecret); 
		bearerTokens.put(key, newBearerToken);
		return newBearerToken;
	}
	
	private String authenticate(String url, String apikeyid, String apikeysecret) {
		RestTemplate restTemplate = new RestTemplate();
		ApiKeyLoginRequest apiKeyLoginRequest = new ApiKeyLoginRequest();
		apiKeyLoginRequest.KeyId=apikeyid;
		apiKeyLoginRequest.KeySecret=apikeysecret;
		ApiKeyLoginResponse response = restTemplate.postForObject(url + REST_APIKEYLOGIN, apiKeyLoginRequest, ApiKeyLoginResponse.class);
		return "Bearer " + response.Token;		
	}
	
	private boolean isStillValid(String url, String bearerToken) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
	 	    headers.add("Authorization", bearerToken);
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        ResponseEntity<AppsCountResponse> response = restTemplate.exchange(url + REST_APPSCOUNT, HttpMethod.GET, entity, AppsCountResponse.class);
	        if(response.getStatusCode().is2xxSuccessful()) {
	        	return true;
	        }
		} catch (RestClientException e) {
			//Will return false below
		}
		return false;
	}

	@SuppressWarnings("unused")
	private static class AppsCountResponse {
		public String Total;
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
