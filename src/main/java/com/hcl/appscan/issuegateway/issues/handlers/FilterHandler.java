/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hcl.appscan.issuegateway.AppscanProvider;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class FilterHandler {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private ExternalIdHandler externalIdHandler=new ExternalIdHandler();

	public ExternalIdHandler getExternalIdHandler() {
		return externalIdHandler;
	}

	public AppScanIssue[] filterIssues(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {
		List<AppScanIssue> filteredIssues=null ;
		try {
			if (jobData.getAppscanData().getAppscanProvider().equals(AppscanProvider.ASE.name())) {
				
				filteredIssues= includeFilterWithRegex(issues, jobData);
				filteredIssues= excludeFilterWithRegex(filteredIssues, jobData);
			}
			else {
				filteredIssues= includeFilterWithRegex(issues, jobData);
			}
			
			AppScanIssue[] finalizedIssues = filterOutPreviouslyHandledIssues(filteredIssues, jobData, errors);
			return finalizedIssues;
		} catch (Exception e) {
			errors.add("Internal Server Error while filtering issues: " + e.getMessage());
			logger.error("Internal Server Error while filtering issues", e);
		}
		//If there were any failures at all, just return an empty list. We don't want to mistakenly create issues
		return new AppScanIssue[0];
	}
	
	private List<AppScanIssue> includeFilterWithRegex(AppScanIssue[] issues, PushJobData jobData) {
		
		if ((jobData.getAppscanData().getIncludeIssuefilters()==null || jobData.getAppscanData().getIncludeIssuefilters().isEmpty())||jobData.getAppscanData().getIncludeIssuefilters().containsKey("id"))
			return Arrays.asList(issues);
		
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		
		//Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, List<Pattern>> patterns = new HashMap<String, List<Pattern>>();
		for (String field: jobData.getAppscanData().getIncludeIssuefilters().keySet()) {
			String [] values=jobData.getAppscanData().getIncludeIssuefilters().get(field).split(",");
			List<Pattern> list=new ArrayList<>();
			for (String value:values) {
				list.add(Pattern.compile(value));
			}
			patterns.put(field, list);
		}
		
		for (AppScanIssue issue : issues) {
			boolean foundMatch = false;
			second:
			for (String field: patterns.keySet()) {
				for (Pattern p :patterns.get(field)) {
					Matcher m = p.matcher(issue.get(field));
					if (m.matches()) {
						foundMatch = true;
						break second;
					}
				}
				
			}
			if (foundMatch) {
				filteredIssues.add(issue);
			}
		}
 	   
		return filteredIssues;	
	}
	
	private List<AppScanIssue> excludeFilterWithRegex(List<AppScanIssue> issues, PushJobData jobData) {
		if ((jobData.getAppscanData().getExcludeIssuefilters() == null||jobData.getAppscanData().getExcludeIssuefilters().isEmpty())||jobData.getAppscanData().getIncludeIssuefilters().containsKey("id"))
			return issues;
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		
		//Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, List<Pattern>> patterns = new HashMap<String, List<Pattern>>();
		for (String field: jobData.getAppscanData().getIncludeIssuefilters().keySet()) {
			String [] values=jobData.getAppscanData().getIncludeIssuefilters().get(field).split(",");
			List<Pattern> list=new ArrayList<>();
			for (String value:values) {
				list.add(Pattern.compile(value));
			}
			patterns.put(field, list);
		}
		
		for (AppScanIssue issue : issues) {
			boolean foundMatch = false;
			second:
			for (String field: patterns.keySet()) {
				for (Pattern p :patterns.get(field)) {
					Matcher m = p.matcher(issue.get(field));
					if (m.matches()) {
						foundMatch = true;
						break second;
					}
				}
				
			}
			if (!foundMatch) {
				filteredIssues.add(issue);
			}
		}
 	   
		return filteredIssues;	
	}
	
	private AppScanIssue[] filterOutPreviouslyHandledIssues(List<AppScanIssue> issues, PushJobData jobData, List<String> errors) throws Exception {
		
		String productId=jobData.getAppscanData().getAppscanProvider();
		if (productId.equalsIgnoreCase(AppscanProvider.ASE.name())) {
			return filterBasedOnExternalId(issues, jobData, errors);
		}
		else {
			return filterBasedOnComment(issues, jobData, errors);
		}
	}
	
	private AppScanIssue[] filterBasedOnComment(List<AppScanIssue> issues, PushJobData jobData, List<String> errors)throws Exception {
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		final int maxIssueCount=jobData.getAppscanData().getMaxissues();
		int issueCount = 0;
		CommentHandler commentHandler = new CommentHandler();
		for (AppScanIssue issue : issues) {
			boolean foundOurComment = false;
			if (shouldCheckDuplicates(jobData)) {
				for (String comment : commentHandler.getComments(issue, jobData, errors)) {
					if (comment.startsWith(commentHandler.getCommentToken())) {
						foundOurComment = true;
						break;
					}
				}
			}
			if (!foundOurComment) {
				filteredIssues.add(issue);
				issueCount++;
				if (issueCount >= maxIssueCount) {
					break;
				}
			}
		}
		
		return filteredIssues.toArray(new AppScanIssue[filteredIssues.size()]);
	}
	
	private AppScanIssue[] filterBasedOnExternalId(List<AppScanIssue> issues, PushJobData jobData, List<String> errors) throws Exception{
        List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
        final int maxIssueCount=jobData.getAppscanData().getMaxissues();
		int issueCount = 0;
		for (AppScanIssue issue : issues) {
			if (shouldCheckDuplicates(jobData)) {
				if (!externalIdHandler.isExternalIdPresent(issue, jobData, errors)) {
					filteredIssues.add(issue);
					issueCount++;
				}
					
				if (issueCount >= maxIssueCount) {
					break;
				}
			}
		}
		
		return filteredIssues.toArray(new AppScanIssue[filteredIssues.size()]);
	}
	
	private boolean shouldCheckDuplicates(PushJobData jobData) {
		if (jobData.getAppscanData().getOther() != null) {
			if (jobData.getAppscanData().getOther().get("checkduplicates") != null) {
				if (jobData.getAppscanData().getOther().get("checkduplicates").equals("false")) {
					return false;
				}
			}
		}
		return true;
	}
}