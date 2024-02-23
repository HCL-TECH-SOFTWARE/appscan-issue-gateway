/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2024.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.asoc;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ASOCCommentHandler {

	private static final String REST_COMMENT = "/api/v4/Issues/ISSUEID/Comments";
	private static final String SUBMIT_COMMENT = "/api/v4/Issues/Application/APPID";
	private static final String COMMENT_TOKEN = "AppScan Issue Gateway";

	public String[] getComments(AppScanIssue issue, PushJobData jobData, List<String> errors) {
		String url = jobData.getAppscanData().getUrl() + REST_COMMENT.replace("ISSUEID", issue.get("Id"));

		RestTemplate restTemplate = ASOCUtils.createASOCRestTemplate();
		HttpHeaders headers = ASOCUtils.createASOCAuthorizedHeaders(jobData);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Comment> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Comment.class);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			Comment[] responseArray = responseEntity.getBody().Items;
			String[] comments = new String[responseArray.length];
			for (int i = 0; i < responseArray.length; i++) {
				comments[i] = responseArray[i].Comment;
			}
			return comments;
		}
		errors.add("An error occured retrieving issue comments. A status code of " + responseEntity.getStatusCodeValue()
				+ " was received from " + url);
		return new String[0];
	}

	public String getCommentToken() {
		return COMMENT_TOKEN;
	}

	public void submitComments(PushJobData jobData, List<String> errors, Map<String, String> results) {

		for (Entry<String, String> result : results.entrySet()) {
			// Only handle result entries that have a value that starts with "http" (A link to a defect)
			if (!result.getValue().startsWith("http")) {
				break;
			}

			UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(jobData.getAppscanData().getUrl())
					.path((SUBMIT_COMMENT).replace("APPID",jobData.getAppscanData().getAppid()))
					.queryParam("odataFilter", ("Id eq ISSUEID").replace("ISSUEID",result.getKey()));

			URI url = urlBuilder.build().encode().toUri();

			RestTemplate restTemplate = ASOCUtils.createASOCRestTemplate();
			HttpHeaders headers = ASOCUtils.createASOCAuthorizedHeaders(jobData);
			Comment comment = new Comment();
			comment.Comment = getCommentToken() + " created the following issue:\n" + result.getValue();
			HttpEntity<Comment> entity = new HttpEntity<>(comment, headers);
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
			if (!responseEntity.getStatusCode().is2xxSuccessful()) {
				errors.add("An error occured adding a comment to an AppScan issue. A status code of "
						+ responseEntity.getStatusCodeValue() + " was received from " + url);
			}
		}
	}

	private static class Comment {
		public String Comment;
		public Comment[] Items;
	}
}
