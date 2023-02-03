/**
 * � Copyright IBM Corporation 2018.
 * � Copyright HCL Technologies Ltd. 2018,2023.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package jira

import common.IAppScanIssue
import common.IProvider
import common.RESTUtils
import common.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class JIRAProvider extends JIRAConstants implements IProvider {
	
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
		try {
			if (validate(config, errors)) {
				for (IAppScanIssue issue : issues) {
					submitIssue(issue, config, errors, results);
				}
			}
		} catch (Exception e) {
			errors.add("Internal Server Error while submitting JIRA issues: " + e.getMessage())
		}

	}

	private Boolean validate(Map<String, String> config, List<String> errors) {
		
		//Check for required fields
		boolean valid = true;		
		if (!config.containsKey(SERVER_URL)) {
			errors.add("JIRA Configuration: URL must be set");
			valid = false;
		}
		if (!(config.containsKey(USERNAME) && config.containsKey(PASSWORD)) && !config.containsKey(TOKEN)) {
			errors.add("JIRA Configuration: Username and Password or Token must be set");
			valid = false;
		}
		if (!config.containsKey(PROJECTKEY)) {
			errors.add("JIRA Configuration: Project Key must be set");
			valid = false;
		}
		
		//If there is a trailing / on the passed in JIRA URL remove it
		def serverURL = config.get(SERVER_URL) 
		if (serverURL.endsWith("/")) {
			config.put(SERVER_URL, serverURL.substring(0, serverURL.length() -1))
		}
		
		//Fill in the severityfield if one wasn't specified
		if (config.get(SEVERITYFIELD) == null) {
			config.put(SEVERITYFIELD, "priority")
		}
		
		//Fill in a severity map if one wasn't specified		
		if (config.get(SEVERITYMAP) == null)  {
		Map<String, String> severityMap = new HashMap<String, String>();
			severityMap.put("High", "Highest");
			severityMap.put("Medium", "High");
			severityMap.put("Low", "Low");
			severityMap.put("Informational", "Lowest");
			config.put(SEVERITYMAP, severityMap);
		}
		
		//Set a default summary if one doesn't exist
		if (config.get(SUMMARY) == null) {
			config.put(SUMMARY, "AppScan: %IssueType% found at %Location%");
		}
		
		//Set a default issuetype if one doesn't exist
		if (config.get(ISSUETYPE) == null) {
			config.put(ISSUETYPE, "Bug");
		}
		return valid;
	}
	
	@Override
	public void submitIssue(IAppScanIssue appscanIssue, Map<String, Object> config, List<String> errors, Map<String, String> results) {
		def API_CREATEISSUE   = "/rest/api/latest/issue"
		def API_ADDATTACHMENT = "/rest/api/latest/issue/{issueKey}/attachments"
		
		try {
			def authorization = getAuthString(config)
			
			//Submit issue
			def createUrl = config.get(SERVER_URL) + API_CREATEISSUE
			def jsonPayload = createIssueJSON(appscanIssue, config)
			def postResultText = RESTUtils.postWithJSON(createUrl, authorization, jsonPayload, null, errors)
			def createdIssue = new JsonSlurper().parseText(postResultText)
			if (createdIssue.errors) {
				errors.add("Error while submitting issue at " + createUrl + ". " + createdIssue.errors.toString());
				return;
			}
			
			//Add attachment
			def jiraIssueKey = createdIssue.key
			def attachUrl = config.get(SERVER_URL) + API_ADDATTACHMENT.replace("{issueKey}", jiraIssueKey)
			def issueDetails = appscanIssue.issueDetails
			def jiraHeaders = ['X-Atlassian-Token':'no-check']
			postResultText = RESTUtils.postMultiPartFileUpload(attachUrl, authorization, issueDetails, "IssueDetails-" + jiraIssueKey + ".html", jiraHeaders, errors)
			if (postResultText == null) {
				errors.add("Something went wrong while attaching report to JIRA issue at " + attachUrl);
			}
			
			def jiraIssue = config.getAt(SERVER_URL) + "/browse/" + createdIssue.key
			// ASE issuedetails API returns "id" while the ASOC issues API returns "Id"
			if (appscanIssue.get("Id") == null || appscanIssue.get("Id") == ""){
				results.put(appscanIssue.get("id"), jiraIssue);
			}
			else {
				results.put(appscanIssue.get("Id"), jiraIssue);
			}
			
		} catch (Exception e) {
			errors.add("Internal Server Error while creating JIRA issues: " + e.getMessage())
		}
	}
			
	private getAuthString(Map<String, Object> config) {
		if(config.containsKey(USERNAME) && config.containsKey(PASSWORD)){
			def username = config.get(USERNAME)
			def password = config.get(PASSWORD)
			"Basic " + (username + ":" + password).bytes.encodeBase64().toString()
		}
		else {
			def token = config.get(TOKEN)
			"Bearer " + token
		}
	}
	
	private createIssueJSON(IAppScanIssue appscanIssue, Map<String, Object> config) {
		def projectKey = config.get(PROJECTKEY)
		def issueType = config.get(ISSUETYPE)
		def issueTypeString = "IssueType"
		def scanNameString ="ScanName";
		//"Issue Type" for ASE and "IssueType" for ASOC
		if (appscanIssue.get(issueTypeString)==null || appscanIssue.get(issueTypeString)=="" ){
			issueTypeString="Issue Type";
			scanNameString ="Scan Name";
		}
		def severity = Utils.escape(config.get(SEVERITYMAP).get(appscanIssue.get("Severity")))
		def severityField = Utils.escape(config.get(SEVERITYFIELD))
			
		//Must use \\n instead of \n for newlines when submitting to JIRA's REST API
		String description = appscanIssue.get("Scanner") + " found a " + severity + " priority issue"
        description += "\\n{quote}"
		description += "\\n*Issue Type*: " + Utils.escape(appscanIssue.get(issueTypeString))
		description += "\\n*Location*: "   + Utils.escape(appscanIssue.get("Location"))
		description += "\\n*Scan Name*: "  + Utils.escape(appscanIssue.get(scanNameString))
		description += "\\n{quote}"
		description += "\\nSee the attached report for more information"
	
		def summary = computeSummary(appscanIssue, config)
		
		def otherfields = "";
		if (config.get(OTHERFIELDS) != null) {
			otherfields = new JsonBuilder(config.get(OTHERFIELDS)).toPrettyString()
			otherfields = "," + otherfields.substring(1, otherfields.length()-1); //Trim off the leading and trailing curly braces
		}
		"""
            {
                "fields": {
                    "project":            { "key":  "${projectKey}" },
                    "issuetype":          { "name": "${issueType}" },
                    "${severityField}":   { "name": "${severity}" },
                    "summary":            "${summary}",
                    "description":        "${description}"
                    ${otherfields}
                }
            }
        """
	}
	
	private String computeSummary(IAppScanIssue appscanIssue, Map<String, Object> config) {
		def summary = config.get(SUMMARY)
		def elements = summary.split("%")
		String computedSummary = ""
		for (int i = 0; i < elements.size(); i += 2) {
			computedSummary += elements[i]
			if (i + 1 < elements.size()) {
				computedSummary += Utils.escape(appscanIssue.get(elements[i+1]))
			}
		}
		computedSummary
	}
}
