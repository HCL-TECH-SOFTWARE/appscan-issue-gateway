# AppScan Issue Management Gateway Service (Alpha)

The AppScan Issue Management Gateway service helps to synchronize issues between [IBM Application Security on Cloud](https://appscan.ibmcloud.com/)
and other issue management systems such as JIRA. This capability should appeal to any AppScan users who need security 
issue data "pushed" into other systems and want to avoid building all the REST calls and plumbing themselves.  This service itself operates as a REST API, 
but as you'll see below the intent is to make this as painless as possible. 

The ideal use case for this service is as a part of an automated scanning workflow where it is getting called when necessary to perform the issue processing.

This service is brand new code and should be considered an early Alpha. We expect this service to evolve very quickly and welcome all feedback. 

# Prerequisites:

- A Java 8 Runtime
- A REST Client (such as "curl" or your language of choice) to submit requests to the service 
- An [IBM Application Security on Cloud API Key](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_generate_api_key_cm.html)

# Getting Started:

To try out the AppScan Issue Management Gateway Service, just download and unzip [the latest release](https://github.com/hclproducts/appscan-issue-gateway/releases).Then run the following
command to start the service on the default port (8080):

	java -jar appscan-issue-gateway.jar 

Choosing a different port is done like this:

	java -Dserver.port=4444 -jar appscan-issue-gateway.jar
	
The server will be started in a few seconds and you should see a Spring Boot logo printed. In a browser navigate to the REST API doc at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) : 

![](images/swagger.png?raw=true)

The service is currently very simple and provides two resources: 
1. GET /providers - Query the currently registered providers
2. POST & GET /issues/pushjobs - Submit and get the results from a "Push Job" that takes a set of Issues from AppScan and creates associated issues in JIRA 

To confirm that things are working correctly, expand the "GET /providers" API and hit the "Try It out!" button
![](images/tryitout.png?raw=true)

You should see that there are two providers registered: The JIRA Provider and a Sample Provider that is present to demonstrate the ease with which other providers can be added to the system.
![](images/providers.png?raw=true)

Before we start submitting jobs, let's take a look at the end goal: An automatically submitted JIRA issue with fields filled in from AppScan:

![](images/jirabug.png?raw=true)

Some things to note:
* All fields have been programmatically set including the Summary, the Description, the Priority and also some Labels
* There is a file attached which is a single-issue report that a developer can use to understand the details of the issue.

Now let's take a look at submitting jobs to the service. The following curl will do the trick:

curl command:

	curl http://localhost:8080/issues/pushjobs -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d @test.json 


The command above uses a JSON file which looks like the following

test.json:	

	{
		"appscanData": {
			"url":          "https://appscan.ibmcloud.com",
			"apikeyid":     "077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg",
			"apikeysecret": "kkV0jB/bClfdgjd4dfgmkjftfgo0Dfj/YNkMPp6w=",
			"appid":        "75c285f9-1995-e711-80ba-002324b5f40c",
			"policyids":    "351231200-0134212346-123461234asdrs6",
			"issuestates":  "New,Open",
			"maxissues":    3,
			"issuefilters": { "Status": "Open" }
		},
		"imData": {
			"provider": "jira",
			"config": {
				"url":        "http://localhost:8081",
				"username":   "xxxxxxxxxxxxxx",
				"password":   "xxxxxxxxxxxxxx",
				"projectkey": "SEC",
				"issuetype":  "Security",
				"summary":    "Security issue: %IssueType% found by %Scanner%",
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


Hopefully some of the JSON is self-explanatory, but here's a quick summary of what is being specified there:

__appscanData__: configuration required to connect to IBM Application Security on Cloud and extract issues
* url, apikeyid, apikeysecret: information required to authenticate with the AppScan REST APIs
* appid: The id of the application that will be used when querying for issues
* policyids: (Optional) Specific Policy Ids to be used when pulling the results from AppScan. If speciying multiple Policy Ids then provide a comma-separated list. By default, only issues that are open and out-of-compliance with the application's registered polices will be pulled.
* issuestates: (Optional) A specific set of issue states to focus on. Default = "Open"
* maxissues: (Optional) The maximum number of issues you want to process in this job.  This is helpful when playing
with the service and you just want to see what it will do with a small subset of your total issues. Default = 25
* issuefilters: (Optional) Additional filters to be used to further trim the results. These filters are regex expresion
that can act on issue attribute

__imData__: configuration required to connect to the Issue Management system (JIRA in this case)
* url, username, password: authentication
* projectkey: All issues submitted in JIRA must be submitted against a project
* issuetype: (Optional) Used if you would like to override the default issue type. Default = "Bug"
* summary: (Optional) Override the default summary that the JIRA provider uses.  Notice that there is basic support here for variable expansion to include required issue data in the summary
* severityfield: (Optional) The field id for the given issuetype that represents the "severity" or "priority". This field will be populated with the issue severity which can be configured with the "severitymap" field below.  Default value = "priority"
* severitymap: (Optional) AppScan severities are High, Medium, Low, Informational. With this setting you can change how those will be mapped when submitting the issue to JIRA.  For example, perhaps your team considers Medium security issues to be High priority
* otherfields: (Optional) This is an area where you can provide any other JSON that JIRA understands.  


# Known Issues & Limitations

- The service currently only supports JIRA but we are hoping to see more support added very quickly
- The JIRA support only handles Basic Auth (username and password)
- Some basic logging is in place for the service, but more work is needed to gracefully handle problems
- More work is required on build and packaging scripts
- A robust automated test suite is required
- The service is English only and would need to go through a String externalization exercise 

# License

All files found in this project are licensed under the [Apache License 2.0](LICENSE).
