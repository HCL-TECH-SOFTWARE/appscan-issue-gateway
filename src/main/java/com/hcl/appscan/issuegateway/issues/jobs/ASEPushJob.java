package com.hcl.appscan.issuegateway.issues.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hcl.appscan.issuegateway.appscanprovider.IAppScanProvider;
import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.issues.PushJobResult;
import com.hcl.appscan.issuegateway.issues.handlers.CreateIssueAndSyncHandler;
import com.hcl.appscan.issuegateway.jobs.Job;

import common.IProvider;

public class ASEPushJob extends Job {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private PushJobData jobData;
	private IAppScanProvider appscanProvider;
	private IProvider provider;

	public ASEPushJob(PushJobData jobData, IAppScanProvider appscanProvider, IProvider provider) {
		this.jobData = jobData;
		this.appscanProvider = appscanProvider;
		this.provider = provider;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			List<String> errors = new ArrayList<String>();
			Map<String, String> results = new HashMap<String, String>();

			// getIssues
			updateResult(new PushJobResult(getId(), "Running - Retrieving AppScan issues", errors, results));
			AppScanIssue[] issues = appscanProvider.getIssues(errors);

			// getFilteredIssues 
			updateResult(new PushJobResult(getId(), "Running - Filtering AppScan issues", errors, results));
			AppScanIssue[] filteredIssues = appscanProvider.getFilteredIssues(issues, errors);

			// retrieveReports
			updateResult(new PushJobResult(getId(), "Running - Retrieving AppScan issue reports", errors, results));
			appscanProvider.retrieveReports(filteredIssues, errors);
			
			//updateAppScanProvider
			
			updateResult(new PushJobResult(getId(), "Running - Submitting issues and updating Appscan Issues",
					errors, results));
			new CreateIssueAndSyncHandler().createDefectAndUpdateId(filteredIssues, jobData, errors, results,
						provider);
			
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
