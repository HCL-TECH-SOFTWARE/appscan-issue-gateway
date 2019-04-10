package com.hcl.appscan.issuegateway.issues.handlers.filter;

import java.util.ArrayList;
import java.util.List;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.handlers.ASOCCommentHandler;

public class ASOCFilterHandler extends FilterHandler {

	@Override
	protected AppScanIssue[] filterOutPreviouslyHandledIssues(List<AppScanIssue> issues, PushJobData jobData,
			List<String> errors) throws Exception {
		return filterBasedOnComment(issues, jobData, errors);
	}

	private AppScanIssue[] filterBasedOnComment(List<AppScanIssue> issues, PushJobData jobData, List<String> errors)
			throws Exception {
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		final int maxIssueCount = jobData.getAppscanData().getMaxissues();
		int issueCount = 0;
		ASOCCommentHandler commentHandler = new ASOCCommentHandler();
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
}
