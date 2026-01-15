package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

class ASOCUtils {

	private ASOCUtils() {}

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
