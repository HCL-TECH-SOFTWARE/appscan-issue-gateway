/**
 * © Copyright HCL Technologies Ltd. 2019. 
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */
package com.hcl.appscan.issuegateway.issues;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ASOCPushJobData {
	public AppScanDataV1 appscanData;
	public IMDataV1 imData;
	
	static public class AppScanDataV1 {
		@ApiModelProperty(position = 1, required = false, value = "The root URL if connecting to any version of ASoC other than public cloud", example = "https://appscan.ibmcloud.com")
		public String url = "https://appscan.ibmcloud.com";
		@ApiModelProperty(position = 2, required = true, value = "Your API Key Id generated from Application Security on Cloud")
		public String apikeyid;
		@ApiModelProperty(position = 3, required = true, value = "Your API Key Secret generated from Application Security on Cloud")
		public String apikeysecret;
		@ApiModelProperty(position = 4, required = true, value = "The Id of the ASoC application to be processed")
		public String appid;
		@ApiModelProperty(position = 5, required = false, example = "25", value = "Maximum number of issues to process. The default is 25. Pass in a value of -1 to have no limit")
		public Integer maxissues = 25;
		@ApiModelProperty(position = 6, required = false, value = "Issue States to process.  The default is to only process issues that are 'Open'. To also process new issues use 'New, Open'")
		public String issuestates = "Open";
		@ApiModelProperty(position = 7, required = false, value = "Comma separated list of ASoC policy ids used to filter the results. If not specified, the application's registered policies will be used")
		public String policyids;	
		@ApiModelProperty(position = 8, required = false, value = "List of regex experessions to run on Issue fields, to further filter the results")
		public Map<String, String> issuefilters;
		@ApiModelProperty(position = 9, required = false, value = "Other internal debug or demo settings")
		public Map<String, String> other;

	}
	
	static public class IMDataV1 {
		@ApiModelProperty(position = 1, required = true, value = "The Issue Management provider to use (e.g. 'jira')")
		public String provider;
		@ApiModelProperty(position = 2, required = true, value = "List of configuration settings to be used while processing the issues. See the help of the appropriate Issue Management provider for specifics")
		public Map<String,Object> config;
	}
}

