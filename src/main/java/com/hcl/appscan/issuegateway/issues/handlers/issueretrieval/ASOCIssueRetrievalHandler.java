package com.hcl.appscan.issuegateway.issues.handlers.issueretrieval;

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
import com.hcl.appscan.issuegateway.issues.handlers.auth.ASOCAuthHandler;

public class ASOCIssueRetrievalHandler extends IssueRetrievalHandler {
	private final String ASOC_API_ISSUES = "/api/v2/Apps/APPID/Issues";
	private final String ASOC_API_POLICY = "/api/v2/Apps/APPID/Policy";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static class Policy {
		public boolean Enabled;
		public String Id;
	}
	
	public AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors) throws Exception{

		try {
	    	RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
	    	HttpHeaders headers = new HttpHeaders();
	 	    headers.add("Authorization", ASOCAuthHandler.getInstance().getBearerToken(jobData,errors));
	 	    setHeaders(headers);	 		
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
        		.path(ASOC_API_ISSUES.replaceAll("APPID", jobData.getAppscanData().getAppid()))
                .queryParam("$filter", getStateFilters(jobData.getAppscanData().getIssuestates()))
	        	.queryParam("$orderby", "SeverityValue");
	        for (String policyId : getPolicyIds(jobData,errors)) {
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
	
	private List<String> getPolicyIds(PushJobData jobData,List<String> errors) throws Exception{
		List<String> policyIds = new ArrayList<String>();
		//If the user passed in some policy ids, use them.  If not go figure out the application's registered policies
		if (jobData.getAppscanData().getPolicyids() != null) {
			for (String policyId : jobData.getAppscanData().getPolicyids().split(",")) {
				policyIds.add(policyId.trim());
			}
			
		} else {
			for (String policyId : getApplicationPolicies(jobData,errors)) {
				policyIds.add(policyId);
			}
		}
		return policyIds;
	}
	
	private List<String> getApplicationPolicies(PushJobData jobData,List<String> errors) throws Exception{
		List<String> policyIds = new ArrayList<String>();
		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
	    	HttpHeaders headers = new HttpHeaders();
	 	    headers.add("Authorization", ASOCAuthHandler.getInstance().getBearerToken(jobData,errors));
	 	    setHeaders(headers);
	 		
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
	    		.path(ASOC_API_POLICY.replaceAll("APPID", jobData.getAppscanData().getAppid()));
	       
	        ResponseEntity<Policy[]> response = restTemplate.exchange(urlBuilder.build().encode().toUri(), HttpMethod.GET, entity, Policy[].class);
	        
	        for (Policy policy : response.getBody()) {
	        	if (policy.Enabled) {
	        		policyIds.add(policy.Id);
	        	}
	        }
		}
		catch ( RestClientException e) {
			errors.add("Internal Server Error while retrieving AppScan Issues: " + e.getMessage());
			logger.error("Internal Server Error while retrieving AppScan Issues", e);
	    }
    	
		return policyIds;
	}
}
