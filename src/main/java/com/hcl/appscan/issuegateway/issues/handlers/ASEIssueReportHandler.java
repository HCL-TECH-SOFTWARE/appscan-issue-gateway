/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.io.Files;
import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

import com.hcl.appscan.issuegateway.CustomRestTemplateProvider;

public class ASEIssueReportHandler {
	
	private final String REST_ISSUE_DETAILS_FILE   = "/api/issues/details_v2";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	
	public void retrieveReports(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {
	
		for (AppScanIssue issue : issues) {
		    try {
			    	File reportFile = downloadReport(jobData, issue.get("id"), errors);
		    		if (reportFile != null) {
		    				issue.setIssueDetails(reportFile);
		    			}
		    		 else {
		    			errors.add("Error: Timed out waiting for issue report job to finish");
		    		}
		    	
		    }  catch (Exception e) {
					errors.add("Internal Server Error while retrieving issue reports: " + e.getMessage());
					logger.error("Internal Server Error while retrieving reports", e);
	        }
	    }
    }
	
	private File downloadReport(PushJobData jobData, String issueId, List<String> errors) throws IOException,Exception { 
		String url= jobData.getAppscanData().getUrl() + REST_ISSUE_DETAILS_FILE;
		RestTemplate restTemplate =CustomRestTemplateProvider.getCustomizedrestTemplate();
	    restTemplate.setErrorHandler(new ResponseErrorHandler());
		HttpHeaders headers = new HttpHeaders();
		headers.add("asc_xsrf_token", AuthHandler.getInstance().getBearerToken(jobData,errors));
		final List<HttpCookie> cookies=AuthHandler.getInstance().getCookies();
	 	   if (cookies != null) {
	            StringBuilder sb = new StringBuilder();
	            for (HttpCookie cookie : cookies) {
	                sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
	            }
	            headers.add("Cookie", sb.toString());
	        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
        		.path(REST_ISSUE_DETAILS_FILE)
        		.queryParam("appId", jobData.getAppscanData().getAppid())
        		.queryParam("ids","[\""+issueId+"\"]");
	        
	        URI theURI = urlBuilder.build().encode().toUri();
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(theURI, HttpMethod.GET, entity, byte[].class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			File tempFile = File.createTempFile("response", ".zip");
			File tempDir=Files.createTempDir();
			FileOutputStream stream = new FileOutputStream(tempFile);
			try {
			    stream.write(responseEntity.getBody());
			} finally {
			    stream.close();
			}
			ZipInputStream zipIn= new ZipInputStream(new FileInputStream(tempFile));
			ZipEntry entry=zipIn.getNextEntry();
			String filePath=tempDir.getPath()+File.separator+entry.getName();
			
			BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[4096];
	        int read = 0;
	        while ((read = zipIn.read(bytesIn)) != -1) {
	            bos.write(bytesIn, 0, read);
	        }
	        bos.close();
	        zipIn.closeEntry();
	        zipIn.close();
			return (new File(filePath));
		} 
		errors.add("An error occurred downloading a report. Receieved " + responseEntity.getStatusCodeValue() + " from " + url);
		return null;
	}
}