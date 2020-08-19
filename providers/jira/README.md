# Jira Provider

## Sample JSON model to start using the Jira integration
```json
{
  "appscanData": {
    "appscanProvider": "ASoC or ASE",
    "url": "https://cloud.appscan.com or https://hostname:port_number/ase ",
    "apikeyid": "00000000-0000-000000000-000000000000",
    "apikeysecret": "111111111-11-1111111-111111111111-1111-11111",
    "issuestates": "New,Open",
    "appid": "22222222222-2222222-22222222222222-2",
    "maxissues": 10,
    "policyids": "333333-33-3-333-3-3333333333333333333-333333-333333",
    "includeIssueFilters": {
      "Severity": "High"
    },
    "excludeIssuefilters": {},
    "other": {
      "checkduplicates": true
    }
  },
  "imData": {
    "provider": "jira",
    "config": {
      "url": "https://<jira server>",
      "username": "testuser",
      "password": "testpassword",
      "projectkey": "Test Project key",
      "issuetype": "Story",
      "summary": "Security issue: %IssueType% found by %Scanner%.",
      "severityfield": "severity",
      "severitymap": {
        "High": "Highest",
        "Medium": "Highest",
        "Low": "Highest",
        "Informational": "Highest"
      },
      "otherfields": {
        "labels": [
          "appscan",
          "security"
        ]
      }
    }
  }
}
```
Hopefully some of the JSON is self-explanatory, but here&#39;s a quick summary of what is being specified there:

**appscanData** : configuration required to connect to HCL Application Security on Cloud or HCL AppScan Enterprise and extract issues

- appscanProvider: Provider for AppScan Product. Use ASE for AppScan Enterprise and ASoC for AppScan On Cloud.
- url, apikeyid, apikeysecret: information required to authenticate with the AppScan REST APIs
- appid: The id of the application that will be used when querying for issues
- policyids: (Optional) Specific Policy Ids to be used when pulling the results from AppScan. If specifying multiple Policy Ids then provide a comma-separated list. By default, only issues that are open and out-of-compliance with the application&#39;s registered polices will be pulled. (Baseline is currently selected)
- issuestates: (Optional) A specific set of issue states to focus on. Default = &quot;Open&quot;
- maxissues: (Optional) The maximum number of issues you want to process in this job. This is helpful when playing with the service and you just want to see what it will do with a small subset of your total issues.
- includeIssuefilters, excludeIssuefilters: Additional filters to be used to further trim the results. These filters are regex expression that can act on issue attributes. Multiple values for a specific parameter can be provided. Specific issue can be moved by providing an issue id (only one id can be provided in includeFilters and if provided, other filters will be discarded).
- other: (Optional) Additional JSON that can be sent when creating JIRA issues. For example: { "checkduplicates": true }.

**imData** : configuration required to connect to the Issue Management system (Jira in this case)

- url: the JIRA URL to connect to.
- Username: The user name to connect to the Jira URL.
- Password: The password used to connect to the Jira URL.
- Projectkey: The Jira project name to be used for the issue migration. The issues would be migrated from ASE(or ASoC) to this project.
- issuetype: (Optional) Used if you would like to override the default issue type. Default = &quot;Bug&quot;
- summary: (Optional) Override default issue summary. Issue attributes can be included with %% substitution variables. For example the default is &#39;AppScan: %IssueType% found at %Location%&#39;
- severityfield: (Optional) Field Id that corresponds to &#39;priority&#39; or &#39;severity&#39;. This field will be populated with the AppScan Issue Severity. Default value = &#39;priority&#39;
- severitymap: (Optional) Map of AppScan Severities to JIRA Priorities .AppScan severities are High, Medium, Low, Informational. With this setting you can change how those will be mapped when submitting the issue to jira. For example, perhaps your team considers Medium security issues to be High priority
- otherfields: (Optional) Additional JSON that should be sent when creating JIRA issues. For example: { labels: [&#39;appscan&#39;,&#39;security&#39;] }.
