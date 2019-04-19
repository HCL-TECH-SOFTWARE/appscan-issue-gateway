/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public abstract class FilterHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public AppScanIssue[] filterIssues(AppScanIssue[] issues, PushJobData jobData, List<String> errors) {
		List<AppScanIssue> filteredIssues = null;
		try {
			filteredIssues = includeFilterWithRegex(issues, jobData);
			filteredIssues = excludeFilterWithRegex(filteredIssues, jobData);
			AppScanIssue[] finalizedIssues = filterOutPreviouslyHandledIssues(filteredIssues, jobData, errors);
			return finalizedIssues;
		} catch (Exception e) {
			errors.add("Internal Server Error while filtering issues: " + e.getMessage());
			logger.error("Internal Server Error while filtering issues", e);
		}
		// If there were any failures at all, just return an empty list. We don't want
		// to mistakenly create issues
		return new AppScanIssue[0];
	}

	private List<AppScanIssue> includeFilterWithRegex(AppScanIssue[] issues, PushJobData jobData) {

		if ((jobData.getAppscanData().getIncludeIssuefilters() == null
				|| jobData.getAppscanData().getIncludeIssuefilters().isEmpty())
				|| jobData.getAppscanData().getIncludeIssuefilters().containsKey("id"))
			return Arrays.asList(issues);

		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();

		// Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, List<Pattern>> patterns = new HashMap<String, List<Pattern>>();
		for (String field : jobData.getAppscanData().getIncludeIssuefilters().keySet()) {
			String[] values = jobData.getAppscanData().getIncludeIssuefilters().get(field).split(",");
			List<Pattern> list = new ArrayList<>();
			for (String value : values) {
				list.add(Pattern.compile(value));
			}
			patterns.put(field, list);
		}

		for (AppScanIssue issue : issues) {
			boolean foundMatch = false;
			second: for (String field : patterns.keySet()) {
				for (Pattern p : patterns.get(field)) {
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
		if ((jobData.getAppscanData().getExcludeIssuefilters() == null
				|| jobData.getAppscanData().getExcludeIssuefilters().isEmpty())
				|| (jobData.getAppscanData().getIncludeIssuefilters()!=null && jobData.getAppscanData().getIncludeIssuefilters().containsKey("id")))
			return issues;
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();

		// Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, List<Pattern>> patterns = new HashMap<String, List<Pattern>>();
		for (String field : jobData.getAppscanData().getExcludeIssuefilters().keySet()) {
			String[] values = jobData.getAppscanData().getExcludeIssuefilters().get(field).split(",");
			List<Pattern> list = new ArrayList<>();
			for (String value : values) {
				list.add(Pattern.compile(value));
			}
			patterns.put(field, list);
		}

		for (AppScanIssue issue : issues) {
			boolean foundMatch = false;
			second: for (String field : patterns.keySet()) {
				for (Pattern p : patterns.get(field)) {
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

	protected abstract AppScanIssue[] filterOutPreviouslyHandledIssues(List<AppScanIssue> issues, PushJobData jobData,
			List<String> errors) throws Exception;

	protected boolean shouldCheckDuplicates(PushJobData jobData) {
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
