/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.appscanprovider.ase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.util.HtmlUtils;

import com.hcl.appscan.issuegateway.issues.AppScanIssue;
import com.hcl.appscan.issuegateway.issues.PushJobData;
import com.hcl.appscan.issuegateway.providers.ProvidersRepository;

import common.IAppScanIssue;
import common.IProvider;

public class ASECreateIssueAndSyncHandler {
	
	
	private static String SERVER_URL    = "url";
	private static String USERNAME      = "username";
	private static String PASSWORD      = "password";
	private static String PROJECTKEY    = "projectkey";
	private static String APIKEY       = "apiKey";
	private static String OTHERFIELDS  = "otherfields";
	private static String PROJECTAREA  = "projectarea";
	private static String ISSUETYPE     = "issuetype";
	private static String SEVERITYFIELD = "severityfield";
	private static String SEVERITYMAP   = "severitymap";
	private static String SUMMARY       = "summary";
	private ASEExternalIdHandler externalIdHandler = new ASEExternalIdHandler();
	
    public void createDefectAndUpdateId(IAppScanIssue[] issues,PushJobData jobData, List<String> errors, Map<String, String> results,IProvider provider ) throws Exception{
		if (validate(jobData)) {
			for (IAppScanIssue issue:issues) {
				if (jobData.getImData().getProvider().equalsIgnoreCase("rtc")) {
					IProvider rtcInstance=ProvidersRepository.getProviders().get("rtc");
					Class<? extends IProvider> rtcProviderClass=rtcInstance.getClass();
					rtcProviderClass.getDeclaredMethod( "setupConnection", new Class[] {} ).invoke( rtcInstance, new Object[] {} ) ;
				}
				
				((AppScanIssue)issue).set("Issue Type", HtmlUtils.htmlUnescape(((AppScanIssue)issue).get("Issue Type")).replaceAll("\"", "'"));
				((AppScanIssue)issue).set("Location", HtmlUtils.htmlUnescape(((AppScanIssue)issue).get("Location")).replaceAll("\"", "'"));
				provider.submitIssue(issue, jobData.getImData().getConfig(), errors, results);
				externalIdHandler.updateExternalId(issue.get("id"), jobData, errors, results);
			}
		}
	}
	
	private boolean validate (PushJobData jobData) throws EntityNotFoundException {
		String DTSProvider=jobData.getImData().getProvider();
		Map<String, Object> config=jobData.getImData().getConfig();
		switch (DTSProvider.toLowerCase()) {
		case "jira":
			validateMandatoryFields(new String[]{SERVER_URL,USERNAME,PASSWORD,PROJECTKEY},config);
			break;
		case "vsts":
			validateMandatoryFields(new String[]{SERVER_URL,APIKEY},config);
			break;
		case "rtc":
			validateMandatoryFields(new String[]{SERVER_URL,USERNAME,PASSWORD,ISSUETYPE,PROJECTAREA,OTHERFIELDS},config);
			break;
		default:
			break;
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
			severityMap.put("Critical", "Highest");
			severityMap.put("High", "Highest");
			severityMap.put("Medium", "High");
			severityMap.put("Low", "Low");
			severityMap.put("Information", "Lowest");
			config.put(SEVERITYMAP, severityMap);
		}
				
		//Set a String default summary if one doesn't exist
		if (config.get(SUMMARY) == null || config.get(SUMMARY) == "") {
			config.put(SUMMARY, "Security issue: %Issue Type% found by %Scanner%");
		}
				
		//Set a String default issuetype if one doesn't exist
		if (config.get(ISSUETYPE) == null || config.get(ISSUETYPE) == "") {
			config.put(ISSUETYPE, "Bug");
		}
		return true;
	}
	
	private void validateMandatoryFields(String [] fields,Map<String, Object> config) throws EntityNotFoundException {
		List<String> emptyFields = new ArrayList<>();
		for (String field:fields) {
			if (!config.containsKey(field) || (config.get(field)==null || config.get(field)=="")) {
				emptyFields.add(field);
			}
		}
		if (!emptyFields.isEmpty()) {
			throw new EntityNotFoundException(PushJobData.class,emptyFields.toString(),"mandatory fields" +emptyFields+" of Defect Tracking System is/are not provided or invalid");
		}
	}
}
