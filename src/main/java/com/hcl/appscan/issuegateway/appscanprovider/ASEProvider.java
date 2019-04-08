package com.hcl.appscan.issuegateway.appscanprovider;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public class ASEProvider implements IAppScanProvider {

	private PushJobData jobData;

	public ASEProvider(PushJobData jobData) {
		this.jobData = jobData;
	}

	@Override
	public AppScanIssue[] getIssues(List<String> errors) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppScanIssue[] getFilteredIssues(AppScanIssue[] issues, List<String> errors) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void retrieveReports(AppScanIssue[] filteredIssues, List<String> errors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAppScanProvider(List<String> errors, Map<String, String> results) {
		// TODO Auto-generated method stub

	}

}
