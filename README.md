# AppScan Issue Management Gateway Service

The AppScan Issue Management Gateway service synchronize issues between  [HCL AppScan On Cloud](https://cloud.appscan.com/), [HCL AppScan Enterprise(ASE)](https://www.hcl-software.com/appscan/products/appscan-enterprise), [HCL AppScan 360º](https://www.hcl-software.com/appscan/products/appscan360) and other issue management systems, such as Jira. AppScan users can "push" security issue data into other systems to avoid building all the REST calls and plumbing.  For seamless synchronization capability, this service itself operates as a REST API. An ideal use case of this service is an automated scanning workflow where it is called for issue processing.

## Prerequisites

- Java 8 runtime
- REST client (such as “curl” or your language of choice) to submit requests to the service
- [HCL AppScan on Cloud API Key](https://help.hcl-software.com/appscan/ASoC/appseccloud_generate_api_key_cm.html) , an [HCL AppScan Enterprise installation](https://help.hcl-software.com/appscan/Enterprise/10.0.0/topics/c_node_installing.html) or an [HCL AppScan 360º installation](https://help.hcl-software.com/appscan/360/1.3.0/InstallMain_360.html)
- Supported issue management system: Jira, VSTS (Azure DevOps), or RTC

## Getting Started

To use AppScan Issue Management Gateway service:

1.	Download “AppScan Issue Management Gateway Service” from https://github.com/hclproducts/appscan-issue-gateway/releases and extract its content to a location on your computer.

2.	Run the following command to start the service on the default port (8080):

java -jar appscan-issue-gateway.jar

3.	To run the service on a different port, use:

java -Dserver.port=4444 -jar appscan-issue-gateway.jar

The server starts in a few seconds and the Spring Boot logo appears. Open a browser to access the REST API documentation at  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) :

![swagger screenshot](docs/images/swagger.png?raw=true)

## Available API Resources
The service provides two API resources:

1.	GET /providers
Query the currently registered providers

2.	POST & GET /issues/pushjobs
Submit and get the results from a “push job” that takes a set of issues from AppScan and creates associated issues in Jira.

Note: The deprecated APIs, POST and GET /issues/pushjobs support only AppScan on Cloud issues. The new version of APIs, POST and GET /v2/issues/pushjobs supports both AppScan Enterprise and AppScan on Cloud.

## Example 
To confirm the service request status, expand the “GET /providers” API and click “Try It out!”

![get providers screenshot](docs/images/tryitout.png?raw=true)

The following providers are registered:

- Jira Provider
- RTC Provider
- VSTS Provider
- A sample provider to demonstrate the ease with which other providers can be added to the system. 

![sample provider screenshot](docs/images/providers.png?raw=true)

## Submitting Jobs

Before submitting jobs, identify the end goal: a Jira issue submitted automatically, with fields filled in from AppScan: 

![example jira issue screenshot](docs/images/jirabug.png?raw=true)

Notes:

- Summary: All fields, including Summary, Description, Priority, and Labels, are set programmatically.
- Attachments: The attached file is a single-issue report with issue details.

Use the following curl command to submit jobs to the service:

```sh
curl http://localhost:8080/issues/pushjobs -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d @test.json
```

This command uses a JSON file. For example: 

test.json:

```json
{
  "appscanData": {
    "appscanProvider": "<The provider of AppScan product>",
    "url": "<The root URL of the provider of AppScan Product>",
    "apikeyid": "077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg",
    "apikeysecret": "kkV0jB/bClfdgjd4dfgmkjftfgo0Dfj/YNkMPp6w=",
    "appid": "75c285f9-1995-e711-80ba-002324b5f40c",
    "policyids": "351231200-0134212346-123461234asdrs6",
    "issuestates": "Open",
    "maxissues": 3,
    "includeIssuefilters": {},
    "excludeIssuefilters": {},
    "trusted":"true",
    "other": {}
  },
  "imData": {
    "provider": "jira",
    "config": {
      "url": "http://localhost:8081",
      "username": "xxxxxxxxxxxxxx",
      "password": "xxxxxxxxxxxxxx",
      "projectkey": "SEC",
      "issuetype": "Security",
      "summary": "Security issue: %IssueType% found by %Scanner%",
      "severitymap": {
        "High": "High",
        "Medium": "High",
        "Low": "Low",
        "Informational": "Low"
      },
      "otherfields": {
        "labels": ["appscan", "security"]
      }
    }
  }
}
```
Note: A sample JSON file for an AppScan Enterprise request (sample_request_ase.json) can be found in the same repository
Use the file [ASE_issue_details_response.json](docs/samples/ase/ASE_issue_details_response.json)to learn about the different attributes of issue details in AppScan Enterprise which can be used in the request JSON.

Refer to the following summary for additional information about the JSON examples:

Note: The following information is for the new APIs /v2/issues/pushjobs. For deprecated APIs, refer to the Model and Example Value on the Swagger page.
## AppScan Data (appscanData)

**appscanData**: Required configuration to connect to HCL AppScan On Cloud or HCL AppScan Enterprise and extract issues

- **appscanProvider**: The provider of AppScan products. For AppScan Enterprise, it is ASE, for AppScan on Cloud, it is ASOC and for AppScan 360°, it is A360.
- **url, apikeyid, apikeysecret**: Information required to authenticate with the AppScan REST APIs.
- **appid**: The ID of the application used when querying for issues
- **policyids**: (Optional) Specific policy IDs for pulling the results from AppScan. If specifying multiple policy IDs, then provide a comma-separated list. By default, only issues that are open and out-of-compliance with the application’s registered policies will be pulled. This applies to AppScan on Cloud only and not to AppScan Enterprise.
- **issuestates**: (Optional) A specific set of issue states to include. Default = “Open”.
- **maxissues**: (Optional)The maximum number of issues to process in this job. This is helpful when experimenting with the service to experience how it handles a small subset of your total issues. Default = 25.
- **includeIssuefilters, excludeIssuefilters**: Additional filters for further trimming the results. These filters are regex expressions that can act on issue attributes. Multiple values for a specific parameter can be provided. The specific issue can be moved by providing an issue ID (only one ID can be provided in includeFilters and if provided, other filters will be discarded).
- **trusted** :Specify trusted or untrusted connections. This field is applicable only for AppScan 360. For trusted connection, specify ‘true’. For untrusted connections, specify ’’false”. For trusted connections, ensure that the AppScan 360° server root certificate is imported to the Java keystore. To import the root certificate into Java keystore use the following keytool command:
keytool -importcert -file "<PATH TO CERTIFICATE FILE>" -keystore "C:\Program Files\OpenLogic\jdk-17.0.7.7-hotspot\lib\security\cacerts" -alias  "a360rootcrt"

## Issue Management (imData)

**imData**: configuration required to connect to the Issue Management system (JIRA in this case)

- **url, username, password**: Authentication
- **projectkey**: All issues submitted in Jira must be submitted against a project.
- **issuetype**: (Optional) Override the default issue type. Default = “Bug”.
- **summary**: (Optional) Override the default summary used by the Jira provider. Notice that there is basic support here for variable expansion to include required issue data in the summary
- **severityfield**: (Optional) The field ID for the given issuetype that represents the “severity” or “priority”. This field is populated with the issue severity configured with the “severitymap” field below. Default value = “priority”.
- **severitymap**: (Optional) Change how AppScan severities are mapped when submitting the issue to Jira.  AppScan severities are High, Medium, Low, and Informational. .For example, if your team considers Medium security issues to be High priority, map it with severitymap.
- **otherfields**: (Optional)Provide any other JSON that Jira understands.

## Known Issues & Limitations

- **Jira authentication**: The service supports only Basic Auth (username and API token) or personal access tokens (available for AppScan on Cloud only).
- **Automated test suite**:A robust automated test suite is required for complete coverage.
- **Language support**: The service is English only and needs to go through a String externalization exercise for other languages.

## License

All files found in this project are licensed under the [Apache License 2.0](LICENSE.txt).
