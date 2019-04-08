package com.hcl.appscan.issuegateway.appscanprovider;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.handlers.CommentHandler;
import com.hcl.appscan.issuegateway.issues.handlers.FilterHandler;
import com.hcl.appscan.issuegateway.issues.handlers.IssueRetrievalHandler;
import com.hcl.appscan.issuegateway.issues.handlers.ReportHandler;

public class ASOCProvider implements IAppScanProvider {

	private PushJobData jobData;

	public ASOCProvider(PushJobData jobData) {
		this.jobData = jobData;
	}

	@Override
	public AppScanIssue[] getIssues(List<String> errors) throws Exception {
		return new IssueRetrievalHandler().retrieveIssues(jobData, errors);
	}

	@Override
	public AppScanIssue[] getFilteredIssues(AppScanIssue[] issues, List<String> errors) {
		return new FilterHandler().filterIssues(issues, jobData, errors);
	}

	@Override
	public void retrieveReports(AppScanIssue[] filteredIssues, List<String> errors) throws Exception {
		new ReportHandler().retrieveReports(filteredIssues, jobData, errors);
	}

	@Override
	public void updateAppScanProvider(List<String> errors, Map<String, String> results) throws Exception {
		new CommentHandler().submitComments(jobData, errors, results);
	}

}
