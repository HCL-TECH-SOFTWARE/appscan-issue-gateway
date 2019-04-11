package com.hcl.appscan.issuegateway.issues.handlers.auth;

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
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class ASEAuthHandler implements IssueGatewayConstants {
	private static ASEAuthHandler authHandler;
	private final List<HttpCookie> cookies = new ArrayList<>();
	
	public static synchronized ASEAuthHandler getInstance() {
		if (authHandler == null) {
			authHandler = new ASEAuthHandler();
		}
		return authHandler;
	}
	
	public List<HttpCookie> getCookies(){
		return cookies;
	}
	
	protected static Map<String, String> bearerTokens = Collections
			.synchronizedMap(new PassiveExpiringMap<String, String>(1000 * 60 * 60 * 3)); // 3 hour expiry

	public String getBearerToken(PushJobData jobData) throws Exception {
		String url = jobData.getAppscanData().getUrl();
		String apikeyid = jobData.getAppscanData().getApikeyid();
		String apikeysecret = jobData.getAppscanData().getApikeysecret();

		String key = url + apikeyid; // keys are the url + apikeyid to support multiple servers and users

		String bearerToken = bearerTokens.get(key);
		if ((bearerToken != null) && isStillValid(url, bearerToken)) {
			return bearerToken;
		}
		String newBearerToken = authenticate(url, apikeyid, apikeysecret);
		bearerTokens.put(key, newBearerToken);
		return newBearerToken;
	}

	private boolean isStillValid(String url, String bearerToken) {
		try {
			RestTemplate restTemplate = getRestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.add(getAuthorizationHeaderName(), bearerToken);
			HttpEntity<Object> entity = new HttpEntity<Object>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url + getValidationAPI(), HttpMethod.GET,
					entity, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				return true;
			}
		} catch (RestClientException e) {
			// Will return false below
		}
		return false;
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
	
	protected RestTemplate getRestTemplate() {
		return CustomRestTemplateProvider.getCustomizedrestTemplate();
	}

	protected String authenticate(String url, String apikeyid, String apikeysecret) throws Exception{
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

	protected String getAuthorizationHeaderName() {
		return HEADER_ASC_XSRF_TOKEN;
	}

	protected String getValidationAPI() {
		return ASE_API_APPS_COUNT;
	}
	
	@SuppressWarnings("unused")
	private static class ASEApiKeyLoginRequest {
		public String keyId;
		public String keySecret;
	}
	
	@SuppressWarnings("unused")
	private static class ASEApiKeyLoginResponse {
		public boolean loggedIn;
		public String sessionId;
		public String version;
		public boolean isDASTScanningEnabled;
	}
}
