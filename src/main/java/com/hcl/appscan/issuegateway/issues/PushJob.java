/**
 * © Copyright IBM Corporation 2018.
 * © Copyright HCL Technologies Ltd. 2018,2019. 
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

import com.hcl.appscan.issuegateway.appscanprovider.IAppScanProvider;
import com.hcl.appscan.issuegateway.jobs.Job;

import common.IProvider;

public class PushJob extends Job {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private PushJobData jobData;
	private IAppScanProvider appscanProvider;
	private IProvider provider;

	public PushJob(PushJobData jobData, IAppScanProvider appscanProvider, IProvider provider) {
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

			// getFilteredIssues TODO handle this in appscan Provider?
			updateResult(new PushJobResult(getId(), "Running - Filtering AppScan issues", errors, results));
			AppScanIssue[] filteredIssues = appscanProvider.getFilteredIssues(issues, errors);

			// retrieveReports
			updateResult(new PushJobResult(getId(), "Running - Retrieving AppScan issue reports", errors, results));
			appscanProvider.retrieveReports(filteredIssues, errors);
			
			// submit issues and update the AppScan provider
			updateResult(new PushJobResult(getId(), "Running - Submitting issues to DTS and updating AppScan Issues", errors, results));
			appscanProvider.submitIssuesAndUpdateAppScanProvider(filteredIssues,errors,results,provider);

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
