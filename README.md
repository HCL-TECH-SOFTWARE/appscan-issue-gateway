# AppScan Issue Management Gateway Service (Alpha)

The AppScan Issue Management Gateway service helps to synchronize issues between [IBM Application Security on Cloud](https://appscan.ibmcloud.com/)
and other issue management systems such as JIRA. This capability should appeal to any AppScan users who need to get security 
issue data "pushed" into other systems and want to avoid building all the REST calls and plumbing themselves.  This service itself operates as a REST API, 
but as you'll see below the intent is to make this as painless as possible. 

The ideal use case for this service is as a part of an automated scanning workflow where it is getting called when necessary to perform the issue processing.

This service is brand new code and should be considered an early Alpha. We expect this service to evolve very quickly and welcome all feedback. 

# Prerequisites:

- A Java 8 Runtime
- A REST Client (such as "curl" or your language of choice) to submit requests to the service 
- An [IBM Application Security on Cloud API Key](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_generate_api_key_cm.html)

# Getting Started:

To try out the AppScan Issue Management Gateway Service, just download and unzip the latest release.Then run the following
command to start the service on the default port (8080):

	java -jar appscan-issue-gateway.jar 

Choosing a different port is done like this:

	java -Dserver.port=4444 -jar appscan-issue-gateway.jar
	
The server will be started in a few seconds and you should see a Spring Boot logo printed. In a browser navigate to the REST API doc at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) : 

	[Swagger Doc](images/swagger.png)

The service is currently very simple and provides two resources: 
1. GET /providers - Query the currently registered providers
2. POST|GET /issues/pushjobs - Submit and get the results from a "Push Job" that takes a set of Issues from AppScan and creates associated issues in JIRA 

To confirm that things are working correctly, expand the "GET /providers" API and hit the "Try It Out" button
You should see that there are to providers registered: The JIRA Provider and a Sample Provider that is present to demonstrate the ease with which other providers can be added to the system.

curl command:

	curl http://localhost:8080/issues/pushjobs -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d @test.json 
	 
test.json:
	
	{
	   "appscanData": {
	       "url":          "https://appscan.ibmcloud.com",
		    "apikeyid":     "077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg",
		    "apikeysecret": "kkV0jB/bClfdgjd4dfgmkjftfgo0Dfj/YNkMPp6w=",
		    "appid":        "75c285f9-1995-e711-80ba-002324b5f40c",
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
			    "issuetype":  "Bug",
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
}

# Known Limitations

- The service currently only supports JIRA but we are hoping to see more support added very quickly)
- We are seeing an issue where in some cases the PDF report that gets attached to a JIRA issue is getting corrupted and appears blank when opened. 
- The JIRA support only handles Basic Auth (username and password)
- Some basic logging is in place but more work is needed to gracefully handle problems
- More work is required on build and packaging scripts
- An robust automated test suite is required to help those wishing to help develop the system
- The service is English only and would need to go through a String externalization exercise 
- An robust automated test suite is required to help those wishing to help develop the system

# License

All files found in this project are licensed under the [Apache License 2.0](LICENSE).
