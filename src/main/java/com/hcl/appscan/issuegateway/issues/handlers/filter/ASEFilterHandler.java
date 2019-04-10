package com.hcl.appscan.issuegateway.issues.handlers.filter;

import java.util.ArrayList;
import java.util.List;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.handlers.ASEExternalIdHandler;

public class ASEFilterHandler extends FilterHandler {

	private ASEExternalIdHandler externalIdHandler = new ASEExternalIdHandler();

	public ASEExternalIdHandler getExternalIdHandler() {
		return externalIdHandler;
	}

	@Override
	protected AppScanIssue[] filterOutPreviouslyHandledIssues(List<AppScanIssue> issues, PushJobData jobData,
			List<String> errors) throws Exception {
		return filterBasedOnExternalId(issues, jobData, errors);
	}

	private AppScanIssue[] filterBasedOnExternalId(List<AppScanIssue> issues, PushJobData jobData, List<String> errors)
			throws Exception {
		List<AppScanIssue> filteredIssues = new ArrayList<AppScanIssue>();
		final int maxIssueCount = jobData.getAppscanData().getMaxissues();
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
}
