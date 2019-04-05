/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hcl.appscan.issuegateway.AppscanProvider;
import com.hcl.appscan.issuegateway.issues.handlers.ASEIssueReportHandler;
import com.hcl.appscan.issuegateway.issues.handlers.CommentHandler;
import com.hcl.appscan.issuegateway.issues.handlers.CreateIssueAndSyncHandler;
import com.hcl.appscan.issuegateway.issues.handlers.FilterHandler;
import com.hcl.appscan.issuegateway.issues.handlers.IssueRetrievalHandler;
import com.hcl.appscan.issuegateway.issues.handlers.ReportHandler;
import com.hcl.appscan.issuegateway.jobs.Job;
import com.hcl.appscan.issuegateway.providers.ProvidersRepository;

import common.IProvider;

public class PushJob extends Job {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private PushJobData jobData;
	
	public PushJob (PushJobData jobData) {
		this.jobData = jobData;
	}
	
	@Override
	public Boolean call() throws Exception {
		try { 
			List<String> errors = new ArrayList<String>();
			Map<String,String> results = new HashMap<String, String>();
			
			updateResult(new PushJobResult(getId(), "Running - Retrieving AppScan issues", errors, results));
			AppScanIssue[] issues =  new IssueRetrievalHandler().retrieveIssues(jobData, errors);

			updateResult(new PushJobResult(getId(), "Running - Filtering AppScan issues", errors, results));
			FilterHandler filterHandler=new FilterHandler();
			AppScanIssue[] filteredIssues = filterHandler.filterIssues(issues, jobData, errors);
		
			updateResult(new PushJobResult(getId(), "Running - Retrieving AppScan issue reports", errors, results));
			if (jobData.getAppscanData().getAppscanProvider().equalsIgnoreCase(AppscanProvider.ASE.name())) {
				new ASEIssueReportHandler().retrieveReports(filteredIssues, jobData, errors);
			}
			else {
				new ReportHandler().retrieveReports(filteredIssues, jobData, errors);
			}
			
			IProvider provider = ProvidersRepository.getProviders(errors).get(jobData.getImData().getProvider());
			if (provider == null) {
				updateResult(new PushJobResult(getId(), "Complete - Failed with errors", errors, results));
				return false; 
			}
			if (jobData.getAppscanData().getAppscanProvider().equalsIgnoreCase(AppscanProvider.ASE.name())) {
				updateResult(new PushJobResult(getId(), "Running - Submitting issues and updating Appscan Issues", errors, results));
				new CreateIssueAndSyncHandler().createDefectAndUpdateId(filteredIssues, jobData, errors, results, provider);
			}
			else {
				updateResult(new PushJobResult(getId(), "Running - Submitting issues", errors, results));
				provider.submitIssues(filteredIssues, jobData.getImData().getConfig(), errors, results);
	            		
				updateResult(new PushJobResult(getId(), "Running - Updating AppScan issues", errors, results));
				new CommentHandler().submitComments(jobData, errors, results);
			}
			
			updateResult(new PushJobResult(getId(), "Completed", errors, results));
			return true;
		
		} catch (Exception e) {
			String error = "Internal Server Error: " + e.getMessage();
			updateResult(new PushJobResult(getId(), "Complete - Failed with errors", Arrays.asList(error), null));
			logger.error("Interal Server Error: ", e);
		}
		return true;
	}
		
}
