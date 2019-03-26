/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues.handlers;

import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.issues.PushJobData;

import common.IAppScanIssue;
import common.IProvider;

public class CreateIssueAndSyncHandler {
	ExternalIdHandler externalIdHandler = new ExternalIdHandler();
	
	
	/*public void setExternalIdHandler(ExternalIdHandler externalIdHandler) {
		this.externalIdHandler = externalIdHandler;
	}*/


	public void createDefectAndUpdateId(IAppScanIssue[] issues,PushJobData jobData, Map<String, Object> config, List<String> errors, Map<String, String> results,IProvider provider ) throws Exception{
		for (IAppScanIssue issue:issues) {
			provider.submitIssue(issue, config, errors, results);
			externalIdHandler.updateExternalId(issue.get("id"), jobData, errors, results);
		}
	}
}
