/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.hcl.appscan.issuegateway.IssueGatewayConstants;
import com.hcl.appscan.issuegateway.errors.ResponseErrorHandler;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class CommentHandler implements IssueGatewayConstants{
	
	
		
	public String[] getComments(AppScanIssue issue, PushJobData jobData, List<String> errors) throws Exception{ 
		String url = jobData.getAppscanData().getUrl() + ASOC_API_COMMENT.replaceAll("ISSUEID",issue.get("Id"));
	
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ResponseErrorHandler());
		HttpHeaders headers = new HttpHeaders();
		headers.add(HEADER_AUTHORIZATION, AuthHandler.getInstance().getBearerToken(jobData,errors));
		headers.add(HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HEADER_ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<Comment[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Comment[].class);
	    if (responseEntity.getStatusCode().is2xxSuccessful()) {
	    	String[] comments = new String[responseEntity.getBody().length];
			for (int i=0; i<responseEntity.getBody().length; i++) {
				comments[i] = responseEntity.getBody()[i].Comment;
			}
			return comments;
		}
	    errors.add("An error occured retrieving issue comments. A status code of " + responseEntity.getStatusCodeValue() + " was received from " + url);
	    return new String[0];
	}
	
	public String getCommentToken() {
		return COMMENT_TOKEN;
	}

	public void submitComments(PushJobData jobData, List<String> errors, Map<String, String> results)throws Exception  {
		
		for (String issueId : results.keySet()) {
			//Only handle result entries that have a value that starts with "http" (A link to a defect)
			if (!results.get(issueId).startsWith("http")) {
				break;
			}
			
			String url = jobData.getAppscanData().getUrl() + ASOC_API_COMMENT.replaceAll("ISSUEID",issueId);
			
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new ResponseErrorHandler());
			HttpHeaders headers = new HttpHeaders();
			headers.add(HEADER_AUTHORIZATION, AuthHandler.getInstance().getBearerToken(jobData,errors));
			headers.add(HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			headers.add(HEADER_ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			Comment comment = new Comment();
			comment.Comment = getCommentToken() + " created the following issue:\n" + results.get(issueId);
			HttpEntity<Comment> entity = new HttpEntity<Comment>(comment, headers);
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			if (!responseEntity.getStatusCode().is2xxSuccessful()) {
				errors.add("An error occured adding a comment to an AppScan issue. A status code of " + responseEntity.getStatusCodeValue() + " was received from " + url);
			}
		}
	}
	
	private static class Comment {
		public String Comment;
	}
}
