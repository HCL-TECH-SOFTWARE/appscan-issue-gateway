/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.applications;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.hcl.appscan.issuegateway.errors.EntityNotFoundException;
import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.handlers.AuthHandler;

import com.hcl.appscan.issuegateway.CustomRestTemplateProvider;
import com.hcl.appscan.issuegateway.IssueGatewayConstants;
import com.hcl.appscan.issuegateway.AppscanProvider;

public class ApplicationsRetrievalHandler implements IssueGatewayConstants {
	
	public ResponseEntity<String> retrieveApplicationList(GetApplicationData jobData, List<String> errors)throws EntityNotFoundException,Exception{
		String productId=jobData.getAppscanProvider();
		
		if(!isASE(productId)&& !isASOC(productId))
			throw new EntityNotFoundException(AppscanProvider.class,productId,"appscanProvider is not correct");
		RestTemplate restTemplate;
		if (isASE(productId))
			restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
		else 
			restTemplate=new RestTemplate();
	    	
		restTemplate.setErrorHandler(new ResponseErrorHandler());
	    HttpHeaders headers = new HttpHeaders();
	    AuthHandler authHandler=AuthHandler.getInstance();
	 	headers.add(getAuthorizationHeaderName(productId), authHandler.getBearerToken(jobData));
	 	final List<HttpCookie> cookies=authHandler.getCookies();
	 	    
	 	if (isASE(productId) && cookies != null) {
	        StringBuilder sb = new StringBuilder();
	        for (HttpCookie cookie : cookies) {
	        	 sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
	        }
	        headers.add("Cookie", sb.toString());
	    }
	 	headers.add(HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
	 	headers.add(HEADER_ACCEPT, MediaType.APPLICATION_JSON_VALUE);
	 	headers.add("Accept-Language", "en-US,en;q=0.9");
	 		
	    HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	    UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getUrl())
        	.path(getApplicationsAPI(productId));
	    if (isASE(productId)) {
	    	urlBuilder.queryParam("columns", "name");
	    	if (jobData.getTag()!=null && !jobData.getTag().isEmpty())
		       urlBuilder.queryParam("query", "tags="+jobData.getTag());
	    }
        if (isASOC(productId))
        	urlBuilder.queryParam("$select", "Id,Name");
	    URI theURI = urlBuilder.build().encode().toUri();
	    ResponseEntity<String> response = restTemplate.exchange(theURI, HttpMethod.GET, entity, String.class);
	    if (!response.getStatusCode().is2xxSuccessful()) {
	    	throw new Exception("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
	    }
	    return response;
	}
	
	private String getAuthorizationHeaderName(String productId) {
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.toString()))
			return HEADER_ASC_XSRF_TOKEN;
		else 
			return HEADER_AUTHORIZATION;
	}
	
	private String getApplicationsAPI(String productId) {
		if (productId.equals(AppscanProvider.ASE.toString())) {
			return ASE_API_APPLICATIONS;
		}
		else  {
			return ASOC_API_APPLICATIONS;
		}
	}
	
	private boolean isASE(String productId) {
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.name()))
			return true;
		return false;
	}
	
	private boolean isASOC(String productId) {
		if (productId.equalsIgnoreCase(AppscanProvider.ASOC.name()))
			return true;
		return false;
	}
}
