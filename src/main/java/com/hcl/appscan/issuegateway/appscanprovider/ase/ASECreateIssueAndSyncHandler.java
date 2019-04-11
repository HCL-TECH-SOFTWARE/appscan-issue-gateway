/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.ase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hcl.appscan.issuegateway.issues.PushJobData;

import common.IAppScanIssue;
import common.IProvider;

public class ASECreateIssueAndSyncHandler {
	
	//Required fields
	private static String SERVER_URL  = "url";
	private static String USERNAME    = "username";
	private static String PASSWORD    = "password";
	private static String PROJECTKEY  = "projectkey";
		
	//Optional fields
	private static String ISSUETYPE     = "issuetype";
	private static String SEVERITYFIELD = "severityfield";
	private static String SEVERITYMAP   = "severitymap";
	private static String SUMMARY       = "summary";
	private ASEExternalIdHandler externalIdHandler = new ASEExternalIdHandler();
	
    public void createDefectAndUpdateId(IAppScanIssue[] issues,PushJobData jobData, List<String> errors, Map<String, String> results,IProvider provider ) throws Exception{
		if (validate(jobData.getImData().getConfig())) {
			for (IAppScanIssue issue:issues) {
				provider.submitIssue(issue, jobData.getImData().getConfig(), errors, results);
				externalIdHandler.updateExternalId(issue.get("id"), jobData, errors, results);
			}
		}
	}
	
	private boolean validate (Map<String, Object> config) throws EntityNotFoundException {
		//Check for required fields
		if (!config.containsKey(SERVER_URL) || (config.get(SERVER_URL)==null || config.get(SERVER_URL)=="")) {
			throw new EntityNotFoundException(PushJobData.class,SERVER_URL,"URL of Defect Tracking System is not found ");
		}
		if (!config.containsKey(USERNAME)|| (config.get(USERNAME)==null || config.get(USERNAME)=="")) {
			throw new EntityNotFoundException(PushJobData.class,USERNAME,"username of Defect Tracking System is not found ");
		}
		if (!config.containsKey(PASSWORD)|| (config.get(PASSWORD)==null || config.get(PASSWORD)=="")) {
			throw new EntityNotFoundException(PushJobData.class,PASSWORD,"password of Defect tracking system is not found ");
		}
		if (!config.containsKey(PROJECTKEY)|| (config.get(PROJECTKEY)==null || config.get(PROJECTKEY)=="")) {
			throw new EntityNotFoundException(PushJobData.class,PROJECTKEY,"project key is not found ");
		}
				
		//If there is a trailing / on the passed in JIRA URL remove it
		String serverURL = (String)config.get(SERVER_URL) ;
		if (serverURL.endsWith("/")) {
			config.put(SERVER_URL, serverURL.substring(0, serverURL.length() -1));
		}
				
		//Fill in the severity field if one wasn't specified
		if (config.get(SEVERITYFIELD) == null || config.get(SEVERITYFIELD)=="") {
			config.put(SEVERITYFIELD, "priority");
		}
				
		//Fill in a severity map if one wasn't specified		
		if (config.get(SEVERITYMAP) == null || config.get(SEVERITYMAP)=="")  {
			Map<String, String> severityMap = new HashMap<String, String>();
			severityMap.put("High", "Highest");
			severityMap.put("Medium", "High");
			severityMap.put("Low", "Low");
			severityMap.put("Information", "Lowest");
			config.put(SEVERITYMAP, severityMap);
		}
				
		//Set a String default summary if one doesn't exist
		if (config.get(SUMMARY) == null) {
			config.put(SUMMARY, "AppScan: %IssueType% found at %Location%");
		}
				
		//Set a String default issuetype if one doesn't exist
		if (config.get(ISSUETYPE) == null) {
			config.put(ISSUETYPE, "Bug");
		}
		return true;
	}
}
