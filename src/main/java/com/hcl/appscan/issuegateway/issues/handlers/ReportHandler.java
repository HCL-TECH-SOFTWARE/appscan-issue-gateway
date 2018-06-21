/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class ReportHandler {
	
	private final String REST_CREATEREPORT = "/api/v2/Apps/APPID/Issues/CreateReport";
	private final String REST_REPORTJOBS   = "/api/v2/Issues/ReportJobs/REPORTID";
	private final String REST_REPORTS      = "/api/v2/Issues/Reports/REPORTID";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	public void retrieveReports(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {
	
		for (AppScanIssue issue : issues) {
		    try {
			    //Step 1: Submit the create report request and get the report job id
				//Step 2: Wait for the report job status to be "Ready"
				//Step 3: Download the report and add it to the issue		    	
		    	String reportId = postReportJob(jobData, issue.get("Id"), errors);
		    	if ((reportId != null) && reportId.length() > 1) {
		    		if (waitForReportJob(jobData, reportId)) {
		    			File reportFile = downloadReport(jobData, reportId, errors);
		    			if (reportFile != null) {
		    				issue.setIssueDetails(reportFile);
		    			}
		    		} else {
		    			errors.add("Error: Timed out waiting for issue report job to finish");
		    		}
		    	}
		    }  catch (Exception e) {
					errors.add("Internal Server Error while retrieving issue reports: " + e.getMessage());
					logger.error("Internal Server Error while retrieving reports", e);
	        }
	    }
    }
	
	private String postReportJob(PushJobData jobData, String issueId, List<String> errors) { 
		String url = jobData.appscanData.url + REST_CREATEREPORT.replaceAll("APPID",jobData.appscanData.appid);
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ResponseErrorHandler());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", AuthHandler.getInstance().getBearerToken(jobData));
		headers.add("Content-Type", "application/json");
		headers.add("Accept", "application/json");	
		CreateReportRequest createReportRequest = new CreateReportRequest();
		createReportRequest.OdataFilter = "Id eq '" + issueId + "'";
		createReportRequest.Configuration = new CreateReportRequestConfiguration();
	
		HttpEntity<CreateReportRequest> entity = new HttpEntity<CreateReportRequest>(createReportRequest,headers);
		ResponseEntity<ReportJobResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, ReportJobResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
		  	return responseEntity.getBody().Id;
		}
		errors.add("An error occurred generating a report.  Received " + responseEntity.getStatusCodeValue() + " from " + url);
		return null;
	}
	
	private Boolean waitForReportJob(PushJobData jobData, String reportId) { 
		String url = jobData.appscanData.url + REST_REPORTJOBS.replaceAll("REPORTID",reportId);
	
		for (long stop=System.nanoTime()+TimeUnit.MINUTES.toNanos(2);stop>System.nanoTime();) {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", AuthHandler.getInstance().getBearerToken(jobData));
			headers.add("Content-Type", "application/json");
			headers.add("Accept", "application/json");	
			HttpEntity<Object> entity = new HttpEntity<Object>(headers);
			ResponseEntity<ReportJobResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ReportJobResponse.class);
			if (responseEntity.getBody().Status.equals("Ready")) {
				return true;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// Eat it
			}
		}
		return false;
	}
	
	private File downloadReport(PushJobData jobData, String reportId, List<String> errors) throws IOException { 
		String url          = jobData.appscanData.url + REST_REPORTS.replaceAll("REPORTID",reportId);

	    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
	    messageConverters.add(new ByteArrayHttpMessageConverter());
		RestTemplate restTemplate = new RestTemplate(messageConverters);
		restTemplate.setErrorHandler(new ResponseErrorHandler());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", AuthHandler.getInstance().getBearerToken(jobData));
        HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class, "1");
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			File tempFile = File.createTempFile("appscan", ".html");
			FileOutputStream stream = new FileOutputStream(tempFile);
			try {
			    stream.write(responseEntity.getBody());
			} finally {
			    stream.close();
			}
			return tempFile;
		} 
		errors.add("An error occurred downloading a report. Receieved " + responseEntity.getStatusCodeValue() + " from " + url);
		return null;
	}
	
	@SuppressWarnings("unused")
	private static class CreateReportRequest {
		public String OdataFilter;
		public String[] PolicyIds = new String[] {""};
		public CreateReportRequestConfiguration Configuration;
	}
	
	@SuppressWarnings("unused")
	private static class CreateReportRequestConfiguration {
		public boolean Summary = true;
		public boolean Details = true;
		public boolean Discussion = true;
		public boolean Overview = true;
		public boolean TableOfContent = false;
		public boolean Advisories = true;
		public boolean FixRecommendation = true;
		public boolean History = true;
		public String Title = "Single Issue Report";
		//public String Notes = "";
		public boolean IsTrialReport = false;
		public String  RegulationReportType = "None";
		public String  ReportFileType = "Html";
	}
	
	@SuppressWarnings("unused")
	private static class ReportJobResponse {
		public String Id;
		public String Status;
		//public Integer Progress;
	}
}