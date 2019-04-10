package com.hcl.appscan.issuegateway.issues.handlers.issueretrieval;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.hcl.appscan.issuegateway.IssueGatewayConstants;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;

public abstract class IssueRetrievalHandler implements IssueGatewayConstants {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public abstract AppScanIssue[] retrieveIssues(PushJobData jobData, List<String> errors)  throws Exception;
	
    protected void setHeaders(HttpHeaders headers) {
    	headers.add("Content-Type", "application/json");
 		headers.add("Accept", "application/json");
 		headers.add("Accept-Language", "en-US,en;q=0.9");
    }
	
	//protected abstract String getAuthorizationHeaderName();
	
	//protected abstract String getIssueFilters(PushJobData jobData);

}
