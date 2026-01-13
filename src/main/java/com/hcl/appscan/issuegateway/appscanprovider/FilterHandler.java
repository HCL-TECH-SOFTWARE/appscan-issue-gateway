/**
 * © Copyright HCL Technologies Ltd. 2019,2026.
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
			return filterOutPreviouslyHandledIssues(filteredIssues, jobData, errors);
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

		List<AppScanIssue> filteredIssues = new ArrayList<>();

		// Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, List<Pattern>> patterns = new HashMap<>();
		for (String field : jobData.getAppscanData().getIncludeIssuefilters().keySet()) {
			String[] values = jobData.getAppscanData().getIncludeIssuefilters().get(field).split(",");
			List<Pattern> list = new ArrayList<>();
			for (String value : values) {
				list.add(Pattern.compile(value));
			}
			patterns.put(field, list);
		}

		for (AppScanIssue issue : issues) {
			if (containsMatch(issue, patterns)) {
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
		List<AppScanIssue> filteredIssues = new ArrayList<>();

		// Pre-compile the patterns so we don't have to do it each issue iteration
		Map<String, List<Pattern>> patterns = new HashMap<>();
		for (String field : jobData.getAppscanData().getExcludeIssuefilters().keySet()) {
			String[] values = jobData.getAppscanData().getExcludeIssuefilters().get(field).split(",");
			List<Pattern> list = new ArrayList<>();
			for (String value : values) {
				list.add(Pattern.compile(value));
			}
			patterns.put(field, list);
		}

		for (AppScanIssue issue : issues) {
			if (!containsMatch(issue, patterns)) {
				filteredIssues.add(issue);
			}
		}
		return filteredIssues;
	}

	private boolean containsMatch(AppScanIssue issue, Map<String, List<Pattern>> patterns) {
		for (String field : patterns.keySet()) {
			for (Pattern p : patterns.get(field)) {
				Matcher m = p.matcher((String)issue.get(field));
				if (m.matches()) {
					return true;
				}
			}
		}
		return false;
	}

	protected abstract AppScanIssue[] filterOutPreviouslyHandledIssues(List<AppScanIssue> issues, PushJobData jobData,
			List<String> errors) throws Exception;

	protected boolean shouldCheckDuplicates(PushJobData jobData) {
		return jobData.getAppscanData().getOther() == null
				|| jobData.getAppscanData().getOther().get("checkduplicates") == null
				|| !jobData.getAppscanData().getOther().get("checkduplicates").equals("false");
	}
}
