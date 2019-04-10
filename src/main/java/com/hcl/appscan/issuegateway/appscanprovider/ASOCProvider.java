package com.hcl.appscan.issuegateway.appscanprovider;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.handlers.ASOCCommentHandler;
import com.hcl.appscan.issuegateway.issues.handlers.ASOCReportHandler;
import com.hcl.appscan.issuegateway.issues.handlers.filter.ASOCFilterHandler;
import com.hcl.appscan.issuegateway.issues.handlers.issueretrieval.ASOCIssueRetrievalHandler;

import common.IProvider;

public class ASOCProvider implements IAppScanProvider {

	private PushJobData jobData;

	public ASOCProvider(PushJobData jobData) {
		this.jobData = jobData;
	}

	@Override
	public AppScanIssue[] getIssues(List<String> errors) throws Exception {
		return new ASOCIssueRetrievalHandler().retrieveIssues(jobData, errors);
	}

	@Override
	public AppScanIssue[] getFilteredIssues(AppScanIssue[] issues, List<String> errors) {
		return new ASOCFilterHandler().filterIssues(issues, jobData, errors);
	}

	@Override
	public void retrieveReports(AppScanIssue[] filteredIssues, List<String> errors) throws Exception {
		new ASOCReportHandler().retrieveReports(filteredIssues, jobData, errors);
	}

	@Override
	public void submitIssuesAndUpdateAppScanProvider(AppScanIssue[] filteredIssues, List<String> errors,
			Map<String, String> results, IProvider provider) throws Exception {
		provider.submitIssues(filteredIssues, jobData.getImData().getConfig(), errors, results);
		new ASOCCommentHandler().submitComments(jobData, errors, results);
	}
}
