package rtc
/**
 * © Copyright HCL Technologies Ltd. 2024.
 * © Copyright IBM Corporation 2018.
 * © Copyright PrimeUP Solucoes em TI LTDA 2018.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

import common.IAppScanIssue
import rtc.RTCConstants as Constants
import common.Utils
/**
 * This class encapsulates information from json file.
 */
class Attributes {
	
	String serverUrl 
	String username 
	String password 
	String projectArea 
	String workItemType			
	
	String summary
	String description	
	String categoryName
	String severity
	static def SEVERITYMAP   = "severitymap"
	RDFUtil rdfUtil
	Map <String,String> config
	IAppScanIssue appscanIssue

	/**
	 * Creates a Attributes based on the parameters
	 * @param config map of settings json
	 * @param connection connection server
	 */
	Attributes(Map <String,String> config, ServerCommunication connection) {	
		serverUrl = config.get(Constants.SERVER_URL)
		username = config.get(Constants.USERNAME)
		password = config.get(Constants.PASSWORD)
		projectArea = config.get(Constants.PROJECTAREA)
		workItemType = config.get(Constants.ISSUETYPE)
		
		rdfUtil = new RDFUtil(serverUrl, username, password, connection, projectArea, workItemType);
		this.config = config

		Map<String,String> otherfields = config.get(Constants.OTHERFIELDS)

		if (otherfields!= null) {
			setFiledAgaint(otherfields[Constants.FILED_AGAINST_PROPERTY])
		}			

	}
	
	/**
	 * Creates a Attributes based on the parameters
	 * @param config config json settings
	 * @param appscanIssue AppScan workitem
	 * @param connection connection server
	 */
	Attributes(Map <String,String> config, IAppScanIssue appscanIssue, ServerCommunication connection) {
		this(config, connection)
		if (config.get("severitymap") != null) {
			this.severity = Utils.escape(config.get(SEVERITYMAP).get(appscanIssue.get("Severity")))
			setSeverity(this.severity);
		}

		this.appscanIssue = appscanIssue
	}
	
	/**	
	 * Get workitem URL
	 * @return String to workitem connection
	 */
	String getWorkItemCreation() {
		return rdfUtil.workItemCreationURL
	}
	
	/**
	 * Get project area ID
	 * @return String to project area ID
	 */
	String getProjectAreaId() {
		return rdfUtil.projectAreaId
	}
	
	String setFiledAgaint(String categoryName) {
		if (categoryName != null && !categoryName.isEmpty()) {
			this.categoryName = rdfUtil.getValueURL(categoryName, "filedAgainst", "dcterms:title")
		} 
	}


	String setSeverity(String severity) {
		if (severity != null && !severity.isEmpty()) {
			this.severity = rdfUtil.getValueURL(severity, "severity", "dcterms:title")
		}
	}


	/**
	 * Get Filed Againt field URL
	 * @return String to filed againt connection
	 */
	String getFiledAgaint() {
		return categoryName
	}

	String getSeverity() {
		return severity
	}
		
	/**
	 * Get Summary of work item
	 * @return String summary
	 */
	String getSummary() {
		if (summary != null && !summary.isEmpty())
			return summary
		if (appscanIssue != null) 
			summary = computeTextWorkItem(appscanIssue, config, Constants.SUMMARY)

		return summary
	}
	
	/**
	 * Get Description of work item
	 * @return String description
	 */

	String getDescription() {
		if (description != null && !description.isEmpty())
			return description
		if (appscanIssue != null)
			description = computeTextWorkItem(appscanIssue, config, Constants.DESCRIPTION)
		if(description == null || description.isEmpty()){
			def issueTypeString = "IssueType"
			def scanNameString ="ScanName";
			def severity
			if (config.get("severitymap") != null)  {
				severity = Utils.escape(config.get(SEVERITYMAP).get(appscanIssue.get("Severity")))
			}else{
				severity = appscanIssue.get("Severity");
			}
			description = appscanIssue.get("Scanner") + " found a " + severity + " severity issue."
			description += "\nIssue Type: " + Utils.escape(appscanIssue.get(issueTypeString))
			description += "\nLocation: "   + Utils.escape(appscanIssue.get("Location"))
			description += "\nScan Name: "  + Utils.escape(appscanIssue.get(scanNameString))

			description += "\nSee the attached report for more information"
		}
		return description
	}
	
	
	/**
	 * Removes any double quotes.
	 * @param theString string to be treated
	 * @return String processed or empty string
	 */
	private String escape(String theString) {
		if(theString != null)
			return theString.replaceAll("\"", "'")
		else
			return ""
	}

	/**
	 * Method responsible for creating summary and description from AppScanIssue
	 * @param appscanIssue AppScan issue
	 * @param config map of settings json
	 * @param workItemAttribute field text to be treated
	 * @return String with value field inputed
	 */

	private String computeTextWorkItem(IAppScanIssue appscanIssue, Map<String, Object> config, String workItemAttribute) {
		Object item = config.get(workItemAttribute)

		if (item == null)
			return ""
		Collection elements = item.split("%")
		String computedText = ""
		for (int i = 0; i < elements.size(); i += 2) {
			computedText += elements[i]
			if (i+1 < elements.size()) {
				computedText += escape(appscanIssue.get(elements[i+1]))
			}
		}
		return computedText
	}	
}
