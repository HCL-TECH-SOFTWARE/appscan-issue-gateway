/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018.
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

	private static final String REST_CREATE_REPORT = "/api/v4/Reports/Security/Application/APPID";
	private static final String REST_REPORT_STATUS = "/api/v4/Reports";
	private static final String REST_REPORT_DOWNLOAD = "/api/v4/Reports/REPORTID/Download";

	private static final Logger logger = LoggerFactory.getLogger(ASOCReportHandler.class);

	public void retrieveReports(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {

		for (AppScanIssue issue : issues) {
			try {
				// Step 1: Submit the create report request and get the report job id
				// Step 2: Wait for the report job status to be "Ready"
				// Step 3: Download the report and add it to the issue
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
			} catch (IOException e) {
				errors.add("Internal Server Error while retrieving issue reports: " + e.getMessage());
				logger.error("Internal Server Error while retrieving reports", e);
			}
		}
	}

	private String postReportJob(PushJobData jobData, String issueId, List<String> errors) {
		String url = jobData.getAppscanData().getUrl()
				+ REST_CREATE_REPORT.replace("APPID", jobData.getAppscanData().getAppid());

		RestTemplate restTemplate = ASOCUtils.createASOCRestTemplate();
		HttpHeaders headers = ASOCUtils.createASOCAuthorizedHeaders(jobData);

		CreateReportRequest createReportRequest = new CreateReportRequest();
		createReportRequest.OdataFilter = "Id eq "+ issueId +"";
		createReportRequest.Configuration = new CreateReportRequestConfiguration();

		HttpEntity<CreateReportRequest> entity = new HttpEntity<>(createReportRequest, headers);
		ResponseEntity<ReportJobResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity,
				ReportJobResponse.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return responseEntity.getBody().Id;
		}
		errors.add("An error occurred generating a report.  Received " + responseEntity.getStatusCodeValue() + " from "
				+ url);
		return null;
	}

	private boolean waitForReportJob(PushJobData jobData, String reportId) {
		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
				.path(REST_REPORT_STATUS)
				.queryParam("$filter", ("Id eq REPORTID").replace("REPORTID",reportId));

		URI theURI = urlBuilder.build().encode().toUri();
		for (long stop = System.nanoTime() + TimeUnit.MINUTES.toNanos(2); stop > System.nanoTime(); ) {
			RestTemplate restTemplate = ASOCUtils.createASOCRestTemplate();
			HttpHeaders headers = ASOCUtils.createASOCAuthorizedHeaders(jobData);

			HttpEntity<Object> entity = new HttpEntity<>(headers);
			ResponseEntity<ReportJobResponse> responseEntity = restTemplate.exchange(theURI, HttpMethod.GET, entity,
					ReportJobResponse.class);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				ReportJobResponse[] responseArray = responseEntity.getBody().Items;
				ReportJobResponse response = responseArray[0];
				if (response.Status.equals("Ready")) {
					return true;
				}
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
		String url = jobData.getAppscanData().getUrl() + REST_REPORT_DOWNLOAD.replace("REPORTID", reportId);

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new ByteArrayHttpMessageConverter());

		RestTemplate restTemplate = new RestTemplate(messageConverters);
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
			return tempFile;
		}
		errors.add("An error occurred downloading a report. Receieved " + responseEntity.getStatusCodeValue() + " " +
				"from "
				+ url);
		return null;
	}

	@SuppressWarnings("unused")
	private static class CreateReportRequest {
		public String OdataFilter;
		public String[] PolicyIds = new String[]{"00000000-0000-0000-0000-000000000000"};
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
		// public String Notes = "";
		public boolean IsTrialReport = false;
		public String RegulationReportType = "None";
		public String ReportFileType = "Html";
	}

	@SuppressWarnings("unused")
	private static class ReportJobResponse {
		public String Id;
		public String Status;
		public String Message;
		public String Key;
		public ReportJobResponse[] Items;
		// public Integer Progress;
	}
}