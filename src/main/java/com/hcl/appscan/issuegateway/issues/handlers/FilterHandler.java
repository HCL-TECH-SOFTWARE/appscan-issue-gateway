/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class FilterHandler {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public AppScanIssue[] filterIssues(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {
		try {
			List<AppScanIssue> filteredIssues = filterWithRegex(issues, jobData);
			AppScanIssue[] finalizedIssues = filterOutPreviouslyHandledIssues(filteredIssues, jobData, errors);
			return finalizedIssues;
		} catch (Exception e) {
			errors.add("Internal Server Error while filtering issues: " + e.getMessage());
			logger.error("Internal Server Error while filtering issues", e);
		}
		//If there were any failures at all, just return an empty list. We don't want to mistakenly create issues
		return new AppScanIssue[0];
	}
	
	private List<AppScanIssue> filterWithRegex(AppScanIssue[] issues, PushJobData jobData) {
		
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		
	
		//Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		if (jobData.appscanData.issuefilters != null) {
			for (String field: jobData.appscanData.issuefilters.keySet()) {
				patterns.put(field, Pattern.compile(jobData.appscanData.issuefilters.get(field)));
			}
		}
		
		for (AppScanIssue issue : issues) {
			boolean foundMatch = false;
			for (String field: patterns.keySet()) {
				Matcher m = patterns.get(field).matcher(issue.get(field));
				if (m.matches()) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) {
				filteredIssues.add(issue);
			}
		}
 	   
		return filteredIssues;	
	}
	
	private AppScanIssue[] filterOutPreviouslyHandledIssues(List<AppScanIssue> issues, PushJobData jobData, List<String> errors) {
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		
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
				if (issueCount >= jobData.appscanData.maxissues) {
					break;
				}
			}
		}
		
		return filteredIssues.toArray(new AppScanIssue[filteredIssues.size()]);
	}
	
	private boolean shouldCheckDuplicates(PushJobData jobData) {
		if (jobData.appscanData.other != null) {
			if (jobData.appscanData.other.get("checkduplicates") != null) {
				if (jobData.appscanData.other.get("checkduplicates").equals("false")) {
					return false;
				}
			}
		}
		return true;
	}
}