/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.hcl.appscan.issuegateway.CustomRestTemplateProvider;
import com.hcl.appscan.issuegateway.IssueGatewayConstants;
import com.hcl.appscan.issuegateway.AppscanProvider;
import com.hcl.appscan.issuegateway.errors.EntityNotFoundException;
import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class IssueRetrievalHandler implements IssueGatewayConstants{
	
	private final String ASOC_API_ISSUES = "/api/v2/Apps/APPID/Issues";
	private final String ASOC_API_POLICY = "/api/v2/Apps/APPID/Policy";
	
	private final String ASE_API_ISSUES = "/api/issues";
	private final String ASE_API_APPLICATION_DETAILS="/api/applications/APPLICATIONID";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

			
	public AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors)  throws EntityNotFoundException,Exception{
		String productId=jobData.getAppscanData().getAppscanProvider();
		if(!productId.equalsIgnoreCase(AppscanProvider.ASE.name()) && !productId.equalsIgnoreCase(AppscanProvider.ASOC.name()))
			throw new EntityNotFoundException(AppscanProvider.class,productId,"appscanProvider is not found ");
		RestTemplate restTemplate;
		try {
			if (productId.equalsIgnoreCase(AppscanProvider.ASE.name()))
				restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
			else 
				restTemplate=new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
	    	HttpHeaders headers = new HttpHeaders();
	 	    headers.add(getAuthorizationHeaderName(productId), AuthHandler.getInstance().getBearerToken(jobData, errors));
	 	    headers.add("Content-Type", "application/json");
	 		headers.add("Accept", "application/json");
	 		headers.add("Accept-Language", "en-US,en;q=0.9");
	 		
	 		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl());
	        	if (productId.equalsIgnoreCase(AppscanProvider.ASE.name())) {
	        		urlBuilder.path(ASE_API_ISSUES)
  				  	.queryParam("query", "Application Name="+getApplicationName(jobData,errors)+","+getIssueFilters(jobData))
  				  	.queryParam("compactResponse", "false");
	        		headers.add("Range", "items=0-10000");
	        	}
	        	else {
	        		urlBuilder.path(ASOC_API_ISSUES.replaceAll("APPID", jobData.getAppscanData().getAppid()))
	        		.queryParam("$filter", getStateFilters(jobData.getAppscanData().getIssuestates()))
		        	.queryParam("$orderby", "SeverityValue");
		        for (String policyId : getPolicyIds(jobData,errors)) {
		        	urlBuilder.queryParam("policyId", policyId);
		       		}
	        	}
	        	
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
	        URI theURI = urlBuilder.build().encode().toUri();
	        ResponseEntity<AppScanIssue[]> response = restTemplate.exchange(theURI, HttpMethod.GET, entity, AppScanIssue[].class);
	        if (!response.getStatusCode().is2xxSuccessful()) {
	        	errors.add("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
	        	logger.error("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
	        }
	        return response.getBody();
	    }
	    catch ( RestClientException e) {
			errors.add("Internal Server Error while retrieving AppScan Issues: " + e.getMessage());
			logger.error("Internal Server Error while retrieving AppScan Issues", e);
	    }
		
		//If we get here there were problems, so just return an empty list so nothing bad will happen
		return new AppScanIssue[0];
	}
	
	private String getAuthorizationHeaderName(String productId) {
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.toString()))
			return HEADER_ASC_XSRF_TOKEN;
		else 
			return HEADER_AUTHORIZATION;
	}
	private String getApplicationName(PushJobData jobData,List<String> errors) throws EntityNotFoundException ,Exception{
		ResponseEntity<ApplicationName> response=null;
		try {
			RestTemplate restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
			   restTemplate.setErrorHandler(new ResponseErrorHandler());
			   HttpHeaders headers=new HttpHeaders();
			   headers.add("asc_xsrf_token", AuthHandler.getInstance().getBearerToken(jobData,errors));
			   final List<HttpCookie> cookies=AuthHandler.getInstance().getCookies();
		 	   if (cookies != null) {
		            StringBuilder sb = new StringBuilder();
		            for (HttpCookie cookie : cookies) {
		                sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
		            }
		            headers.add("Cookie", sb.toString());
		        }
		 	   headers.add("Content-Type", "application/json");
		 	   headers.add("Accept", "application/json");
		 	   headers.add("Accept-Language", "en-US,en;q=0.9");
		 	   
		 	   HttpEntity<Object> entity=new HttpEntity<>(headers);
		 	   String url=jobData.getAppscanData().getUrl()+ASE_API_APPLICATION_DETAILS.replaceAll("APPLICATIONID",jobData.getAppscanData().getAppid() );
		 	   response=restTemplate.exchange(url, HttpMethod.GET, entity, ApplicationName.class);
		 	   if (response.getStatusCode()==HttpStatus.NOT_FOUND) {
		 		   throw new EntityNotFoundException(PushJobData.class, jobData.getAppscanData().getAppid(),"application not found");
		 	   }
		}
		
		catch (RestClientException e) {
			errors.add("Internal Server Error while retrieving AppScan Issues: " + e.getMessage());
			logger.error("Internal Server Error while retrieving AppScan Issues", e);
	    }
	 	return response.getBody().name;   
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
	
	/*private String getStatusFilters(String userInput) {
		String[] states = userInput.split(",");
		String statusFilter = "";
		for (String state : states) {
			statusFilter +="Status=" +state+",";
		}
		if (statusFilter.length() > 1) {
			statusFilter = statusFilter.substring(0, statusFilter.length()- ",".length());
		}
		return statusFilter;
	}*/
	
	private String getIssueFilters(PushJobData jobData) {
		Map<String, String> filters=jobData.getAppscanData().getIncludeIssuefilters();
		String issueFilters="";
		if (filters.containsKey("id") && filters.get("id")!=null) {
			String idValuesString=filters.get("id");
			String [] idValues=idValuesString.split(",");
			for (String value: idValues) {
				issueFilters=issueFilters+"id="+value+",";
			}
			
		}
		else {
			String[] states = jobData.getAppscanData().getIssuestates().split(",");
			for (String state : states) {
				issueFilters +="Status=" +state+",";
			}
			
			if (filters.containsKey("Severity") && filters.get("Severity")!=null) {
				String severityValuesString=filters.get("Severity");
				String[] severityValues = severityValuesString.split(",");
				for (String value: severityValues) {
					issueFilters=issueFilters+"Severity="+value+",";
				}
			}
			
			if (filters.containsKey("Issue Type") && filters.get("Issue Type")!=null) {
				String severityValuesString=filters.get("Issue Type");
				String[] severityValues = severityValuesString.split(",");
				for (String value: severityValues) {
					issueFilters=issueFilters+"Issue Type="+value+",";
				}
			}
			
		}
		
		
		if (issueFilters.length()>1) {
			issueFilters=issueFilters.substring(0, issueFilters.length()- ",".length());
		}
		return issueFilters;
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
	 	    headers.add("Authorization", AuthHandler.getInstance().getBearerToken(jobData,errors));
	 	    headers.add("Content-Type", "application/json");
	 		headers.add("Accept", "application/json");
	 		
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
	
	private static class Policy {
		public boolean Enabled;
		public String Id;
	}
	private static class ApplicationName{
		public String name;
	}
}