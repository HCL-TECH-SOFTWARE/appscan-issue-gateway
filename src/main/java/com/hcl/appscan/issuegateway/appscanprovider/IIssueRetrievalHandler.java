package com.hcl.appscan.issuegateway.appscanprovider;

import java.util.List;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public interface IIssueRetrievalHandler {

	AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors) throws Exception;

}
