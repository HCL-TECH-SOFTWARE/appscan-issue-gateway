# VSTS Provider (now Azure Devops)

## Sample JSON model to start using the VSTS integration

```json
{
  "appscanData": {
    "appscanProvider":"ASOC",
    "url": "https://cloud.appscan.com",
    "apikeyid": "077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg",
    "apikeysecret": "077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg",
    "appid": "application_id",
    "policyids": "ebb9185a-45c9-e711-8de5-002590ac753d",
    "issuestates": "Open",
    "maxissues": 10,
    "includeIssuefilters": { "Severity":"High"},
    "excludeIssuefilters": {},
    "other": { "checkduplicates": "true" }
  },
  "imData": {
    "provider": "vsts",
    "config": {
      "url": "https://dev.azure.com/<VSTS_userid>/<VSTS_projectName>",
      "apiKey": "vsts_api_login_key",
      "issuetype": "Bug",
      "severitymap": {
        "High": "1 - Critical",
        "Medium": "2 - High",
        "Low": "3 - Medium",
        "Informational": "4 - Low"
      }
    }
  }
}
```

Hopefully some of the JSON is self-explanatory, but here's a quick summary of what is being specified there:

**appscanData**: configuration required to connect to HCL Application Security on Cloud or HCL AppScan Enterprise and extract issues

- appscanProvider: Provider for AppScan Product. Use ASE for AppScan Enterprise and ASOC for AppScan On Cloud.
- url, apikeyid, apikeysecret: information required to authenticate with the AppScan REST APIs
- appid: The id of the application that will be used when querying for issues
- policyids: (Optional) Specific Policy Ids to be used when pulling the results from AppScan. If speciying multiple Policy Ids then provide a comma-separated list. By default, only issues that are open and out-of-compliance with the application's registered polices will be pulled. (Baseline is currently selected)
- issuestates: (Optional) A specific set of issue states to focus on. Default = "Open"
- maxissues: (Optional) The maximum number of issues you want to process in this job. This is helpful when playing
  with the service and you just want to see what it will do with a small subset of your total issues.
- includeIssuefilters, excludeIssuefilters: Additional filters to be used to further trim the results. These filters are regex expression that can act on issue attributes. Multiple values for a specific parameter can be provided. Specific issue can be moved by providing an issue id (only one id can be provided in includeFilters and if provided, other filters will be discarded).

**imData**: configuration required to connect to the Issue Management system (VSTS in this case)

- url: as formated in the example add your team/project base url
  \*apiKey: the identification is done via a vsts api key, basic authentication model
- issuetype: (Optional) Used if you would like to override the default issue type. Default = "Bug"
- severitymap: (Optional) AppScan severities are High, Medium, Low, Informational. With this setting you can change how those will be mapped when submitting the issue to vsts. For example, perhaps your team considers Medium security issues to be High priority
