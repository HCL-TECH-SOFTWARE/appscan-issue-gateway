/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class IssueRetrievalHandler {
	
	private final String REST_ISSUES = "/api/v2/Apps/APPID/Issues";
	private final String REST_POLICY = "/api/v2/Apps/APPID/Policy";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

			
	public AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors) {

		try {
	    	RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
	    	HttpHeaders headers = new HttpHeaders();
	 	    headers.add("Authorization", AuthHandler.getInstance().getBearerToken(jobData));
	 	    headers.add("Content-Type", "application/json");
	 		headers.add("Accept", "application/json");
	 		headers.add("Accept-Language", "en-US,en;q=0.9");
	 		
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.appscanData.url)
        		.path(REST_ISSUES.replaceAll("APPID", jobData.appscanData.appid))
                .queryParam("$filter", getStateFilters(jobData.appscanData.issuestates))
	        	.queryParam("$orderby", "SeverityValue");
	        for (String policyId : getPolicyIds(jobData)) {
	        	urlBuilder.queryParam("policyId", policyId);
	       	}
	       
	        URI theURI = urlBuilder.build().encode().toUri();
	        ResponseEntity<AppScanIssue[]> response = restTemplate.exchange(theURI, HttpMethod.GET, entity, AppScanIssue[].class);
	        if (!response.getStatusCode().is2xxSuccessful()) {
	        	errors.add("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
	        	logger.error("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
	        }
	        return response.getBody();
	    }
	    catch (RestClientException e) {
			errors.add("Internal Server Error while retrieving AppScan Issues: " + e.getMessage());
			logger.error("Internal Server Error while retrieving AppScan Issues", e);
	    }
		//If we get here there were problems, so just return an empty list so nothing bad will happen
		return new AppScanIssue[0];
	}
	
	private String getStateFilters(String userInput) {
		String[] states = userInput.split(",");
		String stateFilter = "";
		for (String state : states) {
			stateFilter += "Status eq '" + state + "' or ";
		}
		if (stateFilter.length() > 1) {
			stateFilter = stateFilter.substring(0, stateFilter.length()- " or ".length());
		}
		return stateFilter;
	}

	private List<String> getPolicyIds(PushJobData jobData) {
		List<String> policyIds = new ArrayList<String>();
		//If the user passed in some policy ids, use them.  If not go figure out the application's registered policies
		if (jobData.appscanData.policyids != null) {
			for (String policyId : jobData.appscanData.policyids.split(",")) {
				policyIds.add(policyId.trim());
			}
			
		} else {
			for (String policyId : getApplicationPolicies(jobData)) {
				policyIds.add(policyId);
			}
		}
		return policyIds;
	}
	
	private List<String> getApplicationPolicies(PushJobData jobData) {
		List<String> policyIds = new ArrayList<String>();
		
    	RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ResponseErrorHandler());
    	HttpHeaders headers = new HttpHeaders();
 	    headers.add("Authorization", AuthHandler.getInstance().getBearerToken(jobData));
 	    headers.add("Content-Type", "application/json");
 		headers.add("Accept", "application/json");
 		
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.appscanData.url)
    		.path(REST_POLICY.replaceAll("APPID", jobData.appscanData.appid));
       
        ResponseEntity<Policy[]> response = restTemplate.exchange(urlBuilder.build().encode().toUri(), HttpMethod.GET, entity, Policy[].class);
        
        for (Policy policy : response.getBody()) {
        	if (policy.Enabled) {
        		policyIds.add(policy.Id);
        	}
        }
		return policyIds;
	}
	
	private static class Policy {
		public boolean Enabled;
		public String Id;
	}
}