# AppScan Issue Management Gateway Service

The AppScan Issue Management Gateway service helps to synchronize issues between [IBM Application Security on Cloud](https://appscan.ibmcloud.com/), [HCL AppScan On Cloud](https://cloud.appsechcl.com/), IBM AppScan Enterprise(ASE) and other issue management systems, such as JIRA. This capability helps AppScan users to get the security issue data "pushed" into other systems thereby avoid building all the REST calls and plumbing. For seamless synchronization capability, this service itself operates as a REST API. 
An ideal use case of this service is implemented in an automated scanning workflow where it is called for issue processing.

YouTube links (for IBM Application Security on Cloud and HCL AppScan on Cloud only):																		 
- Part 1: [https://youtu.be/7-a18ypMpM4](https://youtu.be/7-a18ypMpM4)
- Part 2: [https://youtu.be/_lsozLQ5CnM](https://youtu.be/_lsozLQ5CnM)

# Prerequisites:

- A Java 8 Runtime
- A REST Client (such as "curl" or your language of choice) to submit requests to the service 
- An [IBM Application Security on Cloud API Key](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_generate_api_key_cm.html)
- ASE installation (https://www.ibm.com/support/knowledgecenter/SSW2NF_9.0.3/com.ibm.ase.help.doc/helpindex_ase.html) to synchronize issues between ASE and other issue management systems such as JIRA.

# Getting Started:

To use the AppScan Issue Management Gateway service, perform the following:

Download “AppScan Issue Management Gateway Service” from https://github.com/hclproducts/appscan-issue-gateway/releases and extract its content to a location on your computer. 

Run the following command to start the service on the default port (8080):

	java -jar appscan-issue-gateway.jar 

Choosing a different port is as follows:

	java -Dserver.port=4444 -jar appscan-issue-gateway.jar  
	
The server starts in a few seconds and the Spring Boot logo appears. Open a browser, and access REST API doc at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) : 

![](images/swagger.png?raw=true)

The service provides two resources:  
1. GET /providers - Query the currently registered providers
2. POST & GET /issues/pushjobs - Submit and get the results from a "Push Job" that takes a set of Issues from AppScan and creates associated issues in JIRA.

Pleae Note : The deprecated APIs POST and GET /issues/pushjobs supports only ASOC issues .New new version of APIs i.e /v2/issues/pushjobs supports both ASE and ASOC.

To confirm the service request status, expand the "GET /providers" API and click the "Try It out!" button.
![](images/tryitout.png?raw=true)

Following providers are registered: 
1. Jira Provider
2. RTC PRovider 
3. VSTS Provider
4. A Sample Provider is also present to demonstrate the ease with which other providers can be added to the system.
![](images/providers.png?raw=true)

Before we start submitting jobs, let's take a look at the end goal: An automatically submitted JIRA issue with fields filled in from AppScan:
![](images/jirabug.png?raw=true)

Notes:
* All fields have been programmatically set including the Summary, the Description, the Priority and also some Labels.
* There is a file attached, which is a single-issue report that a developer can use to understand the details of the issue.

The following curl helps you submit jobs to the service:

curl command:

	curl http://localhost:8080/issues/pushjobs -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d @test.json

This command uses a JSON file.Refer the following example:(Also a sample json file for ASE request named sample_request_ase.json can be found in this repository)

test.json:	

	{
		"appscanData": {
			"appscanProvider": "<The provider of AppScan product>", 
			"url":          "<The root URL of the provider of AppScan Product>",
			"apikeyid":     "077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg",
			"apikeysecret": "kkV0jB/bClfdgjd4dfgmkjftfgo0Dfj/YNkMPp6w=",
			"appid":        "75c285f9-1995-e711-80ba-002324b5f40c",
			"policyids":    "351231200-0134212346-123461234asdrs6",
			"issuestates":  "New,Open",
			"maxissues":    3,
			"includeIssuefilters": {},
			"excludeIssuefilters": {},
			"other": {}
		},
		"imData": {
			"provider": "jira",
			"config": {
				"url":        "http://localhost:8081",
				"username":   "xxxxxxxxxxxxxx",
				"password":   "xxxxxxxxxxxxxx",
				"projectkey": "SEC",
				"issuetype":  "Security",
				"summary":    "Security issue: %Issue Type% found by %Scanner%",
				"severitymap": {
					"High":   "High",
					"Medium": "High",
					"Low":    "Low",
					"Informational": "Low"
				},
				"otherfields": {
					"labels" : [ "appscan", "security" ]
				}
			}
		}
	}

Please use the file ASE_issue_details_response.txt to know about the different attributes of issue details in ASE which can be used in the request json.

Few JSON are self-explanatory, but for the purpose of understanding, refer the following summary:

__appscanData__: configuration required to connect to IBM Application Security on Cloud or HCL AppScan On Cloud and extract issues
* appscanProvider: The provider of AppScan product.For AppScan Enterprise it is ASE and for AppScan on Cloud it is ASOC.
* url, apikeyid, apikeysecret: information required to authenticate with the AppScan REST APIs
* appid: The id of the application that will be used when querying for issues
* policyids: (Optional) Specific Policy Ids to be used when pulling the results from AppScan. If speciying multiple Policy Ids then provide a comma-separated list. By default, only issues that are open and out-of-compliance with the application's registered polices will be pulled. This is applicable only for ASoC and not for ASE.
* issuestates: (Optional) A specific set of issue states to focus on. Default = "Open"
* maxissues: (Optional) The maximum number of issues you want to process in this job.  This is helpful when playing
with the service and you just want to see what it will do with a small subset of your total issues. Default = 25
* includeIssuefilters, excludeIssuefilters:  Additional filters to be used to further trim the results. These filters are regex expression that can act on issue attribute. Multiple values for a specific parameter can be provided. Specific issue can be moved by providing an issue id (only one id can be provided in includeFilters and if provided, other filters will be discarded).

__imData__: configuration required to connect to the Issue Management system (JIRA in this case)
* url, username, password: authentication
* projectkey: All issues submitted in JIRA must be submitted against a project
* issuetype: (Optional) Used if you would like to override the default issue type. Default = "Bug"
* summary: (Optional) Override the default summary that the JIRA provider uses.  Notice that there is basic support here for variable expansion to include required issue data in the summary
* severityfield: (Optional) The field id for the given issuetype that represents the "severity" or "priority". This field will be populated with the issue severity which can be configured with the "severitymap" field below.  Default value = "priority"
* severitymap: (Optional) AppScan severities are High, Medium, Low, Informational. With this setting you can change how those will be mapped when submitting the issue to JIRA.  For example, perhaps your team considers Medium security issues to be High priority
* otherfields: (Optional) This is an area where you can provide any other JSON that JIRA understands.  

# Known Issues & Limitations
- The JIRA support only handles Basic Auth (username and password)
- Some basic logging is available for the service, but work on handling few limitations, is in progress.
- A robust automated test suite is required.
- The service is English only and need to go through a String externalization exercise.
- Jira APIs do not unescape the html entities in the summary so in case special character like double quotes, braces etc are present in the issue type , the html entities will be rendered as plain text .Same will work fine in the description .This issue will not be seen when it is fixed by the Jira developers.   
# License
All files found in this project are licensed under the [Apache License 2.0](LICENSE).
