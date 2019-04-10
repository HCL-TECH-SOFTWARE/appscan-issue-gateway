package com.hcl.appscan.issuegateway.issues.handlers.auth;

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

public abstract class AuthHandler {

	protected static Map<String, String> bearerTokens = Collections
			.synchronizedMap(new PassiveExpiringMap<String, String>(1000 * 60 * 60 * 3)); // 3 hour expiry

	public String getBearerToken(PushJobData jobData) {
		String url = jobData.getAppscanData().getUrl();
		String apikeyid = jobData.getAppscanData().getApikeyid();
		String apikeysecret = jobData.getAppscanData().getApikeysecret();

		String key = url + apikeyid; // keys are the url + apikeyid to support multiple servers and users

		String bearerToken = bearerTokens.get(key);
		if ((bearerToken != null) && isStillValid(url, bearerToken)) {
			return bearerToken;
		}
		// TODO: Handle error
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
			ResponseEntity<AppsCountResponse> response = restTemplate.exchange(url + getValidationAPI(), HttpMethod.GET,
					entity, AppsCountResponse.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				return true;
			}
		} catch (RestClientException e) {
			// Will return false below
		}
		return false;
	}

	protected abstract RestTemplate getRestTemplate();

	protected abstract String authenticate(String url, String apikeyid, String apikeysecret);

	protected abstract String getAuthorizationHeaderName();

	protected abstract String getValidationAPI();

	private static class AppsCountResponse {
		@SuppressWarnings("unused")
		public String Total;
	}
}
