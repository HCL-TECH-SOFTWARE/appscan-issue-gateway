package com.hcl.appscan.issuegateway.appscanprovider;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;

import common.IProvider;

public interface IAppScanProvider {

	AppScanIssue[] getIssues(List<String> errors) throws Exception;

	AppScanIssue[] getFilteredIssues(AppScanIssue[] issues, List<String> errors);

	void retrieveReports(AppScanIssue[] filteredIssues, List<String> errors) throws Exception;

	void submitIssuesAndUpdateAppScanProvider(AppScanIssue[] filteredIssues, List<String> errors,
			Map<String, String> results, IProvider provider) throws Exception;

}
