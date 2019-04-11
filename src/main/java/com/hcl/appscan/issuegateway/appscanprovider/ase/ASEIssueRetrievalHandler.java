package com.hcl.appscan.issuegateway.appscanprovider.ase;

import static com.hcl.appscan.issuegateway.appscanprovider.ase.ASEConstants.HEADER_ASC_XSRF_TOKEN;

import java.net.HttpCookie;
import java.net.URI;
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

import com.hcl.appscan.issuegateway.appscanprovider.IIssueRetrievalHandler;
import com.hcl.appscan.issuegateway.errors.EntityNotFoundException;
import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class ASEIssueRetrievalHandler implements IIssueRetrievalHandler {
	
	private final String ASE_API_ISSUES = "/api/issues";
	private final String ASE_API_APPLICATION_DETAILS="/api/applications/APPLICATIONID";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors)  throws Exception{
		RestTemplate restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
		try {
			restTemplate.setErrorHandler(new ResponseErrorHandler());
	    	HttpHeaders headers = new HttpHeaders();
	 	    headers.add(HEADER_ASC_XSRF_TOKEN, ASEAuthHandler.getInstance().getBearerToken(jobData));
			headers.add("Content-Type", "application/json");
			headers.add("Accept", "application/json");
			headers.add("Accept-Language", "en-US,en;q=0.9");
	 		
	 		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl());
	        urlBuilder.path(ASE_API_ISSUES)
  			.queryParam("query", "Application Name="+getApplicationName(jobData,errors)+","+getIssueFilters(jobData))
  			.queryParam("compactResponse", "false");
	        headers.add("Range", "items=0-10000");
	        
	        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
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
	
	private String getApplicationName(PushJobData jobData,List<String> errors) throws EntityNotFoundException ,Exception{
		ResponseEntity<ApplicationName> response=null;
		try {
			RestTemplate restTemplate = CustomRestTemplateProvider.getCustomizedrestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
			HttpHeaders headers=new HttpHeaders();
			headers.add(HEADER_ASC_XSRF_TOKEN, ASEAuthHandler.getInstance().getBearerToken(jobData));
			final List<HttpCookie> cookies=ASEAuthHandler.getInstance().getCookies();
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
			if (jobData.getAppscanData().getIssuestates()!=null && jobData.getAppscanData().getIssuestates()!="") {
				String[] states = jobData.getAppscanData().getIssuestates().split(",");
				
				for (String state : states ) {
					issueFilters +="Status=" +state+",";
				}
			}
			
			if (filters.containsKey("Severity") && filters.get("Severity")!=null ) {
				String severityValuesString=filters.get("Severity");
				String[] severityValues = severityValuesString.split(",");
				for (String value: severityValues) {
					issueFilters=issueFilters+"Severity="+value+",";
				}
			}
			
			if (filters.containsKey("Issue Type") && (filters.get("Issue Type")!=null)) {
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
	private static class ApplicationName{
		public String name;
	}
}
