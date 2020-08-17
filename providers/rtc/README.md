# **RTC Provider**

**Sample JSON model to start using the RTC integration**

{

&quot;appscanData&quot;: {

&quot;appscanProvider&quot;:&quot;ASoC or ASE&quot;,

&quot;url&quot;: &quot;https://cloud.appscan.com&quot; or &quot;https://hostname:port\_number/ase &quot;,

&quot;apikeyid&quot;: &quot;077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg&quot;,

&quot;apikeysecret&quot;: &quot;077---------sfgjfsdgjfgj------fgjgfjffgjgfjfg&quot;,

&quot;appid&quot;: &quot;application\_id&quot;,

&quot;policyids&quot;: &quot;ebb9185a-45c9-e711-8de5-002590ac753d&quot;,

&quot;issuestates&quot;: &quot;Open&quot;,

&quot;maxissues&quot;: 10,

&quot;includeIssuefilters&quot;: { &quot;Severity&quot;:&quot;High&quot;},

&quot;excludeIssuefilters&quot;: {},

&quot;other&quot;: { &quot;checkduplicates&quot;: &quot;true&quot; }

},

&quot;imData&quot;: {

&quot;provider&quot;: &quot;rtc&quot;,

&quot;config&quot;: {

&quot;url&quot;: &quot;https://localhost:9443/ccm&quot;,

&quot;username&quot;: &quot;username&quot;,

&quot;password&quot;: &quot;password&quot;,

&quot;projectarea&quot;: &quot;ABC&quot;,

&quot;issuetype&quot;: &quot;defect&quot;,

&quot;otherfields&quot;: {

&quot;filedAgainst&quot; : &quot;My Root Category Name/My Child Category&quot;,

}

} }

}

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

- url: The RTC URL to connect to Eg: https://localhost:9443/ccm&quot;.
- Username: The user name to connect to the RTC url.
- Password: The password used to connect to the RTC url.
- projectarea: The RTC project name to be used for the issue migration. The issues would be migrated from ASE(or ASoC) to this project.
- issuetype: (Optional) Used if you would like to override the default issue type. Default = &quot;Bug&quot;
- summary: (Optional) Issue attributes can be included with %% substitution variables. For example &#39;AppScan: %IssueType% found at %Location%
- description: (Optional) Issue attributes can be included with %% substitution variables. For example &#39;\&lt;b\&gt;Security issue:\&lt;/b\&gt; %IssueType%\&lt;br\&gt;\&lt;/br\&gt; \&lt;b\&gt;Scanner:\&lt;/b\&gt; %Scanner%.&#39;
- filedAgainst: The category of the issue in RTC.