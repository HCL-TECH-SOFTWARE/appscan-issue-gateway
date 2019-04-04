/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.net.HttpCookie;
import java.util.ArrayList;
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

import com.hcl.appscan.issuegateway.CustomRestTemplateProvider;
import com.hcl.appscan.issuegateway.IssueGatewayConstants;
import com.hcl.appscan.issuegateway.AppscanProvider;
import com.hcl.appscan.issuegateway.applications.GetApplicationData;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class AuthHandler implements IssueGatewayConstants {
	
	private static AuthHandler authHandler;
	private static Map<String, String> bearerTokens = Collections.synchronizedMap(new PassiveExpiringMap<String, String>(1000 * 60 * 60 * 3));  //3 hour expiry
	private final List<HttpCookie> cookies = new ArrayList<>();
	
	public static synchronized AuthHandler getInstance() {
		if (authHandler == null) {
			authHandler = new AuthHandler();
		}
		return authHandler;
	}
	
	public List<HttpCookie> getCookies(){
		return cookies;
	}
	
	public String getBearerToken(PushJobData jobData , List<String> errors) throws Exception {
		String productID    = jobData.getAppscanData().getAppscanProvider();
		String url          = jobData.getAppscanData().getUrl();
		String apikeyid     = jobData.getAppscanData().getApikeyid();
		String apikeysecret = jobData.getAppscanData().getApikeysecret();
		
		String key = url + apikeyid; //keys are the url + the apikeyid since we want to support multiple servers and users
		
		String bearerToken = bearerTokens.get(key);
		
		if ( (bearerToken != null) && isStillValid(url, bearerToken,productID)) {
				return bearerToken;
		}
		String newBearerToken = authenticate(url, apikeyid, apikeysecret,productID); 
		bearerTokens.put(key, newBearerToken);
		return newBearerToken;
	}
	
	public String getBearerToken(GetApplicationData jobData) throws Exception {
		String productId    = jobData.getAppscanProvider();
		String url          = jobData.getUrl();
		String apikeyid     = jobData.getApikeyid();
		String apikeysecret = jobData.getApikeysecret();
		
		String key = url + apikeyid; //keys are the url + the apikeyid since we want to support multiple servers and users
		
		String bearerToken = bearerTokens.get(key);  
		if ( (bearerToken != null) && isStillValid(url, bearerToken,jobData.getAppscanProvider())) {
			return bearerToken;
		
		}		
		//TODO: Handle error
		String newBearerToken = authenticate(url, apikeyid, apikeysecret,productId); 
		bearerTokens.put(key, newBearerToken);
		return newBearerToken;
	}
	
	private String authenticate(String url, String apikeyid, String apikeysecret, String productId) throws Exception{
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.toString()))
			return getSessionId(url, apikeyid, apikeysecret);
		else 
			return getAuthToken(url, apikeyid, apikeysecret);
	}
	
	//TODO try to make this logic simple and align it with the correct use of rest template
	private String getSessionId(String url, String apikeyid, String apikeysecret) throws Exception {
		RestTemplate restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
		ASEApiKeyLoginRequest apiKeyLoginRequest1 = new ASEApiKeyLoginRequest();
		apiKeyLoginRequest1.keyId=apikeyid;
		apiKeyLoginRequest1.keySecret=apikeysecret;
		HttpEntity<ASEApiKeyLoginRequest> apiKeyLoginRequest=new HttpEntity<>(apiKeyLoginRequest1);
		ResponseEntity<ASEApiKeyLoginResponse> response=restTemplate.exchange(url + ASE_API_APIKEYLOGIN, HttpMethod.POST, apiKeyLoginRequest,ASEApiKeyLoginResponse.class);
		HttpHeaders headers=response.getHeaders();
		setCookies(headers);
		if(response.getStatusCode().is2xxSuccessful()) {
			return response.getBody().sessionId;
        }
		throw new Exception("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + url+" .Please verify the url, id and secret ");
    	
	}
	
	private void setCookies(HttpHeaders headers) {
		final List<String> cooks = headers.get("Set-Cookie");
        if (cooks != null && !cooks.isEmpty()) {
            cooks.stream().map((c) -> HttpCookie.parse(c)).forEachOrdered((cook) -> {
                cook.forEach((a) -> {
                    HttpCookie cookieExists = cookies.stream().filter(x -> a.getName().equals(x.getName())).findAny().orElse(null);
                    if (cookieExists != null) {
                        cookies.remove(cookieExists);
                    }
                    cookies.add(a);
                });
            });
        }
	}
	private String getAuthToken(String url, String apikeyid, String apikeysecret) {
		RestTemplate restTemplate = new RestTemplate();
		ASOCApiKeyLoginRequest apiKeyLoginRequest = new ASOCApiKeyLoginRequest();
		apiKeyLoginRequest.KeyId=apikeyid;
		apiKeyLoginRequest.KeySecret=apikeysecret;
		ASOCApiKeyLoginResponse response = restTemplate.postForObject(url + ASOC_API__APIKEYLOGIN, apiKeyLoginRequest, ASOCApiKeyLoginResponse.class);
		return "Bearer " + response.Token;
	}
	
	private boolean isStillValid(String url, String bearerToken, String productId) {
		try {
			RestTemplate restTemplate;
			if (productId.equalsIgnoreCase(AppscanProvider.ASE.toString()))
				restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
			else 
				restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
	 	    headers.add(getAuthorizationHeaderName(productId), bearerToken);
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        ResponseEntity<String> response = restTemplate.exchange(url + getValidationAPI(productId), HttpMethod.GET, entity, String.class);
	        if(response.getStatusCode().is2xxSuccessful()) {
	        	return true;
	        }
		} catch (RestClientException e) {
			//Will return false below
		}
		return false;
	}
	private String getAuthorizationHeaderName(String productId) {
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.toString()))
			return HEADER_ASC_XSRF_TOKEN;
		else 
			return HEADER_AUTHORIZATION;
		
	}

	private String getValidationAPI(String productId) {
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.toString()))
			return ASE_API_APPS_COUNT;
		else 
			return ASOC_API_APPSCOUNT;
	}
	
	@SuppressWarnings("unused")
	private static class AppsCountResponse {
		public String Total;//TODO check if this applicable to ASE as well.
	}
	
	@SuppressWarnings("unused")
	private static class ASOCApiKeyLoginRequest {
		public String KeyId;
		public String KeySecret;
	}
	
	@SuppressWarnings("unused")
	private static class ASEApiKeyLoginRequest {
		public String keyId;
		public String keySecret;
	}
	
	@SuppressWarnings("unused")
	private static class ASOCApiKeyLoginResponse {
		public String Token;
		public String Expire;
	}
	
	@SuppressWarnings("unused")
	private static class ASEApiKeyLoginResponse {
		public boolean loggedIn;
		public String sessionId;
		public String version;
		public boolean isDASTScanningEnabled;
	}

}
