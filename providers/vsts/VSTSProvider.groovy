/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018,2019.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package vsts

import java.util.List
import java.util.Map

import common.IAppScanIssue
import common.IProvider
import common.RESTUtils
import common.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 *  Note: For now the provider loading logic is very basic (and I guess fragile).
 *  Originally the plan was to just use the folder names as proper package names, but I spent more time than I'd care to admit
 *  trying to figure out why the GroovyClassLoader on the Java side seemed to ignore packages and get confused easily when loading classes. 
 *  So this does need a better solution, but for now the rules of Provider file naming are:
 *  1. Name your provider XXXProvider.groovy where XXX is going to be a prefix unlikely to clash with other providers
 *  2. Name any other helper classes you create with the same prefix. 
 *  3. One provider per folder
 */
class VSTSProvider extends VSTSConstants implements IProvider {

	@Override
	public String getId() {
		return PROVIDER_NAME;
	}
	
	@Override
	List<String> getDescription() {
		return PROVIDER_DESCRIPTION;
	}
	
	@Override
	public void submitIssues(IAppScanIssue[] issues, Map<String, Object> config, List<String> errors, Map<String, String> results) {
		//Just extract the Id and Severity out of each issue and return them back as a result Map (which the caller of the Service REST API could do something with);	
		try {
			if (validate(config, errors)) {
				for (IAppScanIssue issue : issues) {
					submitIssue(issue, config, errors, results);
				}	
			}
		} catch (Exception e) {
			errors.add("Internal Server Error while submitting VSTS issues: " + e.getMessage())
		}
	}
	
	private Boolean validate(Map<String, String> config, List<String> errors) {
		
		//Check for required fields
		boolean valid = true;
		if (!config.containsKey(SERVER_URL)) {
			errors.add("VSTS Configuration: URL must be set");
			valid = false;
		}
		if (!config.containsKey(API_KEY )) {
			errors.add("VSTS Configuration: API key must be set");
			valid = false;
		}
		
		//If there is a trailing / on the passed in vsts URL remove it
		def serverURL = config.get(SERVER_URL)
		if (serverURL.endsWith("/")) {
			config.put(SERVER_URL, serverURL.substring(0, serverURL.length() -1))
		}
		
		//Fill in a severity map if one wasn't specified
		if (config.get(SEVERITYMAP) == null)  {
		Map<String, String> severityMap = new HashMap<String, String>();
			severityMap.put("High", "1 - Critical");
			severityMap.put("Medium", "2 - High");
			severityMap.put("Low", "3 - Medium");
			severityMap.put("Informational", "4 - Low");
			config.put(SEVERITYMAP, severityMap);
		}
		
		//Set a default issuetype if one doesn't exist
		if (config.get(ISSUETYPE) == null) {
			config.put(ISSUETYPE, "Bug");
		}

		return valid;
	}
	
	@Override
	public void submitIssue(IAppScanIssue appscanIssue, Map <String,String> config, List<String> errors, Map<String, String> results){
		def API_CREATEISSUE   = "/_apis/wit/workitems/\$${config.get(ISSUETYPE)}?api-version=1.0"
		def API_UPLOADATTACHMENT = "/_apis/wit/attachments?fileName=issueDetails-{issueKey}.html&api-version=1.0"
		def API_ADDATTACHMENT = "/_apis/wit/workitems/{issueKey}?api-version=1.0" 
		def WORKITEMS = "/_workitems/edit/"
		
		try{
			def authorization = getAuthString(config)
			
			//Submit issue
			def createUrl = config.get(SERVER_URL) +  API_CREATEISSUE
			def jsonPayload = createIssueJSON (appscanIssue, config)
			def patchResult = RESTUtils.patchWithJSON(createUrl, authorization, jsonPayload, null, errors)
			def createdIssue = new JsonSlurper().parseText(patchResult)
			if (createdIssue.errors) {
				errors.add("Error while submitting issue at "+ createUrl + ". " +createdIssue.errors.toString());
				return ;
			}
			
			//Upload the html description 
			def vstsIssueId = createdIssue.id.toString()
			def vstsIssueUrl = config.get(SERVER_URL) + WORKITEMS + vstsIssueId
			def uploadUrl = config.get(SERVER_URL) + API_UPLOADATTACHMENT.replace("{issueKey}",vstsIssueId)
			def issueDetails = appscanIssue.issueDetails
			def uploadResultText = RESTUtils.postOctetStreamFileUpload(uploadUrl,authorization,issueDetails,null,errors)
			if (uploadResultText == null) {
				errors.add("Something went wrong while uploading report to VSTS issue at " + uploadUrl);
			}
			
			
			//Attach the html description  
			def uploadedFile = new JsonSlurper().parseText(uploadResultText)
			def attachUrl = config.get(SERVER_URL) + API_ADDATTACHMENT.replace("{issueKey}",vstsIssueId)
			jsonPayload = defineAttachment(uploadedFile.url)
			patchResult = RESTUtils.patchWithJSON(attachUrl, authorization, jsonPayload, null, errors)
			def updatedIssue = new JsonSlurper().parseText(patchResult)
			if (updatedIssue.errors) {
				errors.add("Error while attaching issue detail for VSTS at "+ attachUrl + ". " +createdIssue.errors.toString());
				return ;
			}

			if (appscanIssue.get("Id") == null || appscanIssue.get("Id") == ""){
				results.put(appscanIssue.get("id"), vstsIssueUrl);
			}
			else {
				results.put(appscanIssue.get("Id"), vstsIssueUrl);
			}
		}		
		catch (Exception e) {
			errors.add("Internal Server Error while submitting VSTS issue:" + e.getMessage())
		}	
	}
	
	private defineAttachment(String attUrl) {
		def attachmentUrl=attUrl
				
		"""[
			{
			  "op": "add",
			  "path": "/fields/System.History",
			  "value": "Added the issue details"
			},
			{
			  "op": "add",
			  "path": "/relations/-",
			  "value": {
				"rel": "AttachedFile",
				"url": "${attachmentUrl}",
				"attributes": {
				  "comment": "Issue details"
				}
			  }
			}
		  ]"""
	}

	
	private createIssueJSON(IAppScanIssue appscanIssue, Map<String, Object> config) {
		def issueTypeString = "IssueType"
		def scanNameString ="ScanName";
		
		//"Issue Type" and "Scan Name" for ASE and "IssueType" for ASOC
		if (appscanIssue.get(issueTypeString) == null || appscanIssue.get(issueTypeString) == "" ){
			issueTypeString="Issue Type";
			scanNameString ="Scan Name";
		}
		def issueType = Utils.escape(config.get("issuetype"))
		def severity = config.get("severitymap").get(appscanIssue.get("Severity"))
		def title = Utils.escape(appscanIssue.get(issueTypeString)) + " found at: " + Utils.escape(appscanIssue.get("Location"))
		
		//Write description
		String description = "\n*Issue Type*: " + Utils.escape(appscanIssue.get(issueTypeString))
		description += "\n*Location*: "   + Utils.escape(appscanIssue.get("Location"))
		description += "\n*Scan Name*: "  + Utils.escape(appscanIssue.get(scanNameString))
		description += "\nSee the attached report for more information"
		
		"""[
			{
			  "op": "add",
			  "path": "/fields/System.Title",
			  "value": "${title}"
			},
			{
			  "op": "add",
			  "path": "/fields/System.Description",
			  "value": "${description}"
			},
			{
			 "op": "add",
			 "path": "/fields/Microsoft.VSTS.Common.Severity",
			 "value": "${severity}"
			
			}
		  ]"""
	}
	
	private getAuthString(Map<String, Object> config) {
			def apiKey = config.get (API_KEY)
			"Basic " + (apiKey + ":").bytes.encodeBase64().toString()
	}
}
