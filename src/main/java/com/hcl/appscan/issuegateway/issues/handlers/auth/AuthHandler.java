package com.hcl.appscan.issuegateway.issues.handlers.auth;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.IssueGatewayConstants;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public abstract class AuthHandler implements IssueGatewayConstants {
	
	protected static Map<String, String> bearerTokens = Collections.synchronizedMap(new PassiveExpiringMap<String, String>(1000 * 60 * 60 * 3));  //3 hour expiry
	
	public String getBearerToken(PushJobData jobData , List<String> errors) throws Exception {
		String productID    = jobData.getAppscanData().getAppscanProvider();
		String url          = jobData.getAppscanData().getUrl();
		String apikeyid     = jobData.getAppscanData().getApikeyid();
		String apikeysecret = jobData.getAppscanData().getApikeysecret();
		
		String key = url + apikeyid; //keys are the url + the apikeyid since we want to support multiple servers and users
		String bearerToken = bearerTokens.get(key);
		
		if ((bearerToken != null) && isStillValid(url, bearerToken,productID))
			return bearerToken;
		
		String newBearerToken = authenticate(url, apikeyid, apikeysecret); 
		bearerTokens.put(key, newBearerToken);
		return newBearerToken;
	}
	
	private boolean isStillValid(String url, String bearerToken, String productId) {
		try {
			RestTemplate restTemplate=getRestTemplate();
			HttpHeaders headers = new HttpHeaders();
	 	    headers.add(getAuthorizationHeaderName(), bearerToken);
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        ResponseEntity<String> response = restTemplate.exchange(url + getValidationAPI(), HttpMethod.GET, entity, String.class);
	        if(response.getStatusCode().is2xxSuccessful()) {
	        	return true;
	        }
		} catch (RestClientException e) {
			//Will return false below
		}
		return false;
	}
	
	protected abstract RestTemplate getRestTemplate();
	
	protected abstract String authenticate(String url, String apikeyid, String apikeysecret) throws Exception;
	
	protected abstract String getAuthorizationHeaderName(); 

	protected abstract String getValidationAPI();
	
	@SuppressWarnings("unused")
	static class AppsCountResponse {
		public String Total;
	}
}
