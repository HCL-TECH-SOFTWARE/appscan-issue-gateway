/**
 * © Copyright HCL Technologies Ltd. 2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import com.hcl.appscan.issuegateway.appscanprovider.IIssueRetrievalHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ASOCIssueRetrievalHandler implements IIssueRetrievalHandler {

	private static final String REST_ISSUES = "/api/v2/Apps/APPID/Issues";
	private static final String REST_POLICY = "/api/v2/Apps/APPID/Policy";
	private static final Logger logger = LoggerFactory.getLogger(ASOCIssueRetrievalHandler.class);

	@Override
	public AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors) {

		try {
			RestTemplate restTemplate = ASOCUtils.createASOCRestTemplate();
			HttpHeaders headers = ASOCUtils.createASOCAuthorizedHeaders(jobData);
			headers.add("Accept-Language", "en-US,en;q=0.9");
			HttpEntity<Object> entity = new HttpEntity<>(headers);

			UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
					.path(REST_ISSUES.replace("APPID", jobData.getAppscanData().getAppid()))
					.queryParam("$filter", getStateFilters(jobData.getAppscanData().getIssuestates()))
					.queryParam("$orderby", "SeverityValue");
			for (String policyId : getPolicyIds(jobData)) {
				urlBuilder.queryParam("policyId", policyId);
			}

			URI theURI = urlBuilder.build().encode().toUri();
			ResponseEntity<AppScanIssue[]> response = restTemplate.exchange(theURI, HttpMethod.GET, entity,
					AppScanIssue[].class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				errors.add("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
				logger.error("Error: Receieved a " + response.getStatusCodeValue() + " status code from " + theURI);
			}
			return response.getBody();
		} catch (RestClientException e) {
			errors.add("Internal Server Error while retrieving AppScan Issues: " + e.getMessage());
			logger.error("Internal Server Error while retrieving AppScan Issues", e);
		}
		// If we get here there were problems, so just return an empty list so nothing
		// bad will happen
		return new AppScanIssue[0];
	}

	private String getStateFilters(String userInput) {
		String[] states = userInput.split(",");
		StringBuilder stateFilterBuilder = new StringBuilder();
		for (String state : states) {
			stateFilterBuilder.append("Status eq '")
					.append(state)
					.append("' or ");
		}
		String stateFilter = stateFilterBuilder.toString();
		if (stateFilter.length() > 1) {
			stateFilter = stateFilter.substring(0, stateFilter.length() - " or ".length());
		}
		return stateFilter;
	}

	private List<String> getPolicyIds(PushJobData jobData) {
		List<String> policyIds = new ArrayList<>();
		// If the user passed in some policy ids, use them. If not go figure out the
		// application's registered policies
		if (jobData.getAppscanData().getPolicyids() != null) {
			for (String policyId : jobData.getAppscanData().getPolicyids().split(",")) {
				policyIds.add(policyId.trim());
			}

		} else {
			policyIds.addAll(getApplicationPolicies(jobData));
		}
		return policyIds;
	}

	private List<String> getApplicationPolicies(PushJobData jobData) {
		List<String> policyIds = new ArrayList<>();

		RestTemplate restTemplate = ASOCUtils.createASOCRestTemplate();
		HttpHeaders headers = ASOCUtils.createASOCAuthorizedHeaders(jobData);

		HttpEntity<Object> entity = new HttpEntity<>(headers);
		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
				.path(REST_POLICY.replace("APPID", jobData.getAppscanData().getAppid()));

		ResponseEntity<Policy[]> response = restTemplate.exchange(urlBuilder.build().encode().toUri(), HttpMethod.GET,
				entity, Policy[].class);

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
