# **RTC Provider**

**Sample JSON model to start using the RTC integration**
```json
{
  "appscanData": {
    "appscanProvider": "ASOC or ASE",
    "url": "https://cloud.appscan.com or https://hostname:port_number/ase",
    "apikeyid": "00000000-0000-000000000-000000000000",
    "apikeysecret": "111111111-11-1111111-111111111111-1111-11111",
    "appid": "22222222222-2222222-22222222222222-2",
    "issuestates": "New,Open",
    "maxissues": 10,
    "other": {
      "checkduplicates": false
    },
    "includeIssueFilters": {},
    "excludeIssueFilters": {}
  },
  "imData": {
    "provider": "rtc",
    "config": {
      "url": "https://<server>:<port>/ccm",
      "username": "username",
      "password": "password",
      "projectarea": "ABC",
      "issuetype": "defect",
      "otherfields": {
        "filedAgainst": "My Root Category Name/My Child Category"
      }
    }
  }
}
```
Hopefully some of the JSON is self-explanatory, but here&#39;s a quick summary of what is being specified there:

**appscanData** : configuration required to connect to HCL Application Security on Cloud or HCL AppScan Enterprise and extract issues

- appscanProvider: Provider for AppScan Product. Use ASE for AppScan Enterprise and ASOC for AppScan On Cloud.
- url, apikeyid, apikeysecret: information required to authenticate with the AppScan REST APIs
- appid: The id of the application that will be used when querying for issues
- policyids: (Optional) Specific Policy Ids to be used when pulling the results from AppScan. If specifying multiple Policy Ids then provide a comma-separated list. By default, only issues that are open and out-of-compliance with the application&#39;s registered polices will be pulled. (Baseline is currently selected)
- issuestates: (Optional) A specific set of issue states to focus on. Default = &quot;Open&quot;
- maxissues: (Optional) The maximum number of issues you want to process in this job. This is helpful when playing with the service and you just want to see what it will do with a small subset of your total issues.
- includeIssuefilters, excludeIssuefilters: Additional filters to be used to further trim the results. These filters are regex expression that can act on issue attributes. Multiple values for a specific parameter can be provided. Specific issue can be moved by providing an issue id (only one id can be provided in includeFilters and if provided, other filters will be discarded).

**imData** : configuration required to connect to the Issue Management system (RTC in this case)

- url: The RTC Server URL to connect to.
- Username: The user name to connect to the RTC Server  URL.
- Password: The password used to connect to the RTC Server URL.
- projectarea: The RTC project name to be used for the issue migration. The issues would be migrated from ASE(or ASoC) to this project.
- issuetype: Issue type of the RTC.
- otherfields: other fields
- filedAgainst: The category of the issue in RTC.
- summary: (Optional) Issue attributes can be included with %% substitution variables. For example &#39;AppScan: %IssueType% found at %Location%
- description: (Optional) Issue attributes can be included with %% substitution variables. For example "Security issue: %IssueType% Scanner:%Scanner%"
