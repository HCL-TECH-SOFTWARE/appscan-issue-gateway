package com.hcl.appscan.issuegateway.issues.handlers.auth;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.CustomRestTemplateProvider;
import com.hcl.appscan.issuegateway.IssueGatewayConstants;

public class ASEAuthHandler extends AuthHandler implements IssueGatewayConstants {
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

	@Override
	protected RestTemplate getRestTemplate() {
		return CustomRestTemplateProvider.getCustomizedrestTemplate();
	}


	@Override
	protected String authenticate(String url, String apikeyid, String apikeysecret) {
		try {
			return getSessionId(url, apikeyid, apikeysecret);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; // TODO
	}


	@Override
	protected String getAuthorizationHeaderName() {
		return HEADER_ASC_XSRF_TOKEN;
	}


	@Override
	protected String getValidationAPI() {
		return ASE_API_APPS_COUNT;
	}
}
