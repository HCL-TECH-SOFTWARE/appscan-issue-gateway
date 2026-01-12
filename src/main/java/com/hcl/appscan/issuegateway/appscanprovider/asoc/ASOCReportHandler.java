/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018, 2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ASOCReportHandler {
	private static final String REST_ISSUE_DETAIL = "/api/v4/Issues/ISSUEID/Details?locale=en-US";
	private static final Logger logger = LoggerFactory.getLogger(ASOCReportHandler.class);

	public void retrieveReports(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {

		for (AppScanIssue issue : issues) {
			try {
				String url = jobData.getAppscanData().getUrl()
						+REST_ISSUE_DETAIL.replace("ISSUEID",(String)issue.get("Id"));

				List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
				messageConverters.add(new ByteArrayHttpMessageConverter());
				RestTemplate restTemplate;
				if(jobData.getAppscanData().getTrusted().equalsIgnoreCase("false")){
					restTemplate = ASOCUtils.createUntrustedASOCRestTemplate();
				}else{
					restTemplate = ASOCUtils.createASOCRestTemplate();
				}
				restTemplate.setMessageConverters(messageConverters);
				restTemplate.setErrorHandler(new ResponseErrorHandler());

				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", ASOCAuthHandler.getInstance().getBearerToken(jobData));
				HttpEntity<String> entity = new HttpEntity<>(headers);

				ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class, "1");
				if (responseEntity.getStatusCode().is2xxSuccessful()) {
					File tempFile = File.createTempFile("appscan", ".html");
					try (FileOutputStream stream = new FileOutputStream(tempFile)) {
						stream.write(responseEntity.getBody());
					}
					issue.setIssueDetails(tempFile);
				}
			} catch (IOException e) {
				errors.add("Internal Server Error while retrieving issue reports: " + e.getMessage());
				logger.error("Internal Server Error while retrieving reports", e);
			}
		}
	}

}