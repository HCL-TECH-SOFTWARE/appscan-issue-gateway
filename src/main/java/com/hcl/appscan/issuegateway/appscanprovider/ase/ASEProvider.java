package com.hcl.appscan.issuegateway.appscanprovider.ase;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.appscanprovider.IAppScanProvider;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

import common.IProvider;

public class ASEProvider implements IAppScanProvider {

	private PushJobData jobData;

	public ASEProvider(PushJobData jobData) {
		this.jobData = jobData;
	}

	@Override
	public AppScanIssue[] getIssues(List<String> errors) throws Exception {
		return new ASEIssueRetrievalHandler().retrieveIssues(jobData, errors);
	}

	@Override
	public AppScanIssue[] getFilteredIssues(AppScanIssue[] issues, List<String> errors) {
		return new ASEFilterHandler().filterIssues(issues, jobData, errors);
	}

	@Override
	public void retrieveReports(AppScanIssue[] filteredIssues, List<String> errors) {
		new ASEIssueReportHandler().retrieveReports(filteredIssues, jobData, errors);

	}

	@Override
	public void submitIssuesAndUpdateAppScanProvider(AppScanIssue[] filteredIssues, List<String> errors,
			Map<String, String> results, IProvider provider) throws Exception {
		new ASECreateIssueAndSyncHandler().createDefectAndUpdateId(filteredIssues, jobData, errors, results, provider);
	}
}
